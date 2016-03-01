package org.fog.dsp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.dsp.bruteforce.OperatorPlacementBruteForce;
import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.TupleEmitTimes;
import org.fog.utils.TupleFinishDetails;

public class Controller extends SimEntity{

	public static double RESOURCE_MANAGE_INTERVAL = 100;
	public static double LATENCY_WINDOW = 1000;
	
	public static boolean ONLY_CLOUD = false;
	
	private OperatorPlacement operatorPlacement;
	
	private List<FogDevice> fogDevices;
	
	private Map<String, StreamQuery> queries;
	private Map<String, Integer> queryLaunchDelays;
	
	private Map<String, Queue<Double>> tupleLatencyByQuery;

	public Controller(String name, List<FogDevice> fogDevices) {
		super(name);
		this.queries = new HashMap<String, StreamQuery>();
		this.setQueryLaunchDelays(new HashMap<String, Integer>());
		for(FogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setTupleLatencyByQuery(new HashMap<String, Queue<Double>>());
		System.out.println("Constructor done");
	}

	@Override
	public void startEntity() {
		// TODO Auto-generated method stub
		for(String queryId : queries.keySet()){
			if(getQueryLaunchDelays().get(queryId)==0)
				processQuerySubmit(queries.get(queryId));
			else
				send(getId(), getQueryLaunchDelays().get(queryId), FogEvents.QUERY_SUBMIT, queries.get(queryId));
		}

		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.QUERY_SUBMIT:
			processQuerySubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		}
	}

	protected double calculateAverageTupleLatency(String queryId){
		Queue<Double> tupleLatencies = getTupleLatencyByQuery().get(queryId);
		double sum = 0;
		for(Double d : tupleLatencies){
			sum += d;
		}
		return sum/tupleLatencies.size();
	}
	
	protected boolean queryNeedsHelp(String queryId){
		double averageLatency = calculateAverageTupleLatency(queryId);
		//System.out.println("Average latency for "+queryId+" = "+averageLatency);
		return false;
	}
	
	protected void helpQuery(String queryId){
		
	}
	
	protected void manageResources(){
		/*for(String queryId : getQueries().keySet()){
			if(queryNeedsHelp(queryId)){
				helpQuery(queryId);
			}				
		}*/
		send(getId(), RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
		TupleFinishDetails details = (TupleFinishDetails)ev.getData();
		double latency = (details.getFinishTime()-details.getEmitTime());
		if(getTupleLatencyByQuery().get(details.getQueryId()).size() >= LATENCY_WINDOW)
			getTupleLatencyByQuery().get(details.getQueryId()).remove();
		getTupleLatencyByQuery().get(details.getQueryId()).add(latency);
		TupleEmitTimes.setLatency(details.getQueryId(), details.getActualTupleId(), details.getFinishTime()-details.getEmitTime());
		displayTupleDetails(details);
	}
	
	private void displayTupleDetails(TupleFinishDetails details){
		String sensorType = details.getSensorType(); 
		if(sensorType.equals("TYPE1"))
			System.out.println(sensorType+" : "+details.getActualTupleId()+"\t---->\t"+(details.getFinishTime()-details.getEmitTime()));
	}

	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}
	
	public void submitStreamQuery(StreamQuery streamQuery, int delay){
		FogUtils.queryIdToGeoCoverageMap.put(streamQuery.getQueryId(), streamQuery.getGeoCoverage());
		getQueries().put(streamQuery.getQueryId(), streamQuery);
		getQueryLaunchDelays().put(streamQuery.getQueryId(), delay);
	}
	
	private void processQuerySubmit(SimEvent ev){
		StreamQuery query = (StreamQuery) ev.getData();
		processQuerySubmit(query);
	}
	
	private void processQuerySubmit(StreamQuery query){
		System.out.println(CloudSim.clock()+" Submitted query "+query.getQueryId());
		FogUtils.queryIdToGeoCoverageMap.put(query.getQueryId(), query.getGeoCoverage());
		getQueries().put(query.getQueryId(), query);
		getTupleLatencyByQuery().put(query.getQueryId(), new LinkedList<Double>());
		
		
		Map<String, Integer> allocationMap = null;
		if(ONLY_CLOUD)
			allocationMap = (new OperatorPlacementOnlyCloud(fogDevices, query)).getOperatorToDeviceMap();
		else
			allocationMap = (new OperatorPlacementBruteForce(fogDevices, query)).getOperatorToDeviceMap();
		
		for(FogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_QUERY_UPDATE, query);
		}
		
		for(String operatorName : allocationMap.keySet()){
			StreamOperator operator = query.getOperatorByName(operatorName);
			
			sendNow(allocationMap.get(operatorName), FogEvents.QUERY_SUBMIT, query);
			
			sendNow(allocationMap.get(operatorName), FogEvents.LAUNCH_OPERATOR, operator);
		}
	}
	
	public OperatorPlacement getOperatorPlacement() {
		return operatorPlacement;
	}

	public void setOperatorPlacement(OperatorPlacement operatorPlacement) {
		this.operatorPlacement = operatorPlacement;
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Queue<Double>> getTupleLatencyByQuery() {
		return tupleLatencyByQuery;
	}

	public void setTupleLatencyByQuery(Map<String, Queue<Double>> tupleLatencyByQuery) {
		this.tupleLatencyByQuery = tupleLatencyByQuery;
	}

	public Map<String, StreamQuery> getQueries() {
		return queries;
	}

	public void setQueries(Map<String, StreamQuery> queries) {
		this.queries = queries;
	}

	public Map<String, Integer> getQueryLaunchDelays() {
		return queryLaunchDelays;
	}

	public void setQueryLaunchDelays(Map<String, Integer> queryLaunchDelays) {
		this.queryLaunchDelays = queryLaunchDelays;
	}
}
