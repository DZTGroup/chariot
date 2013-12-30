package com.aperture.docx.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationService {
	Map<String, Notifier> watchList = new HashMap<String, Notifier>();
	List<String> order = new ArrayList<String>();
	
	public interface Notifier{
		void tail(Object obj);
		void forceTailing();
	}
	
	public String getId(){
		return UUID.randomUUID().toString();
	}
	
	public void watch(String id, Notifier p){
		order.add(id);
		watchList.put(id, p);
	}
	
	public void unwatch(String id){
		order.remove(id);
		watchList.remove(id);
	}
	
	Collection<Notifier> get(){
		List<Notifier> list = new ArrayList<Notifier>(); 
		for(String id:order){
			list.add(watchList.get(id));
		}
		return list;
	}
}
