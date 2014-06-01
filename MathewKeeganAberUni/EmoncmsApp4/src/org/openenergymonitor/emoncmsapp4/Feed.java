package org.openenergymonitor.emoncmsapp4;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;

public class Feed implements Serializable {
	
	int id;
	String name;
	int datatype;
	String tag;
	Long time;
	Double value;
	String dpInterval;
	
	HashMap<Long, Double> historicFeedData;
	
	public Feed() {
		historicFeedData = new HashMap<Long, Double>();
	}
	
	public Feed(int id, String name, int datatype, String tag, Long time,
			Double value, String dpInterval) {
		super();
		this.id = id;
		this.name = name;
		this.datatype = datatype;
		this.tag = tag;
		this.time = time;
		this.value = value;
		this.dpInterval = dpInterval;
		historicFeedData = new HashMap<Long, Double>();
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDatatype() {
		return datatype;
	}
	public void setDatatype(int datatype) {
		this.datatype = datatype;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public String getDpInterval() {
		return dpInterval;
	}
	
	public void setDpInterval(String dpInterval) {
		this.dpInterval = dpInterval;
	}
	
	/* Adds a key/pair value to the Hash Map */
	public void addElement(Long unixTime, Double feedValue) {
		historicFeedData.put(unixTime, feedValue);
	}
	
	/* Returns the value of a key/pair using Unix Time as key */
	public Double getElement(Long unixTime) {
		Double value = historicFeedData.get(unixTime);
		return value;		
	}
	
	/* Clear all key/pairs from Hash Map */
	public void clearHistoricFeedData() {
		historicFeedData.clear();
	}
	
	public String toString() {
		String s = "Object : "+id+" / "+name+" / "+tag+" / "+time+" / "+value;
		return s;
	}
	
	public String printHashMap() {
		
		Iterator iterator = historicFeedData.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = historicFeedData.get(key).toString();
			
			System.out.println("Timestamp : "+key+" / Value : "+value);
		}
		
		return null;
	}


	
}
