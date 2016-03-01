package org.fog.dsp.bruteforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.dsp.OperatorPlacement;
import org.fog.dsp.StreamQuery;
import org.fog.entities.FogDevice;
import org.fog.entities.StreamOperator;
import org.fog.utils.FogUtils;
import org.fog.utils.OperatorEdge;

public class OperatorPlacementBruteForce extends OperatorPlacement{
	
	private double alpha = 1;
	private double beta = 10;
	protected List<Map<String, Integer>> placements;
	protected Map<String, Double> operatorOutputRate;

	public OperatorPlacementBruteForce(List<FogDevice> fogDevices, StreamQuery streamQuery){
		super(fogDevices, streamQuery);
		setOperatorOutputRate(new HashMap<String, Double>());
		setPlacements(new ArrayList<Map<String, Integer>>());
		
		calculateOutputRates();
		
		System.out.println("OUTPUT RATES");
		System.out.println(operatorOutputRate);
		
		List<String> front = new ArrayList<String>();
		
		for(String leafOperator : getStreamQuery().getLeaves()){
			front.add(leafOperator);
		}
		
		Map<String, Integer> noPlacementMap = new HashMap<String, Integer>();
		
		//List<Map<String, Integer>> tentativePlacements = new ArrayList<Map<String, Integer>>());
		List<Map<String, Integer>> tentativePlacements = calculatePlacements(noPlacementMap, front);
		for(Map<String, Integer> placement : tentativePlacements){
			if(satisfiesNetworkIntensity(placement))
				getPlacements().add(placement);
		}

		displayPlacements();
		
		System.out.println(getStreamQuery().getEndToEndPaths());
		
		//THIS CAUSES NULL POINTER EXCEPTION BECAUSE WE HAVE NOT ALLOCATED THE OPERATOR TO THE DEVICE YET
		
		double minCost = FogUtils.MAX;
		Map<String, Integer> bestPlacement = null;
		for(Map<String, Integer> placement : getPlacements()){
			System.out.println("\n\n\n=========================================================");
			double cost = calculateCostOfPlacement(placement);
			System.out.println("Cost of placement "+placement+" = "+cost);
			if(cost < minCost){
				minCost = cost;
				bestPlacement = placement;
			}
		}
		
		for(String operator : bestPlacement.keySet()){
			StreamOperator streamOperator = getStreamQuery().getOperatorByName(operator);
			FogDevice fogDevice = (FogDevice)CloudSim.getEntity(bestPlacement.get(operator));
			if(!fogDevice.getVmAllocationPolicy().allocateHostForVm(streamOperator)){
				System.out.println("IMPOSSIBLE");
				System.exit(0);
			}
		}
		setOperatorToDeviceMap(bestPlacement);
		System.out.println("BEST PLACEMENT : "+bestPlacement);
		//System.exit(0);
	}
	
	protected double getCpuLoadOnDevice(int deviceId, Map<String, Integer> placement){
		FogDevice fogDevice = (FogDevice)CloudSim.getEntity(deviceId);
		
		double runningCpuLoad = fogDevice.calculateCpuLoad()*fogDevice.getHost().getTotalMips();

		List<String> operatorsPlacedHere = new ArrayList<String>();
		for(String operator : placement.keySet()){
			if(placement.get(operator) == deviceId)
				operatorsPlacedHere.add(operator);
		}
		List<String> children;
		double cpuLoadByPlacement = 0;
		for(String operator : operatorsPlacedHere){
			children = getStreamQuery().getAllChildren(operator);
			for(String child : children){
				if(!getStreamQuery().isSensor(child)){
					cpuLoadByPlacement += calculateOutputRate(child) * getStreamQuery().getOperatorByName(child).getTupleLength();
				}
				else{
					cpuLoadByPlacement += getStreamQuery().getOperatorByName(operator).getSensorRate() 
							* getStreamQuery().getTupleCpuLengthOfSensor(FogUtils.getSensorTypeFromSensorName(child));
				}
			}
		}
		return (runningCpuLoad+cpuLoadByPlacement);
	}
	
