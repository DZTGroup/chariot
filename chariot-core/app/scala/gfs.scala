package com.aperture.docx.scala

import play.api.Logger

import reactivemongo.bson._
import reactivemongo.api._
import reactivemongo.api.gridfs._
import reactivemongo.api.gridfs.Implicits._

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current
import play.api.cache.Cache

import scala.util.{Success, Failure}
import scala.concurrent.blocking
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Gfs {
	// Gets a reference to the database
	// read db conf from conf context
	private val db = ReactiveMongoPlugin.db
	private val collection = db("docs")
	
	private val gridFS = new GridFS(db)
	gridFS.ensureIndex().onComplete {
		case flag =>
			Logger.info(s"Checked index, result is $flag")
	}
	
	def save(name:String, in:java.io.InputStream):String = {
		
		val fileToSave = DefaultFileToSave(name, Some("application/octet-stream"))
	  
		//write returns Future[ReadFile[BSONValue]]
		val futureResult = gridFS.writeFromInputStream(fileToSave, in)
		
		Await.ready(futureResult, Duration.Inf).value.get match {
			case Success(rf) =>
				rf.id match {
					case id:BSONObjectID => {
						Logger.info("saving done! id:"+ id.stringify)
						id.stringify
					}
					case _ => throw new Exception("[WTF]impossible error.")
				}
			case Failure(e) =>
				throw e
		}
	}
	
	
	def load(id:String, out:java.io.OutputStream):Unit = {
		val now = System.nanoTime
		
		// TODO: add cache setting to some config
		// caching
		val data: Array[Byte] = Cache.getOrElse[Array[Byte]](key = "module_" + id, expiration = 12 * 3600) {
			val maybeFile = gridFS.find(BSONDocument("_id" -> new BSONObjectID(id))).headOption
		
			Await.ready(maybeFile, Duration.Inf).value.get match {
				case Success(Some(fileToLoad)) => blocking {
					
					val readBuffer = new java.io.ByteArrayOutputStream()
					// loadTask is Future[Unit]
					val loadTask = gridFS.readToOutputStream(fileToLoad, readBuffer)

					Await.ready(loadTask, Duration.Inf).value.get match {
						// we successfully wrote the file contents on disk
						case Success(_) =>{
							Logger.info("loading done! id:" + id + ". cast time:" + (System.nanoTime - now) / math.pow(10, 9))
							readBuffer.toByteArray()
						}
						case Failure(e) =>
							throw e
					}
				}
				case Success(None) =>
					throw new Exception("not found! id:" + id)
				case Failure(e) =>
					throw e
			}
		}
		
		data.map(_.toInt).foreach(out.write)
	}
}