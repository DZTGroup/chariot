package com.aperture.docx

import play.api.Logger

import reactivemongo.api.gridfs._
import reactivemongo.api.gridfs.Implicits._

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.Play.current

class Sample{
	def test() = "scala! @ " + System.getProperty("user.dir")
	
	def connect() {
		import reactivemongo.api._
		import scala.concurrent.ExecutionContext.Implicits.global

		// Gets a reference to the database
		// read db conf from conf context
		val db = ReactiveMongoPlugin.db

		// Gets a reference to the collection "acoll"
		// By default, you get a BSONCollection.
		val collection = db("docs")
	  
		val gridFS = new GridFS(db)
		gridFS.ensureIndex().onComplete {
			case flag =>
			Logger.info("Checked index, result is $flag")
		}
	  
		val name = "sample.docx"
		val path = System.getProperty("user.dir")+"/template/debug/" + name
		val contentType = "application/octet-stream"
	  
		val fileToSave = DefaultFileToSave(name, Some(contentType))
	  
		import java.io.{File, FileInputStream}
		//Future[ReadFile[BSONValue]]
		val futureResult = gridFS.writeFromInputStream(fileToSave, new FileInputStream(new File(path)))
	  
		import scala.util.{Success, Failure}
		import reactivemongo.bson._
		futureResult.onComplete {
			case Success(rf) => {
				rf.id match{
					case id:BSONObjectID => Logger.info("save done! id:"+ id.stringify)
					case _ =>
				}
			}
			case Failure(t) => Logger.info(t.getMessage)
		}
	}
}