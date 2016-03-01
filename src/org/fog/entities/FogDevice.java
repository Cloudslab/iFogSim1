package org.fog.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.dsp.StreamQuery;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;
import org.fog.utils.OperatorSetComparator;
import org.fog.utils.ResourceUsageDetails;
import org.fog.utils.TupleFinishDetails;

public class FogDevice extends Datacenter {
	private static boolean PRINTING_ENABLED = false;
	private static void print(String msg){
		if(PRINTING_ENABLED)System.out.println(CloudSim.clock()+" : "+msg);
	}
	private static boolean ADAPTIVE_REPLACEMENT = true;
	private static double RESOURCE_USAGE_COLLECTION_INTERVAL = 10;
	private static double RESOURCE_USAGE_VECTOR_SIZE = 100;
	private static double INPUT_RATE_TIME = 1000;
	private static double ADAPTIVE_INTERVAL = 10000;
	private Queue<Tuple> outgoingTupleQueue;
	private boolean isOutputLinkBusy;
	private double missRate;
	private double uplinkBandwidth;
	private double latency;
	private List<String> activeQueries;
	private Map<String, StreamQuery> streamQueryMap;
	private Map<String, List<String>> queryToOperatorsMap;
	private GeoCoverage geoCoverage;
	private Map<Pair<String, Integer>, Double> inputRateByChildId;
	private Map<Pair<String, Integer>, Integer> inputTuples;
	
	private Map<Pair<String, Integer>, Queue<Double>> inputTupleTimesByChildOperatorAndNode;
	
	protected Queue<Double> outputTupleTimes;
	protected Map<String, Queue<Double>> outputTupleTimesByOperator;
	protected Map<String, Queue<Double>> intermediateTupleTimesByOperator;
	
	protected Map<Pair<String, String>, Double> inputRateByChildOperator;
	protected Map<Pair<String, String>, Integer> inputTuplesByChildOperator;
	protected Map<Pair<String, String>, Double> tupleLengthByChildOperator;
	protected Map<Pair<String, String>, Double> tupleCountsByChildOperator;
	protected Map<String, Double> outputTupleLengthsByOperator;
	
	protected Map<Pair<String, Integer>, Queue<Double>> inputTupleTimes;
	protected Map<Pair<String, String>, Queue<Double>> inputTupleTimesByChildOperator;
	protected Map<String, Queue<Double>> utilization; 
	
	protected Map<Integer, Integer> cloudTrafficMap;
	
	protected double lockTime;
	
	/**	
	 * ID of the parent Fog Device
	 */
	protected int parentId;
	
	/**
	 * ID of the Controller
	 */
	private int controllerId;
	/**
	 * IDs of the children Fog devices
	 */
	private List<Integer> childrenIds;

	private Map<Integer, List<String>> childToOperatorsMap;
	