	protected double calculateCpuCostOfPath(List<String> path, Map<String, Integer> placement){
		double totalCpuCost = 0;
		int i=-1;
		for(String operator : path){
			++i;
			int device = placement.get(operator);
			double loadOnDevice = getCpuLoadOnDevice(device, placement);
			double tupleLength;
			double inputRateForOperatorOnPath, temp = loadOnDevice;
			if(getStreamQuery().isLeafOperator(operator)){
				inputRateForOperatorOnPath = getStreamQuery().getOperatorByName(operator).getSensorRate();
				tupleLength = getStreamQuery().getTupleCpuLengthOfSensor(getStreamQuery().getOperatorByName(operator).getSensorName());
				//temp += inputRateForOperatorOnPath*getStreamQuery().getTupleCpuLengthOfSensor(getStreamQuery().getOperatorByName(operator).getSensorName());
				temp -= inputRateForOperatorOnPath*tupleLength;
			}else{
				inputRateForOperatorOnPath = calculateOutputRate(path.get(i-1));
				tupleLength = getStreamQuery().getOperatorByName(path.get(i-1)).getTupleLength();
				//temp +=  inputRateForOperatorOnPath * getStreamQuery().getOperatorByName(path.get(i-1)).getTupleLength();
				temp -=  inputRateForOperatorOnPath * tupleLength;
			}
			//totalCpuCost += temp/(2*inputRateForOperatorOnPath*((FogDevice)CloudSim.getEntity(device)).getHost().getTotalMips());
			
			// FOR CALCULATION OF CPU COST, WE TAKE INTO ACCOUNT THE CPU LOAD ON A DEVICE BEFORE THE OPERATOR WAS LAUNCHED
			totalCpuCost += alpha*tupleLength/((FogDevice)CloudSim.getEntity(device)).getHost().getTotalMips() + beta*temp;
		}
		return totalCpuCost;
	}
	
