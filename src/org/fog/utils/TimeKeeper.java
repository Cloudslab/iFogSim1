package org.fog.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeKeeper {

	private static TimeKeeper instance;
	
	private int count; 
	private Map<Integer, Double> emitTimes;
	private Map<Integer, Double> endTimes;
	private Map<Integer, List<Integer>> loopIdToTupleIds;
	
	public static TimeKeeper getInstance(){
		if(instance == null)
			instance = new TimeKeeper();
		return instance;
	}
	
	public int getUniqueId(){
		return count++;
	}
	
	public Map<Integer, List<Integer>> loopIdToTupleIds(){
		return getInstance().getLoopIdToTupleIds();
	}
	
	private TimeKeeper(){
		count = 1;
		setEmitTimes(new HashMap<Integer, Double>());
		setEndTimes(new HashMap<Integer, Double>());
		setLoopIdToTupleIds(new HashMap<Integer, List<Integer>>());
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Map<Integer, Double> getEmitTimes() {
		return emitTimes;
	}

	public void setEmitTimes(Map<Integer, Double> emitTimes) {
		this.emitTimes = emitTimes;
	}

	public Map<Integer, Double> getEndTimes() {
		return endTimes;
	}

	public void setEndTimes(Map<Integer, Double> endTimes) {
		this.endTimes = endTimes;
	}

	public Map<Integer, List<Integer>> getLoopIdToTupleIds() {
		return loopIdToTupleIds;
	}

	public void setLoopIdToTupleIds(Map<Integer, List<Integer>> loopIdToTupleIds) {
		this.loopIdToTupleIds = loopIdToTupleIds;
	}
	
	
}
