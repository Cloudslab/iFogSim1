package org.fog.utils;

import java.util.HashMap;
import java.util.Map;

public class TupleEmitTimes {

	private Map<String, Map<Integer, Double>> timeMap;
	
	private static TupleEmitTimes instance;
	
	public static TupleEmitTimes getInstance(){
		if(instance==null)
			instance = new TupleEmitTimes();
		return instance;
	}
	
	public static void removeEmitTime(String queryId, int tupleId){
		getInstance().getTimeMap().get(tupleId).remove(tupleId);
	}
	
	public static void setLatency(String queryId, int tupleId, double time){
		if(!getInstance().getTimeMap().containsKey(queryId))
			getInstance().getTimeMap().put(queryId, new HashMap<Integer, Double>());
		getInstance().getTimeMap().get(queryId).put(tupleId, time);
	}
	
	public static double getLatency(String queryId, int tupleId){
		return getInstance().getTimeMap().get(queryId).get(tupleId);
	}
	
	private TupleEmitTimes(){
		this.setTimeMap(new HashMap<String, Map<Integer, Double>>());
	}

	public Map<String, Map<Integer, Double>> getTimeMap() {
		return timeMap;
	}

	public void setTimeMap(Map<String, Map<Integer, Double>> timeMap) {
		this.timeMap = timeMap;
	}
	
	
}