	protected double getNwLoadOnDevice(int deviceId, Map<String, Integer> placement){
		FogDevice fogDevice = (FogDevice)CloudSim.getEntity(deviceId);
		
		double runningNwLoad = fogDevice.getTrafficIntensity()*fogDevice.getUplinkBandwidth();
		
		List<OperatorEdge> operatorEdges = new ArrayList<OperatorEdge>(getStreamQuery().getOperatorEdges());
		System.out.println("------------OPERATOR EDGES---------------");
		for(OperatorEdge operatorEdge : operatorEdges){
			System.out.println(operatorEdge.getSrc()+"--->"+operatorEdge.getDst());
		}
		String apex = null;
		for(StreamOperator op : getStreamQuery().getOperators()){
			if(getStreamQuery().getEdges().get(op.getName())==null){
				apex = op.getName();
				break;
			}
		}
		operatorEdges.add(new OperatorEdge(apex, null, 0));
		
		double otherLoad = 0;
		for(OperatorEdge operatorEdge : operatorEdges){
			if(operatorEdge.getDst() == null || isAncestor(fogDevice.getParentId(), placement.get(operatorEdge.getDst()))){
				if(operatorEdge.getSrc().contains("sensor")){
					otherLoad += getStreamQuery().getOperatorForSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())).getSensorRate() * 
							getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())); 
							System.out.println(getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())));
				}else if(isAncestor(placement.get(operatorEdge.getSrc()), deviceId)){
					otherLoad += calculateOutputRate(operatorEdge.getSrc())*getStreamQuery().getOperatorByName(operatorEdge.getSrc()).getTupleFileLength();
				}
			}
		}
		return (otherLoad+runningNwLoad);
	}
	
	protected double calculateNwCostOfPath(List<String> path, Map<String, Integer> placement){
		double totalNwCost = 0;
		int i=-1;
		System.out.println("Calculating NW cost of path "+path+" for placement "+placement);
		FogDevice device = getLowestCoveringFogDevice(getFogDevices(), getStreamQuery());
		String sensorType = getStreamQuery().getOperatorByName(path.get(0)).getSensorName();
		String sender = "sensor";
		while(!device.getName().equals("cloud")){
			//System.out.println("Now calculating cost on "+device.getName());
			/*double deviceNwCost = 0, temp = getNwLoadOnDevice(device.getId(), placement);
			
			for(String operator : path){
				if(placement.get(operator)==device.getId())
					sender = operator;
			}
			System.out.println("SENDER = "+sender);
			double outputRate;
			System.out.println("TEMP="+temp);
			if(sender.equals("sensor")){
				outputRate = getStreamQuery().getOperatorByName(path.get(0)).getSensorRate();
				temp += outputRate*getStreamQuery().getTupleNwLengthOfSensor(sensorType);
			}else{
				outputRate = calculateOutputRate(sender);
				temp += outputRate *getStreamQuery().getOperatorByName(sender).getTupleFileLength();
			}
			System.out.println("TEMP="+temp);
			System.out.println((2*outputRate*device.getUplinkBandwidth()));
			System.out.println("NW Cost on device "+device.getName()+" = "+(temp/(2*outputRate*device.getUplinkBandwidth())));
			
			totalNwCost += temp/(2*outputRate*device.getUplinkBandwidth());
			device = (FogDevice)CloudSim.getEntity(device.getParentId());*/
			
			double nwLoadOnDevice = getNwLoadOnDevice(device.getId(), placement);
			for(String operator : path){
				if(placement.get(operator)==device.getId())
					sender = operator;
			}
			//System.out.println("SENDER = "+sender);
			System.out.println("nwLoadOnDevice "+device.getName()+" = "+nwLoadOnDevice);
			double outputRate;
			double nwLength;
			if(sender.equals("sensor")){
				System.out.println("YO");
				nwLength = getStreamQuery().getTupleNwLengthOfSensor(sensorType);
				outputRate = getStreamQuery().getOperatorByName(path.get(0)).getSensorRate();
				nwLoadOnDevice -= outputRate*nwLength;
			}else{
				nwLength = getStreamQuery().getOperatorByName(sender).getTupleFileLength();
				outputRate = calculateOutputRate(sender);
				nwLoadOnDevice -= outputRate *nwLength;
			}
			System.out.println("nwLoadOnDevice "+device.getName()+" = "+nwLoadOnDevice);
			totalNwCost += alpha*nwLength/device.getUplinkBandwidth() + beta*nwLoadOnDevice;
			device = (FogDevice)CloudSim.getEntity(device.getParentId());

		}
		
		return totalNwCost;
	}
	
	protected double calculateCostOfPlacement(Map<String, Integer> placement){
		List<List<String>> paths = getStreamQuery().getEndToEndPaths();
		double maxCost = -1*FogUtils.MAX;
		for(List<String> path : paths){
			double cpuCost = calculateCpuCostOfPath(path, placement);
			double nwCost = calculateNwCostOfPath(path, placement);
			System.out.println("CPU Cost of path "+path+" = "+cpuCost);
			System.out.println("NW Cost of path "+path+" = "+nwCost);
			double cost =  cpuCost + nwCost; 
			if(cost > maxCost)
				maxCost = cost;
		}
		return maxCost;
	}
	
	
	protected List<String> getOperatorsOnDevice(int device, Map<String, Integer> allocation){
		List<String> operators = new ArrayList<String>();
		for(String op : allocation.keySet()){
			if(allocation.get(op)==device)
				operators.add(op);
		}
		return operators;
	}
	protected boolean satisfiesNetworkIntensity(Map<String, Integer> allocation){
		System.out.println("\n\nEntering satisfiesNetworkIntensity for placement : "+allocation);
		for(int deviceId : getFogDevicesForInitialPlacement()){
			if(CloudSim.getEntityName(deviceId).equals("cloud"))
				continue;
			FogDevice fogDevice = (FogDevice)CloudSim.getEntity(deviceId);
			double runningLoad = fogDevice.getTrafficIntensity()*fogDevice.getUplinkBandwidth();
			
			List<OperatorEdge> operatorEdges = new ArrayList<OperatorEdge>(getStreamQuery().getOperatorEdges());
			String apex = null;
			for(StreamOperator op : getStreamQuery().getOperators()){
				if(getStreamQuery().getEdges().get(op.getName())==null){
					apex = op.getName();
					break;
				}
			}
			operatorEdges.add(new OperatorEdge(apex, null, 0));
			//TODO
			double otherLoad = 0;
			for(OperatorEdge operatorEdge : operatorEdges){
				if(operatorEdge.getDst() == null || isAncestor(fogDevice.getParentId(), allocation.get(operatorEdge.getDst()))){
					if(operatorEdge.getSrc().contains("sensor")){
						System.out.println("SENSOR FOUND");
						otherLoad += getStreamQuery().getOperatorForSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())).getSensorRate() * 
								getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())); 
								//System.out.println(getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(operatorEdge.getSrc())));
					}else if(isAncestor(allocation.get(operatorEdge.getSrc()), deviceId)){
						otherLoad += calculateOutputRate(operatorEdge.getSrc())*getStreamQuery().getOperatorByName(operatorEdge.getSrc()).getTupleFileLength();
					}
				}
			}
			
			System.out.println("LOAD ON DEVICE "+CloudSim.getEntityName(deviceId)+" = "+(runningLoad + otherLoad));
			
			if((runningLoad + otherLoad)/fogDevice.getUplinkBandwidth() > 1)
				return false;
		}
		
		return true;
	}
	
	protected List<String> getPlacedFront(Map<String, Integer> currentAllocation){
		List<String> front = new ArrayList<String>();
		List<String> leafOperators = getStreamQuery().getLeaves();
		
		for(String leafOperator : leafOperators){
			if(currentAllocation.get(leafOperator) == null){
				// IF THE LEAF OPERATOR IS NOT PLACED, WE ADD THE SENSOR TO THE FRONT
				front.add("sensor-"+getStreamQuery().getOperatorByName(leafOperator).getSensorName()+"-");
			}else{
				String op = leafOperator;
				while(currentAllocation.get(getStreamQuery().getEdges().get(op)) != null){
					op = getStreamQuery().getEdges().get(op);
					if(op == null)
						break;
				}
				if(op != null)
					front.add(op);
			}				
		}
		return front;
		
	}
	
	protected void setBestPlacement(){
		double minCost = FogUtils.MAX;
		Map<String, Integer> optimalPlacement = null;
		for(Map<String, Integer> placement : getPlacements()){
			double costOfPlacement = calculateCostOfPlacement(placement);
			if(costOfPlacement < minCost){
				minCost = costOfPlacement;
				optimalPlacement = placement;
			}
		}
		setOperatorToDeviceMap(optimalPlacement);
	}
	
	/**
	 * Returns the list of devices present uplink of deviceId (INCLUDING IT)
	 * @param deviceId
	 * @return
	 */
	protected List<Integer> getAncestorDevices(int deviceId){
		List<Integer> ancestors = new ArrayList<Integer>();
		int id = deviceId;
		while(id > -1){
			ancestors.add(id);
			id = ((FogDevice)CloudSim.getEntity(id)).getParentId();
		}
		return ancestors;
	}
	
	protected boolean canBePlacedCpu(String operatorName, int device, Map<String, Integer> currentAllocationMap){
		
		FogDevice fogDevice = (FogDevice)CloudSim.getEntity(device);
		System.out.println("Entered canBePlacedCpu for operator "+operatorName+" on device "+fogDevice.getName());
		System.out.println("Current placement : "+currentAllocationMap);
		double runningCpuLoad = fogDevice.calculateCpuLoad()*fogDevice.getHost().getTotalMips();
		
		System.out.println("Running load = "+runningCpuLoad);
		
		double cpuLoadByOperator = 0;
		List<String> children = getStreamQuery().getAllChildren(operatorName);
		
		for(String child : children){
			if(!getStreamQuery().isSensor(child)){
				cpuLoadByOperator += calculateOutputRate(child) * getStreamQuery().getOperatorByName(child).getTupleLength();
			}
			else{
				System.out.println("Is Sensor : "+FogUtils.getSensorTypeFromSensorName(child));
				cpuLoadByOperator += getStreamQuery().getOperatorByName(operatorName).getSensorRate() * getStreamQuery().getTupleCpuLengthOfSensor(FogUtils.getSensorTypeFromSensorName(child));
				System.out.println("CPU length of sensor tuple = "+getStreamQuery().getTupleCpuLengthOfSensor(FogUtils.getSensorTypeFromSensorName(child)));
			}
		}
		System.out.println("Operator load = "+cpuLoadByOperator);
		
		double cpuLoadOther = 0;
		List<String> operatorsAlreadyPlacedHere = new ArrayList<String>();
		for(String operator : currentAllocationMap.keySet()){
			if(currentAllocationMap.get(operator) == device)
				operatorsAlreadyPlacedHere.add(operator);
		}
		for(String operator : operatorsAlreadyPlacedHere){
			children = getStreamQuery().getAllChildren(operator);
			for(String child : children){
				if(!getStreamQuery().isSensor(child)){
					cpuLoadOther += calculateOutputRate(child) * getStreamQuery().getOperatorByName(child).getTupleLength();
				}
				else{
					System.out.println("Is Sensor : "+FogUtils.getSensorTypeFromSensorName(child));
					cpuLoadOther += getStreamQuery().getOperatorByName(operator).getSensorRate() * getStreamQuery().getTupleCpuLengthOfSensor(FogUtils.getSensorTypeFromSensorName(child));
				}
			}
		}
		
		System.out.println("Other load = "+cpuLoadOther);

		System.out.println("+++"+(runningCpuLoad + cpuLoadOther + cpuLoadByOperator)/fogDevice.getHost().getTotalMips());
		
		if((runningCpuLoad + cpuLoadOther + cpuLoadByOperator)/fogDevice.getHost().getTotalMips() <= 1)
			return true;
		else
			return false;
	}
	
	protected double calculateOutputRate(String operator){
		if(getStreamQuery().isLeafOperator(operator)){
			double outputRate = getStreamQuery().getOperatorByName(operator).getSensorRate()*getStreamQuery().getSelectivity(operator, "sensor-"+getStreamQuery().getOperatorByName(operator).getSensorName()+"-");
			//System.out.println("Output rate of "+operator+" = "+outputRate);
			return outputRate;
		}
		double outputRate = 0;
		List<String> childOperators = getStreamQuery().getAllChildren(operator);
		for(String childOperator : childOperators){
			if(!operatorOutputRate.containsKey(childOperator)){
				return -1;
			}
			else{
				outputRate += getStreamQuery().getSelectivity(operator, childOperator)*operatorOutputRate.get(childOperator);
			}
		}
		
		return outputRate;
	}
	
	protected void calculateOutputRates(){
		while(true){
			for(StreamOperator operator : getStreamQuery().getOperators()){
				double outputRate = calculateOutputRate(operator.getName());
				System.out.println(outputRate);
				if(outputRate > 0){
					operatorOutputRate.put(operator.getName(), outputRate);
					
				}
			}
			
			boolean allNotDone = false;
			for(StreamOperator operator : getStreamQuery().getOperators()){
				if(!operatorOutputRate.containsKey(operator.getName()))
					allNotDone = true;
			}
			if(!allNotDone)
				break;
		}
	}
	
	protected boolean canBePlacedNetwork(String operator, int device, Map<String, Integer> currentAllocationMap){
		// GET THE TRAFFIC INTENSITY OF THE DEVICE AND CALCULATE WHETHER THE OPERATOR CAN BE CREATED HERE OR NOT
		System.out.println("Checking canbePlacedNetwork for operator "+operator+" on "+CloudSim.getEntityName(device));
		FogDevice fogDevice = (FogDevice)CloudSim.getEntity(device);
		StreamOperator streamOperator = getStreamQuery().getOperatorByName(operator);
		double runningLoad = fogDevice.getTrafficIntensity()*fogDevice.getUplinkBandwidth();
		
		double otherLoad = 0;
		List<String> placedFront = getPlacedFront(currentAllocationMap);
		
		for(String frontMember : placedFront){
			if(frontMember.contains("sensor")){
				if(!(getStreamQuery().isLeafOperator(operator) && streamOperator.getSensorName().equals(FogUtils.getSensorTypeFromSensorName(frontMember))))
					otherLoad += getStreamQuery().getOperatorForSensor(FogUtils.getSensorTypeFromSensorName(frontMember)).getSensorRate() * getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(frontMember));
			}else{
				if(!(getStreamQuery().getEdges().get(frontMember)!=null && getStreamQuery().getEdges().get(frontMember).equals(operator)))
					otherLoad += calculateOutputRate(frontMember)*getStreamQuery().getOperatorByName(frontMember).getTupleFileLength();
			}
		}
		
		double loadByOperator = calculateOutputRate(operator)*getStreamQuery().getOperatorByName(operator).getTupleFileLength();
		
		System.out.println("Running load : "+runningLoad);
		System.out.println("Operator Load : "+loadByOperator);
		System.out.println("Others Load : "+otherLoad);
		
		if((runningLoad + otherLoad + loadByOperator)/fogDevice.getUplinkBandwidth() > 1)
			return false;
		
		// now check for other devices
		List<Integer> otherDevices = getOtherDevicesToCheck(device, placedFront, currentAllocationMap);
		for(Integer otherDeviceId : otherDevices){
			FogDevice otherDevice = (FogDevice)CloudSim.getEntity(otherDeviceId);
			double load = otherDevice.getTrafficIntensity()*otherDevice.getUplinkBandwidth();
			for(String frontMember : placedFront){
				if(frontMember.contains("sensor")){
					load += getStreamQuery().getOperatorForSensor(FogUtils.getSensorTypeFromSensorName(frontMember)).getSensorRate() * getStreamQuery().getTupleNwLengthOfSensor(FogUtils.getSensorTypeFromSensorName(frontMember));
				}else{
					load += calculateOutputRate(frontMember)*getStreamQuery().getOperatorByName(frontMember).getTupleFileLength();
				}
			}
			if(load/otherDevice.getUplinkBandwidth() > 1)
				return false;
		}
		return true;
	}
	
	protected List<Integer> getOtherDevicesToCheck(int device, List<String> placedFront, Map<String, Integer> allocation){
		int highestFrontDevice = getLowestCoveringFogDevice(getFogDevices(), getStreamQuery()).getId();
		List<Integer> otherDevices = new ArrayList<Integer>();
		for(String placedOperator : placedFront){
			
			if((!placedOperator.contains("sensor")) && isAncestor(highestFrontDevice, allocation.get(placedOperator))){
				highestFrontDevice = allocation.get(placedOperator);
			}
		}
		if(highestFrontDevice == device)
			return otherDevices;
		
		int dev = ((FogDevice)CloudSim.getEntity(highestFrontDevice)).getParentId();
		
		while(dev != -1 && dev != device){
			otherDevices.add(dev);
			dev = ((FogDevice)CloudSim.getEntity(dev)).getParentId();
		}
		
		return otherDevices;
	}
	
	protected void displayPlacements(){
		for(Map<String, Integer> placement : getPlacements()){
			System.out.println("------------------------------------------------------------");
			for(String operator : placement.keySet()){
				System.out.println(operator+"\t--->\t"+CloudSim.getEntityName(placement.get(operator)));
			}
		}
		System.out.println("------------------------------------------------------------");
	}
	
	protected boolean isAncestor(int child, int ancestor){
		int id = child;
		while(id > -1){
			if(id == ancestor)
				return true;
			id = ((FogDevice)CloudSim.getEntity(id)).getParentId();
		}
		return false;
	}
	
	/**
	 * Returns the highest device on which any child of operator has been mapped in map 
	 */
	protected int getHighestDeviceAmongChildMappings(String operator, Map<String, Integer> map){
		if(getStreamQuery().isLeafOperator(operator))
			return getLowestCoveringFogDevice(getFogDevices(), getStreamQuery()).getId();
		List<String> children = getStreamQuery().getAllChildren(operator);
		int highestDevice = getLowestCoveringFogDevice(getFogDevices(), getStreamQuery()).getId();
		for(String child : children){
			int childPlacedAt = map.get(child);
			if(isAncestor(highestDevice, childPlacedAt))
				highestDevice = childPlacedAt;
		}
		return highestDevice;
	}
	
	protected List<String> updateFront(List<String> front, String operator, Map<String, Integer> placement){
		List<String> newFront = new ArrayList<String>();
		for(String op : front){
			if(!op.equals(operator))
				newFront.add(op);
		}
		String parentOperator = getStreamQuery().getParentOperator(operator);
		boolean allChildrenPlaced = true;
		if(parentOperator != null){
			for(String child : getStreamQuery().getAllChildren(parentOperator)){
				if(placement.get(child) == null)
					allChildrenPlaced = false;
			}
		}
		if(allChildrenPlaced && parentOperator!=null)
			newFront.add(parentOperator);
		return newFront;
	}
	
	protected List<Map<String, Integer>> calculatePlacements(final Map<String, Integer> currentPlacement, List<String> front){		
		if(front.size()==0)
			return new ArrayList<Map<String, Integer>>(){{add(currentPlacement);}};
					
		List<Map<String, Integer>> _placements = new ArrayList<Map<String, Integer>>();
		
		for(String operator : front){
			int highestChildPlacement = getHighestDeviceAmongChildMappings(operator, currentPlacement);
			
			for(Integer ancestorId : getAncestorDevices(highestChildPlacement)){
				
				//if(canBePlacedCpu(operator, ancestorId, currentPlacement) && canBePlacedNetwork(operator, ancestorId, currentPlacement)){
				if(canBePlacedCpu(operator, ancestorId, currentPlacement)){
					Map<String, Integer> newPlacement = new HashMap<String, Integer>();
					for(String op : currentPlacement.keySet())
						newPlacement.put(op, currentPlacement.get(op));
					newPlacement.put(operator, ancestorId);
					System.out.println("Placing "+operator+" on "+CloudSim.getEntityName(ancestorId));
					List<String> newFront = updateFront(front, operator, newPlacement);
					_placements.addAll(calculatePlacements(newPlacement, newFront));
				}
			}
			
		}
		return _placements;
	}

	public List<Map<String, Integer>> getPlacements() {
		return placements;
	}

	public void setPlacements(List<Map<String, Integer>> placements) {
		this.placements = placements;
	}

	public Map<String, Double> getOperatorOutputRate() {
		return operatorOutputRate;
	}

	public void setOperatorOutputRate(Map<String, Double> operatorOutputRate) {
		this.operatorOutputRate = operatorOutputRate;
	}
	
}