	public FogDevice(
			String name, 
			GeoCoverage geoCoverage,
			FogDeviceCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval,
			double uplinkBandwidth, double latency) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setGeoCoverage(geoCoverage);
		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);
		setUplinkBandwidth(uplinkBandwidth);
		setLatency(latency);
		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}
		setActiveQueries(new ArrayList<String>());
		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}

		// stores id of this class
		getCharacteristics().setId(super.getId());
		
		streamQueryMap = new HashMap<String, StreamQuery>();
		queryToOperatorsMap = new HashMap<String, List<String>>();
		outgoingTupleQueue = new LinkedList<Tuple>();
		setOutputLinkBusy(false);
		setMissRate(0);
		
		this.inputRateByChildId = new HashMap<Pair<String, Integer>, Double>();
		this.inputTuples = new HashMap<Pair<String, Integer>, Integer>();
		this.inputTupleTimes = new HashMap<Pair<String, Integer>, Queue<Double>>();
		this.inputTupleTimesByChildOperator = new HashMap<Pair<String, String>, Queue<Double>>();
		
		this.utilization = new HashMap<String, Queue<Double>>();
		
		this.tupleLengthByChildOperator = new HashMap<Pair<String, String>, Double>();
		this.tupleCountsByChildOperator = new HashMap<Pair<String, String>, Double>();
		this.inputRateByChildOperator = new HashMap<Pair<String, String>, Double>();
		this.inputTuplesByChildOperator = new HashMap<Pair<String, String>, Integer>();
		
		this.outputTupleTimes = new LinkedList<Double>();
		this.outputTupleTimesByOperator = new HashMap<String, Queue<Double>>();
		this.intermediateTupleTimesByOperator = new HashMap<String, Queue<Double>>();
		this.outputTupleLengthsByOperator = new HashMap<String, Double>();
		setInputTupleTimesByChildOperatorAndNode(new HashMap<Pair<String, Integer>, Queue<Double>>());
		setChildrenIds(new ArrayList<Integer>());
		setChildToOperatorsMap(new HashMap<Integer, List<String>>());
		
		this.cloudTrafficMap = new HashMap<Integer, Integer>();
		
		this.lockTime = 0;
	}

	/**
	 * Overrides this method when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		updateResourceUsage();
		performAdativeReplacement(null);
	}
	
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ARRIVAL:
			processTupleArrival(ev);
			break;
		case FogEvents.LAUNCH_OPERATOR:
			processOperatorArrival(ev);
			break;
		case FogEvents.RELEASE_OPERATOR:
			processOperatorRelease(ev);
			break;
		case FogEvents.SENSOR_JOINED:
			processSensorJoining(ev);
			break;
		case FogEvents.QUERY_SUBMIT:
			processQuerySubmit(ev);
			break;
		case FogEvents.UPDATE_RESOURCE_USAGE:
			updateResourceUsage();
			break;
		case FogEvents.UPDATE_TUPLE_QUEUE:
			updateTupleQueue();
			break;
		case FogEvents.ACTIVE_QUERY_UPDATE:
			updateActiveQueries(ev);
			break;
		case FogEvents.ADAPTIVE_OPERATOR_REPLACEMENT:
			performAdativeReplacement(ev);
			break;
		case FogEvents.GET_RESOURCE_USAGE:
			sendResourceUsage();
			break;
		case FogEvents.RESOURCE_USAGE:
			processResourceUsage(ev);
			break;
		default:
			break;
		}
	}
	
	protected List<String> getSubtreeApexes(List<String> operators, String queryId){
		print(getName()+"\tOperators : "+operators);
		List<String> apexes =  new ArrayList<String>();
		StreamQuery query = getStreamQueryMap().get(queryId);
		for(String operator : operators){
			boolean isApex = true;
			for(String operator1 : operators){
				/*System.out.println(operator1);
				System.out.println(query.getEdges());*/
				if(query.getEdges().get(operator)!=null && query.getEdges().get(operator).equals(operator1))
					isApex = false;
			}
			if(isApex)
				apexes.add(operator);
		}
		return apexes;
	}
	
	protected List<String> getSubtreeLeaves(List<String> operators, String queryId){
		List<String> leaves =  new ArrayList<String>();
		StreamQuery query = getStreamQueryMap().get(queryId);
		for(String operator : operators){
			boolean isLeaf = true;
			for(String operator1 : operators){
				if(query.getEdges().get(operator1)!=null && query.getEdges().get(operator1).equals(operator))
					isLeaf = false;
			}
			if(isLeaf)
				leaves.add(operator);
		}
		return leaves;
	}
	protected List<String> getSubtreeChildren(String operator, List<String> operators, String queryId){
		List<String> children = new ArrayList<String>();
		for(String op : operators){
			if(getStreamQueryMap().get(queryId).getEdges().get(op) != null && getStreamQueryMap().get(queryId).getEdges().get(op).equals(operator))
				children.add(op);
		}
		return children;
	}
	
	/**
	 * Returns the paths of operators present in the subset of operators. The paths are always in the order of leaf-to-root
	 * @param operators
	 * @param queryId
	 * @return
	 */
	public List<List<String>> getPathsInOperatorSubset(List<String> operators, String queryId){
		//TODO 
		StreamQuery query = getStreamQueryMap().get(queryId);
		//System.out.println("Subset of operators : "+operators);
		List<List<String>> paths = new ArrayList<List<String>>();
		for(String leaf : getSubtreeLeaves(operators, queryId)){
			List<String> path = new ArrayList<String>();
			String op = leaf;
			while(op != null && operators.contains(op)){
				path.add(op);
				op = query.getEdges().get(op);
			}
			paths.add(path);
		}
		//System.out.println("Paths : "+paths);
		return paths;
	}
	
	protected boolean canBeSentToCpu(List<String> operators, int childDeviceId, ResourceUsageDetails resourceUsageDetails, String queryId){
		StreamQuery streamQuery = getStreamQueryMap().get(queryId);
		double cpuLoad = 0;
		Map<String, Double> outputRateMap = new HashMap<String, Double>();
		
		List<String> leaves = getSubtreeLeaves(operators, queryId);
		for(String leaf : leaves){
			// calculate the output rate of each leaf operator in the subtree
			
			System.out.println("Children of "+leaf+" : "+streamQuery.getAllChildren(leaf));
			double outputRate = 0;
			for(String childOperator : streamQuery.getAllChildren(leaf)){
				if(streamQuery.isSensor(childOperator))
					cpuLoad += getInputRateByChildOperatorAndNode(childOperator, childDeviceId)*streamQuery.getTupleCpuLengthOfSensor(FogUtils.getSensorTypeFromSensorName(childOperator));
				else
					cpuLoad += getInputRateByChildOperatorAndNode(childOperator, childDeviceId)*streamQuery.getOperatorByName(childOperator).getTupleLength();
				//CORRECTED outputRateMap.put(leaf, streamQuery.getSelectivity(leaf, childOperator)*getInputRateByChildOperatorAndNode(childOperator, childDeviceId));
				outputRate += streamQuery.getSelectivity(leaf, childOperator)*getInputRateByChildOperatorAndNode(childOperator, childDeviceId);
			}
			//CORRECTED 
			outputRateMap.put(leaf, outputRate);
		}
		
		//now calculate the output rates of all the non-leaf operators in the subtree
		boolean done = false;	// denotes whether output rate of all operators has been calculated
		while(!done){
			done = true;
			for(String operator : operators){
				if(!outputRateMap.containsKey(operator)){
					double outputRate = 0, cpuLoadOperator = 0; boolean bool = true;
					for(String child : getSubtreeChildren(operator, operators, queryId)){
						if(!outputRateMap.containsKey(child)){
							bool = false;
							break;
						}
						outputRate += outputRateMap.get(child)*streamQuery.getSelectivity(operator, child);
						cpuLoadOperator += outputRateMap.get(child)*streamQuery.getOperatorByName(child).getTupleLength();
					}
					if(bool){
						outputRateMap.put(operator, outputRate);
						cpuLoad += cpuLoadOperator;
					}
					done = false;
				}
			}
		}
		
		// NOW, cpuLoad REPRESENTS THE LOAD ON CPU INCURRED BY operators IF THEY ARE MOVED TO childId
		
		//System.out.println(getName()+"\t"+CloudSim.getEntityName(childDeviceId)+"\t"+cpuLoad);
		
		double finalTrafficIntensityOnChild = (cpuLoad+resourceUsageDetails.getCpuTrafficIntensity()*resourceUsageDetails.getMips())/resourceUsageDetails.getMips(); 
		
		if(finalTrafficIntensityOnChild > 1)
			return false;
		
		// NOW CALCULATING THE COST OF RUNNING THE SUBSET OF OPERATORS ON CHOSEN CHILD DEVICE
		
		List<List<String>> paths = getPathsInOperatorSubset(operators, queryId);
		
		double maxCostChildDevice = -1;
		for(List<String> path : paths){
			double pathCost = calculatePathCPUCostOnChildDevice(path, queryId, childDeviceId, 
					finalTrafficIntensityOnChild*resourceUsageDetails.getMips(), resourceUsageDetails.getMips());
			if(pathCost > maxCostChildDevice)
				maxCostChildDevice = pathCost;
		}
		
		// NOW CALCULATING THE COST OF RUNNING THE SUBSET OF OPERATORS ON CURRENT DEVICE
		double maxCostCurrentDevice = -1;
		for(List<String> path : paths){
			double pathCost = calculatePathCPUCostOnCurrentDevice(path, queryId, childDeviceId, 
					calculateCpuLoad()*getHost().getTotalMips(), getHost().getTotalMips());
			if(pathCost > maxCostCurrentDevice)
				maxCostCurrentDevice = pathCost;
		}
		//return (maxCostChildDevice < maxCostCurrentDevice);
		return true;
	}
	
	protected double calculatePathCPUCostOnCurrentDevice(List<String> path, String queryId, int childDeviceId, double currentTrafficLoad, double mips){
		StreamQuery streamQuery = getStreamQueryMap().get(queryId);
		double cost = 0;
		double inputRate = 0;
		for(int i=0;i<path.size();i++){
			String operator = path.get(i);
			if(i==0){
				double maxCost = -1;
				for(String childOperator : streamQuery.getAllChildren(operator)){// TAKING THE MAXIMUM COST AMONG ALL OPERATORS SENDING TUPLES TO THE PATH LEAF
					double cost1 = currentTrafficLoad/(getInputRateByChildOperatorAndNode(childOperator, childDeviceId)*mips);
					if(cost1 > maxCost)
						maxCost= cost1;
					inputRate += streamQuery.getSelectivity(operator, childOperator)*getInputRateByChildOperatorAndNode(childOperator, childDeviceId);
				}
				cost += maxCost;
			}else{
				String prevOperator = path.get(i-1);
				cost += currentTrafficLoad/(inputRate*mips);
				inputRate = inputRate*streamQuery.getSelectivity(operator, prevOperator);
			}
		}
		//System.out.println("Cost of running "+path+" on "+getName()+" = "+cost);
		return cost;
	}
	
	
	protected double calculatePathNwCostOnCurrentDevice(List<String> path, String queryId, int childDeviceId, double currentNwLoadOnChild, double childBw){
		StreamQuery streamQuery = getStreamQueryMap().get(queryId);
		String operator = path.get(0);
		double maxCost = -1;
		for(String childOperator : streamQuery.getAllChildren(operator)){// TAKING THE MAXIMUM COST AMONG ALL OPERATORS SENDING TUPLES TO THE PATH LEAF
			double cost = currentNwLoadOnChild/(getInputRateByChildOperatorAndNode(childOperator, childDeviceId)*childBw);
			/*
			System.out.println("Calculating path nw cost on current device");
			System.out.println("currentNwLoadOnChild = "+currentNwLoadOnChild);
			System.out.println("InputRateByChildOperatorAndNode = "+getInputRateByChildOperatorAndNode(childOperator, childDeviceId));
			*/
			if(cost > maxCost)
				maxCost= cost;
		}
		return maxCost;	
	}
	
	protected double calculatePathNwCostOnChildDevice(List<String> path, String queryId, int childDeviceId, double finalTrafficLoadOnChild, double childBw){
		StreamQuery streamQuery = getStreamQueryMap().get(queryId);
		double outputRate = 0;
		for(int i=0;i<path.size();i++){
			String operator = path.get(i);
			if(i==0){
				for(String childOperator : streamQuery.getAllChildren(operator))
					outputRate += streamQuery.getSelectivity(operator, childOperator)*getInputRateByChildOperatorAndNode(childOperator, childDeviceId);
			}else{
				String prevOperator = path.get(i-1);
				outputRate = outputRate*streamQuery.getSelectivity(operator, prevOperator);
			}
		}
		/*
		System.out.println("Calculating path nw cost on child device");
		System.out.println("finalNwLoadOnChild = "+finalTrafficLoadOnChild);
		System.out.println("outputRate = "+outputRate);
		*/
		return finalTrafficLoadOnChild/(outputRate*childBw);
	}
	
	protected double calculatePathCPUCostOnChildDevice(List<String> path, String queryId, int childDeviceId, double finalTrafficLoad, double mips){
		StreamQuery streamQuery = getStreamQueryMap().get(queryId);
		
		double cost = 0;
		double inputRate = 0;
		for(int i=0;i<path.size();i++){
			String operator = path.get(i);
			if(i==0){
				double maxCost = -1;
				for(String childOperator : streamQuery.getAllChildren(operator)){
					double cost1 = finalTrafficLoad/(getInputRateByChildOperatorAndNode(childOperator, childDeviceId)*mips);
					if(cost1 > maxCost)
						maxCost= cost1;
					inputRate += streamQuery.getSelectivity(operator, childOperator)*getInputRateByChildOperatorAndNode(childOperator, childDeviceId);
				}
				cost += maxCost;
			}else{
				String prevOperator = path.get(i-1);
				cost += finalTrafficLoad/(inputRate*mips);
				inputRate = inputRate*streamQuery.getSelectivity(operator, prevOperator);
			}
		}
		return cost;
	}
	
	protected boolean canBeSentTo(List<String> operators, int childDeviceId, ResourceUsageDetails resourceUsageDetails, String queryId){
		for(String operator : operators){
			if(getChildToOperatorsMap().get(childDeviceId).contains(operator))
				return false;
		}
		boolean cpu = canBeSentToCpu(operators, childDeviceId, resourceUsageDetails, queryId);
		//TODO cpu sending condition needs to be improved. N/w sending conditions need to be added.
		return cpu;
	}
	
	/**
	 * Process resource usage of child and decide whether to send operators to that child or not
	 * @param ev
	 */
	protected void processResourceUsage(SimEvent ev) {
		/*for(Pair<String, Integer> pair : getInputTupleTimesByChildOperatorAndNode().keySet()){
			System.out.println(pair.getFirst()+"\t"+CloudSim.getEntityName(pair.getSecond())+"\t---->\t"+getInputRateByChildOperatorAndNode(pair.getFirst(), pair.getSecond()));
		}*/
		
		ResourceUsageDetails resourceUsageDetails = (ResourceUsageDetails)ev.getData();
		int childId = ev.getSource();

		//finding out which query has what all operators running on the device
		Map<String, List<StreamOperator>> map = new HashMap<String, List<StreamOperator>>();
		for(Vm vm : getHost().getVmList()){
			StreamOperator streamOperator = (StreamOperator)vm;
			if(map.containsKey(streamOperator.getQueryId()))
				map.get(streamOperator.getQueryId()).add(streamOperator);
			else{
				map.put(streamOperator.getQueryId(), new ArrayList<StreamOperator>());
				map.get(streamOperator.getQueryId()).add(streamOperator);
			}
		}
		
		for(String queryId : map.keySet()){
			List<List<String>> sets = generateSets(queryId, map);
			Collections.sort(sets, new OperatorSetComparator());
			List<Integer> childIdsForQuery = childIdsForQuery(queryId);
			
			if(childIdsForQuery.contains(childId)){
				for(List<String> set : sets){	
					if(canBeSentTo(set, childId, resourceUsageDetails, queryId)){
						System.out.println("SENDING "+set+" FROM "+getName()+" TO "+CloudSim.getEntityName(childId));
						sendOperatorsToChild(queryId, set, childId);
						//System.out.println(CloudSim.clock()+" : "+CloudSim.getEntityName(childId));
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Returns the child fog devices concerned for the query queryId
	 * @param queryId
	 * @return
	 */
	protected List<Integer> childIdsForQuery(String queryId){
		List<Integer> childIdsForQuery = new ArrayList<Integer>();
		GeoCoverage geo = getStreamQueryMap().get(queryId).getGeoCoverage();
		for(Integer childId : getChildrenIds()){
			if(((FogDevice)CloudSim.getEntity(childId)).getGeoCoverage().covers(geo) || geo.covers(((FogDevice)CloudSim.getEntity(childId)).getGeoCoverage()))
				childIdsForQuery.add(childId);
		}
		return childIdsForQuery;
	}
	
	/**
	 * Finds out the sets of operators that can be sent to a child device together for a given query
	 * @param queryId the ID of the query whose operator sets have to be returned
	 * @param map map of query ID to operators of that query running on this device 
	 * @return
	 */
	protected List<List<String>> generateSets(String queryId, Map<String, List<StreamOperator>> map){
		StreamQuery query = streamQueryMap.get(queryId);
		List<List<String>> sets = new ArrayList<List<String>>();
			
		for(StreamOperator operator : map.get(queryId)){
			List<String> set = new ArrayList<String>();
			set.add(operator.getName());
				
			for(StreamOperator operator1 : map.get(queryId)){
				if(query.isAncestorOperator(operator.getName(), operator1.getName()))
					set.add(operator1.getName());
			}
			sets.add(set);
		}
		return sets;
	}
	
	protected void sendOperatorsToChild(String queryId, List<String> operators, int deviceId) {
		FogDevice fogDevice = (FogDevice)CloudSim.getEntity(deviceId);

		sendNow(deviceId, FogEvents.QUERY_SUBMIT, getStreamQueryMap().get(queryId));
		
		for(String operator : operators){
			StreamOperator streamOperator_original = getStreamQueryMap().get(queryId).getOperatorByName(operator);
			StreamOperator streamOperator = new StreamOperator(streamOperator_original);
			fogDevice.getVmAllocationPolicy().allocateHostForVm(streamOperator);
			sendNow(deviceId, FogEvents.LAUNCH_OPERATOR, streamOperator);
			if(!getChildToOperatorsMap().containsKey(deviceId)){
				getChildToOperatorsMap().put(deviceId, new ArrayList<String>());
			}
			getChildToOperatorsMap().get(deviceId).add(operator);
		}
	}

	/**
	 * Sends the resource usage details to it's parent.
	 * @param ev
	 */
	private void sendResourceUsage() {
		sendNow(getParentId(), FogEvents.RESOURCE_USAGE, new ResourceUsageDetails(getHost().getTotalMips(), 
				getUplinkBandwidth(), calculateCpuLoad(), getTrafficIntensity()));
	}

	protected void performAdativeReplacement(SimEvent ev){
		if(!ADAPTIVE_REPLACEMENT)
			return;
		for(int childId : getChildrenIds()){
			sendNow(childId, FogEvents.GET_RESOURCE_USAGE);
		}
		send(getId(), ADAPTIVE_INTERVAL, FogEvents.ADAPTIVE_OPERATOR_REPLACEMENT);
	}
	
	private void updateActiveQueries(SimEvent ev) {
		StreamQuery streamQuery = (StreamQuery)ev.getData();
		getActiveQueries().add(streamQuery.getQueryId());
	}

	/**
	 * Calculates utilization of each operator.
	 * @param operatorName
	 * @return
	 */
	public double getUtilizationOfOperator(String operatorName){
		double total = 0;
		for(Double d : utilization.get(operatorName)){
			total += d;
		}
		return total/utilization.get(operatorName).size();
	}
	
	public String getOperatorName(int vmId){
		for(Vm vm : this.getHost().getVmList()){
			if(vm.getId() == vmId)
				return ((StreamOperator)vm).getName();
		}
		return null;
	}
	
	public boolean checkIfDeviceOverloaded(){
		double load = calculateCpuLoad();
		//OLA1 System.out.println(getName()+"\tLOAD = "+load);
		if(load < 0.95)
			return false;
		else
			return true;
	}
	
	protected void checkCloudletCompletion() {
		
		boolean cloudletCompleted = false;
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				//System.out.println(getName()+"Remaining tuples on operator "+((StreamOperator)vm).getName()+" = "+vm.getCloudletScheduler().runningCloudlets());
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						
						cloudletCompleted = true;
						//System.out.println("+++++"+getName()+":"+cl.getVmId());
						//System.out.println(CloudSim.clock()+ " : Tuple ID "+((Tuple)cl).getActualTupleId()+" finished on operator "+getOperatorName(cl.getVmId()));
						//System.out.println(getName()+"Remaining tuples on operator "+getOperatorName(cl.getVmId())+" = "+vm.getCloudletScheduler().runningCloudlets());
						Tuple tuple = (Tuple)cl;
						StreamQuery streamQuery = getStreamQueryMap().get(tuple.getQueryId());
						StreamOperator streamOperator = streamQuery.getOperatorByName(tuple.getDestOperatorId());
								
						
						//System.out.println(streamOperator.getName()+" : "+(long) (streamOperator.getTupleFileLength()* streamQuery.getSelectivity(streamOperator.getName(), tuple.getSrcOperatorId())));
						if(Math.random() <  streamQuery.getSelectivity(streamOperator.getName(), tuple.getSrcOperatorId())){
							Tuple result = new Tuple(tuple.getQueryId(), FogUtils.generateTupleId(),
									(long) (streamOperator.getTupleLength()),
									tuple.getNumberOfPes(),
									(long) (streamOperator.getTupleFileLength()),
									tuple.getCloudletOutputSize(),
									tuple.getUtilizationModelCpu(),
									tuple.getUtilizationModelRam(),
									tuple.getUtilizationModelBw()
									);
							
							result.setSensorType(tuple.getSensorType());
							result.setActualTupleId(tuple.getActualTupleId());
							result.setUserId(tuple.getUserId());
							result.setQueryId(tuple.getQueryId());
							result.setEmitTime(tuple.getEmitTime());
							String destoperator = null;
							
							if(getStreamQueryMap().get(tuple.getQueryId()).getNextOperator(tuple.getDestOperatorId())!=null)
								destoperator = getStreamQueryMap().get(tuple.getQueryId()).getNextOperator(tuple.getDestOperatorId()).getName();
							result.setDestOperatorId(destoperator);
							result.setSrcOperatorId(tuple.getDestOperatorId());
							//System.out.println(getName()+" sending "+tuple.getDestOperatorId()+" ---> "+destoperator);
							sendToSelf(result);	
						}
						
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
		if(cloudletCompleted)
			updateAllocatedMips(null);
	}
	
	private void updateAllocatedMips(String incomingOperator){
		getHost().getVmScheduler().deallocatePesForAllVms();
		for(final Vm vm : getHost().getVmList()){
			if(vm.getCloudletScheduler().runningCloudlets() > 0 || ((StreamOperator)vm).getName().equals(incomingOperator)){
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					private static final long serialVersionUID = 1L;
				{add((double) getHost().getTotalMips());}});
			}else{
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					private static final long serialVersionUID = 1L;
				{add(0.0);}});
			}
		}
		for(final Vm vm : getHost().getVmList()){
			StreamOperator operator = (StreamOperator)vm;
			operator.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(operator).getVmScheduler()
					.getAllocatedMipsForVm(operator));
			//System.out.println("MIPS of "+((StreamOperator)vm).getName()+" = "+getHost().getVmScheduler().getTotalAllocatedMipsForVm(vm));
		}
		//displayAllocatedMipsForOperators();
	}
	
	private void updateUtils(){
		for(Vm vm : getHost().getVmList()){
			StreamOperator operator = (StreamOperator)vm;
			if(utilization.get(operator.getName()).size() > RESOURCE_USAGE_VECTOR_SIZE){
				utilization.get(operator.getName()).remove();
			}
			utilization.get(operator.getName()).add(operator.getTotalUtilizationOfCpu(CloudSim.clock()));
			
			//System.out.println(CloudSim.clock()+":\t"+operator.getName()+"\tCLOUDLETS\t"+getUtilizationOfOperator(operator.getName()));
			
			//System.out.println(CloudSim.clock()+":\t"+operator.getName()+"\t\t"+operator.getCurrentRequestedMips());
			
			
			/*System.out.println(getName()+"\t"+operator.getName()+"\tINPUT RATE\t"+getInputTupleRate(operator.getName()));
			System.out.println(getName()+"\t"+operator.getName()+"\tINTER RATE\t"+getIntermediateTupleRate(operator.getName()));
			*/
			
			//OLA2 System.out.println(getName()+"\tOUTPUT RATE\t"+getOutputTupleRate());
		}
		//OLA1System.out.println(checkIfDeviceOverloaded());
		//OLA2 displayInputTupleRateByChildOperator();
	}
	
	public double getTrafficIntensity(){
		double trafficIntensity = 0;
		for(String operatorName : outputTupleTimesByOperator.keySet()){
			if(outputTupleLengthsByOperator.get(operatorName) != null){
				trafficIntensity += outputTupleLengthsByOperator.get(operatorName)*getOutputTupleRate(operatorName);
			}
			
		}
		trafficIntensity /= getUplinkBandwidth();
		return trafficIntensity;
	}
	
	private void updateInputRate(){
		for(Pair<String, Integer> pair : inputTuples.keySet()){
			inputRateByChildId.put(pair, inputTuples.get(pair)/RESOURCE_USAGE_COLLECTION_INTERVAL);
			inputTuples.put(pair, 0);
			//System.out.println(getName()+"\t"+CloudSim.getEntityName(pair.getSecond())+"+"+pair.getFirst()+" --> "+inputRateByChildId.get(pair));
		}
	}
	
	private void updateInputRateByChildOperator(){
		for(Pair<String, String> pair : inputTuplesByChildOperator.keySet()){
			inputRateByChildOperator.put(pair, inputTuplesByChildOperator.get(pair)/RESOURCE_USAGE_COLLECTION_INTERVAL);
			inputTuplesByChildOperator.put(pair, 0);
		}
	}
	
	private void updateResourceUsage(){
		/*System.out.println("CPU LOAD ON "+getName()+" = "+calculateCpuLoad());
		System.out.println(getInputTupleRate("spout"));
		System.out.println(getInputTupleRate("bolt"));
		*/
		updateUtils();
		updateInputRate();
		updateInputRateByChildOperator();
		send(getId(), RESOURCE_USAGE_COLLECTION_INTERVAL, FogEvents.UPDATE_RESOURCE_USAGE);
		
		/*if(getName().equals("cloud")){
			System.out.println("===================================================");
			for(Integer time : cloudTrafficMap.keySet())
				System.out.println("TRAFFIC\t" + time + "\t" + cloudTrafficMap.get(time));
			System.out.println("===================================================");	
		}*/
	}
	
	private double getIntermediateTupleRate(String operatorName){
		Queue<Double> tupleInterTimes = intermediateTupleTimesByOperator.get(operatorName);
		double lastTime = CloudSim.clock() - INPUT_RATE_TIME;
		for(;;){
			if(tupleInterTimes.size() == 0)
				return 0;
			Double time = tupleInterTimes.peek();
			
			if(time < lastTime)
				tupleInterTimes.remove();
			else{
				intermediateTupleTimesByOperator.put(operatorName, tupleInterTimes);
				return (tupleInterTimes.size()/INPUT_RATE_TIME);
			}
		}
	}

	/**
	 * Returns the input rate of tuples from operator operatorName running on Fog device with ID childId
	 * @param childId
	 * @param operatorName
	 */
	private double getInputTupleRate(int childId, String operatorName){
		Queue<Double> tupleInputTimes = inputTupleTimes.get(new Pair<String, Integer>(operatorName, childId));
		double lastTime = CloudSim.clock() - INPUT_RATE_TIME;
		for(;;){
			if(tupleInputTimes.size() == 0)
				return 0;
			Double time = tupleInputTimes.peek();
			
			if(time < lastTime)
				tupleInputTimes.remove();
			else{
				inputTupleTimes.put(new Pair<String, Integer>(operatorName, childId), tupleInputTimes);
				return (tupleInputTimes.size()/INPUT_RATE_TIME);
			}
		}
	}
	
	/**
	 * Returns the input rate of tuples from operator operatorName running on Fog device with ID childId
	 * @param childId
	 * @param operatorName
	 */
	public double getInputTupleRateByChildOperator(String childOpId, String operatorName){
		Queue<Double> tupleInputTimes = inputTupleTimesByChildOperator.get(new Pair<String, String>(operatorName, childOpId));
		double lastTime = CloudSim.clock() - INPUT_RATE_TIME;
		for(;;){
			if(tupleInputTimes.size() == 0)
				return 0;
			Double time = tupleInputTimes.peek();
			
			if(time < lastTime)
				tupleInputTimes.remove();
			else{
				inputTupleTimesByChildOperator.put(new Pair<String, String>(operatorName, childOpId), tupleInputTimes);
				return (tupleInputTimes.size()/INPUT_RATE_TIME);
			}
		}
	}
	
	public double getInputRateByChildOperatorAndNode(String childOperator, int childNodeId){
		Queue<Double> tupleInputTimes = inputTupleTimesByChildOperatorAndNode.get(new Pair<String, Integer>(childOperator, childNodeId));
		if(tupleInputTimes==null)
			return 0;
		double lastTime = CloudSim.clock() - INPUT_RATE_TIME;
		for(;;){
			if(tupleInputTimes.size() == 0)
				return 0;
			Double time = tupleInputTimes.peek();
			
			if(time < lastTime)
				tupleInputTimes.remove();
			else{
				inputTupleTimesByChildOperatorAndNode.put(new Pair<String, Integer>(childOperator, childNodeId), tupleInputTimes);
				return (tupleInputTimes.size()/INPUT_RATE_TIME);
			}
		}
	}
	
	
	/**
	 * Returns the input rate for operator operatorName
	 * @param operatorName
	 * @return
	 */
	private double getInputTupleRate(String operatorName){
		double totalInputRate = 0;
		for(Pair<String, Integer> key : inputTupleTimes.keySet()){
			if(operatorName.equals(key.getFirst())){
				totalInputRate += getInputTupleRate(key.getSecond(), operatorName);
			}
		}
		return totalInputRate;
	}
	
	/**
	 * Displays the input rate for operator operatorName by Child operator ID
	 * @param operatorName
	 * @return
	 */
	private void displayInputTupleRateByChildOperator(){
		for(Pair<String, String> key : inputTupleTimesByChildOperator.keySet()){
			System.out.println(getName()+" : INPUT RATE BY CHILD OP ID : "+key.getFirst()+"\t"+key.getSecond()+"\t\t"+getInputTupleRateByChildOperator(key.getSecond(), key.getFirst()));
		}
	}
	
	private void updateInputTupleCount(int srcId, String operatorName){
		Pair<String, Integer> pair = new Pair<String, Integer>(operatorName, srcId);
		if(inputTuples.containsKey(pair)){
			inputTuples.put(pair, inputTuples.get(pair)+1);
			inputTupleTimes.get(pair).add(CloudSim.clock());
		}else{
			inputTuples.put(pair, 1);
			inputTupleTimes.put(pair, new LinkedList<Double>());
		}
	}
	
	private void updateInputTupleCountByChildOperator(String operatorName, String childOperatorId){
		Pair<String, String> pair = new Pair<String, String>(operatorName, childOperatorId);
		if(inputTuplesByChildOperator.containsKey(pair)){
			inputTuplesByChildOperator.put(pair, inputTuplesByChildOperator.get(pair)+1);
			inputTupleTimesByChildOperator.get(pair).add(CloudSim.clock());
		}else{
			inputTuplesByChildOperator.put(pair, 1);
			inputTupleTimesByChildOperator.put(pair, new LinkedList<Double>());
		}
	}
	
	private void updateInputTupleCountByChildOperatorAndNode(String childOperator, Integer childNodeId){
		Pair<String, Integer> pair = new Pair<String, Integer>(childOperator, childNodeId);
		if(inputTupleTimesByChildOperatorAndNode.containsKey(pair)){
			inputTupleTimesByChildOperatorAndNode.get(pair).add(CloudSim.clock());
		}else{
			inputTupleTimesByChildOperatorAndNode.put(pair, new LinkedList<Double>());
		}
	}
	
	private void processQuerySubmit(SimEvent ev) {
		StreamQuery query = (StreamQuery)ev.getData();
		streamQueryMap.put(query.getQueryId(), query);
	}

	private void displayAllocatedMipsForOperators(){
		System.out.println("-----------------------------------------");
		for(Vm vm : getHost().getVmList()){
			StreamOperator operator = (StreamOperator)vm;
			System.out.println("Allocated MIPS for "+operator.getName()+" : "+getHost().getVmScheduler().getTotalAllocatedMipsForVm(operator));
		}
		System.out.println("-----------------------------------------");
	}
	
	private void addChild(int childId){
		if(CloudSim.getEntityName(childId).contains("sensor"))
			return;
		if(!getChildrenIds().contains(childId) && childId != getId())
			getChildrenIds().add(childId);
		if(!getChildToOperatorsMap().containsKey(childId))
			getChildToOperatorsMap().put(childId, new ArrayList<String>());
	}
	
	private void updateCloudTraffic(){
		int time = (int)CloudSim.clock()/1000;
		if(!cloudTrafficMap.containsKey(time))
			cloudTrafficMap.put(time, 0);
		cloudTrafficMap.put(time, cloudTrafficMap.get(time)+1);
	}
	
	private void processTupleArrival(SimEvent ev){
		if(getName().equals("cloud")){
			updateCloudTraffic();
		}
		Tuple tuple = (Tuple)ev.getData();
		send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);
		addChild(ev.getSource());
		if(FogUtils.queryIdToGeoCoverageMap.containsKey(tuple.getQueryId())){
			GeoCoverage geo = FogUtils.queryIdToGeoCoverageMap.get(tuple.getQueryId());
			if(!(getGeoCoverage().covers(geo) || geo.covers(geoCoverage)))
				return;
		}
		
		if(getHost().getVmList().size() > 0){
			final StreamOperator operator = (StreamOperator)getHost().getVmList().get(0);
			if(CloudSim.clock() > 100){
				getHost().getVmScheduler().deallocatePesForVm(operator);
				getHost().getVmScheduler().allocatePesForVm(operator, new ArrayList<Double>(){
					private static final long serialVersionUID = 1L;
				{add((double) getHost().getTotalMips());}});
			}
		}
		
		if(Math.random() < missRate)
			return;
		
		if(getName().equals("cloud") && tuple.getDestOperatorId()==null){
			sendNow(getControllerId(), FogEvents.TUPLE_FINISHED, new TupleFinishDetails(tuple.getQueryId(), tuple.getActualTupleId(), tuple.getEmitTime(), CloudSim.clock(), tuple.getSensorType()));
		}
		
		if(queryToOperatorsMap.containsKey(tuple.getQueryId())){
			if(queryToOperatorsMap.get(tuple.getQueryId()).contains(tuple.getDestOperatorId())){
				int vmId = -1;
				for(Vm vm : getHost().getVmList()){
					if(((StreamOperator)vm).getName().equals(tuple.getDestOperatorId()))
						vmId = vm.getId();
				}
				//int vmId = streamQueryMap.get(tuple.getQueryId()).getOperatorByName(tuple.getDestOperatorId()).getId();
				
				if(vmId < 0){
					return;
				}
				
				tuple.setVmId(vmId);
				updateInputTupleCount(ev.getSource(), tuple.getDestOperatorId());
				updateInputTupleCountByChildOperator(tuple.getDestOperatorId(), tuple.getSrcOperatorId());
				//System.out.println("Tuple from operator "+tuple.getSrcOperatorId()+" from node "+ CloudSim.getEntityName(ev.getSource()));
				updateInputTupleCountByChildOperatorAndNode(tuple.getSrcOperatorId(), ev.getSource());
				updateTupleLengths(tuple.getSrcOperatorId(), tuple.getDestOperatorId(), tuple.getCloudletLength());
				executeTuple(ev, tuple.getDestOperatorId());
			}else if(tuple.getDestOperatorId()!=null){
				sendUp(tuple);
			}else{
				sendUp(tuple);
			}
		}else{
			sendUp(tuple);
		}
	}
	
	public double calculateCpuLoad(){
		double load = 0;
		for(Pair<String, String> pair : inputRateByChildOperator.keySet()){
			load += getInputTupleRateByChildOperator(pair.getSecond(), pair.getFirst())*tupleLengthByChildOperator.get(pair);
		}
		load /= getHost().getTotalMips();
		return load;
	}
	
	private void updateTupleLengths(String srcId, String destOperatorId, long length) {
		Pair<String, String> pair = new Pair<String, String>(destOperatorId, srcId);
		if(tupleLengthByChildOperator.containsKey(pair)){
			double previousTotalLength = tupleLengthByChildOperator.get(pair)*tupleCountsByChildOperator.get(pair);
			tupleLengthByChildOperator.put(pair, (previousTotalLength+length)/(tupleCountsByChildOperator.get(pair)+1));
			tupleCountsByChildOperator.put(pair, tupleCountsByChildOperator.get(pair)+1);
		}else{
			tupleLengthByChildOperator.put(pair, (double)length);
			tupleCountsByChildOperator.put(pair, 1.0);
		}
		
	}

	private void processSensorJoining(SimEvent ev){
		//TODO Process sensor joining
		send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);
	}
	
	private void executeTuple(SimEvent ev, String operatorId){
		updateAllocatedMips(operatorId);
		processCloudletSubmit(ev, false);
		updateAllocatedMips(operatorId);
	}
	
	private void processOperatorArrival(SimEvent ev){
		StreamOperator operator = (StreamOperator)ev.getData();
		String queryId = operator.getQueryId();
		if(!queryToOperatorsMap.containsKey(queryId)){
			queryToOperatorsMap.put(queryId, new ArrayList<String>());
		}
		queryToOperatorsMap.get(queryId).add(operator.getName());
		getVmList().add(operator);
		//getHost().getVmList().add(operator);
		if (operator.isBeingInstantiated()) {
			operator.setBeingInstantiated(false);
		}
		utilization.put(operator.getName(), new LinkedList<Double>());
		operator.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(operator).getVmScheduler()
				.getAllocatedMipsForVm(operator));
		intermediateTupleTimesByOperator.put(operator.getName(), new LinkedList<Double>());
		outputTupleTimesByOperator.put(operator.getName(), new LinkedList<Double>());
	}
	
	private void processOperatorRelease(SimEvent ev){
		this.processVmMigrate(ev, false);
	}
	
	public boolean isAncestorOf(FogDevice dev){
		if(this.geoCoverage.covers(dev.getGeoCoverage()))
			return true;
		return false;
	}
	
	private void updateTupleQueue(){
		if(!getOutgoingTupleQueue().isEmpty()){
			Tuple tuple = getOutgoingTupleQueue().poll();
			sendUpFreeLink(tuple);
		}else{
			setOutputLinkBusy(false);
		}
	}
	
	protected void sendUpFreeLink(Tuple tuple){
		//System.out.println(CloudSim.clock()+"\tSending tuple ID "+tuple.getActualTupleId()+" from "+getName());
		//System.out.println(CloudSim.clock()+" : Tuple ID " + tuple.getActualTupleId()+" being sent up FREE LINK.");
		double networkDelay = tuple.getCloudletFileSize()/getUplinkBandwidth();
		setOutputLinkBusy(true);
		send(getId(), networkDelay, FogEvents.UPDATE_TUPLE_QUEUE);
		send(parentId, networkDelay+latency, FogEvents.TUPLE_ARRIVAL, tuple);
	}
	
	public double getOutputTupleRate(String operatorName){
		
		if(!outputTupleTimesByOperator.containsKey(operatorName))
			return 0;
		//System.out.println(CloudSim.clock()+"\t"+operatorName+"\t"+outputTupleTimesByOperator.get(operatorName));
		Queue<Double> tupleOutputTimes = outputTupleTimesByOperator.get(operatorName);
		double lastTime = CloudSim.clock() - INPUT_RATE_TIME;
		for(;;){
			if(tupleOutputTimes.size() == 0)
				return 0;
			Double time = tupleOutputTimes.peek();
			
			if(time < lastTime)
				tupleOutputTimes.remove();
			else{
				outputTupleTimesByOperator.put(operatorName, tupleOutputTimes);
				return (tupleOutputTimes.size()/INPUT_RATE_TIME);
			}
		}
	}
	
	protected void sendUp(Tuple tuple){
		//System.out.println(CloudSim.clock()+" : Tuple ID " + tuple.getActualTupleId()+" being sent up.");
		if(getActiveQueries().contains(tuple.getQueryId())){
			outputTupleTimes.add(CloudSim.clock());
			if(outputTupleTimesByOperator.containsKey(tuple.getSrcOperatorId()))
				outputTupleTimesByOperator.get(tuple.getSrcOperatorId()).add(CloudSim.clock());
			else{
				outputTupleTimesByOperator.put(tuple.getSrcOperatorId(), new LinkedList<Double>());
				outputTupleTimesByOperator.get(tuple.getSrcOperatorId()).add(CloudSim.clock());
			}
			
			outputTupleLengthsByOperator.put(tuple.getSrcOperatorId(), (double) tuple.getCloudletFileSize());
		}
		/*if(intermediateTupleTimesByOperator.get(tuple.getSrcOperatorId())==null){
			intermediateTupleTimesByOperator.put(tuple.getSrcOperatorId(), new LinkedList<Double>());
		}
		intermediateTupleTimesByOperator.get(tuple.getSrcOperatorId()).add(CloudSim.clock());
		*/
		if(parentId > 0){
			if(!isOutputLinkBusy()){
				sendUpFreeLink(tuple);
			}else{
				outgoingTupleQueue.add(tuple);
			}
		}
	}
	private void sendToSelf(Tuple tuple){
		send(getId(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ARRIVAL, tuple);
	}
	public Host getHost(){
		return getHostList().get(0);
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public List<Integer> getChildrenIds() {
		return childrenIds;
	}
	public void setChildrenIds(List<Integer> childrenIds) {
		this.childrenIds = childrenIds;
	}
	public Map<String, StreamQuery> getStreamQueryMap() {
		return streamQueryMap;
	}
	public void setStreamQueryMap(Map<String, StreamQuery> streamQueryMap) {
		this.streamQueryMap = streamQueryMap;
	}
	public GeoCoverage getGeoCoverage() {
		return geoCoverage;
	}
	public void setGeoCoverage(GeoCoverage geoCoverage) {
		this.geoCoverage = geoCoverage;
	}
	public double getMissRate() {
		return missRate;
	}
	public void setMissRate(double missRate) {
		this.missRate = missRate;
	}	public double getUplinkBandwidth() {
		return uplinkBandwidth;
	}
	public void setUplinkBandwidth(double uplinkBandwidth) {
		this.uplinkBandwidth = uplinkBandwidth;
	}
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
	public Queue<Tuple> getOutgoingTupleQueue() {
		return outgoingTupleQueue;
	}
	public void setOutgoingTupleQueue(Queue<Tuple> outgoingTupleQueue) {
		this.outgoingTupleQueue = outgoingTupleQueue;
	}
	public boolean isOutputLinkBusy() {
		return isOutputLinkBusy;
	}
	public void setOutputLinkBusy(boolean isOutputLinkBusy) {
		this.isOutputLinkBusy = isOutputLinkBusy;
	}
	public int getControllerId() {
		return controllerId;
	}
	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}
	public List<String> getActiveQueries() {
		return activeQueries;
	}
	public void setActiveQueries(List<String> activeQueries) {
		this.activeQueries = activeQueries;
	}
	public Map<String, Queue<Double>> getOutputTupleTimesByOperator() {
		return outputTupleTimesByOperator;
	}
	public void setOutputTupleTimesByOperator(
			Map<String, Queue<Double>> outputTupleTimesByOperator) {
		this.outputTupleTimesByOperator = outputTupleTimesByOperator;
	}
	public Map<String, Double> getOutputTupleLengthsByOperator() {
		return outputTupleLengthsByOperator;
	}
	public void setOutputTupleLengthsByOperator(
			Map<String, Double> outputTupleLengthsByOperator) {
		this.outputTupleLengthsByOperator = outputTupleLengthsByOperator;
	}
	public Map<Integer, List<String>> getChildToOperatorsMap() {
		return childToOperatorsMap;
	}
	public void setChildToOperatorsMap(Map<Integer, List<String>> childToOperatorsMap) {
		this.childToOperatorsMap = childToOperatorsMap;
	}

	public Map<Pair<String, Integer>, Queue<Double>> getInputTupleTimesByChildOperatorAndNode() {
		return inputTupleTimesByChildOperatorAndNode;
	}

	public void setInputTupleTimesByChildOperatorAndNode(
			Map<Pair<String, Integer>, Queue<Double>> inputTupleTimesByChildOperatorAndNode) {
		this.inputTupleTimesByChildOperatorAndNode = inputTupleTimesByChildOperatorAndNode;
	}	
}