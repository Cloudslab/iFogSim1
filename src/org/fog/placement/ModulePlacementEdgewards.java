package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;

public class ModulePlacementEdgewards extends ModulePlacement{
	
	protected ModuleMapping moduleMapping;
	protected List<Sensor> sensors;
	protected List<Actuator> actuators;
	protected Map<Integer, Double> currentCpuLoad;
	
	/**
	 * Stores the current mapping of application modules to fog devices 
	 */
	protected Map<Integer, List<String>> currentModuleMap;
	protected Map<Integer, Map<String, Double>> currentModuleLoadMap;
	protected Map<Integer, Map<String, Integer>> currentModuleInstanceNum;
	
	public ModulePlacementEdgewards(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators, 
			Application application, ModuleMapping moduleMapping){
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setModuleMapping(moduleMapping);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		setSensors(sensors);
		setActuators(actuators);
		setCurrentCpuLoad(new HashMap<Integer, Double>());
		updateCurrentCpuLoad();
		setCurrentModuleMap(new HashMap<Integer, List<String>>());
		setCurrentModuleLoadMap(new HashMap<Integer, Map<String, Double>>());
		setCurrentModuleInstanceNum(new HashMap<Integer, Map<String, Integer>>());
		for(FogDevice dev : getFogDevices()){
			getCurrentModuleLoadMap().put(dev.getId(), new HashMap<String, Double>());
			getCurrentModuleMap().put(dev.getId(), new ArrayList<String>());
			getCurrentModuleInstanceNum().put(dev.getId(), new HashMap<String, Integer>());
		}
		
		mapModules();
		
		System.out.println(getCurrentModuleInstanceNum());
		setModuleInstanceCountMap(getCurrentModuleInstanceNum());
	}
	
	/**
	 * Function to calculate the available MIPS on each device at the time placement is called.
	 */
	private void updateCurrentCpuLoad() {
		//TODO Needs changing. We need to fit in the current available MIPS here.
		for(FogDevice device : getFogDevices()){
			getCurrentCpuLoad().put(device.getId(), 0.0);
		}
	}

	@Override
	protected void mapModules() {
		
		for(String deviceName : getModuleMapping().getModuleMapping().keySet()){
			for(String moduleName : getModuleMapping().getModuleMapping().get(deviceName).keySet()){
				int deviceId = CloudSim.getEntityId(deviceName);
				getCurrentModuleMap().get(deviceId).add(moduleName);
				getCurrentModuleLoadMap().get(deviceId).put(moduleName, 0.0);
				getCurrentModuleInstanceNum().get(deviceId).put(moduleName, 0);
			}
		}
		
		List<List<Integer>> leafToRootPaths = getLeafToRootPaths();
		
		for(List<Integer> path : leafToRootPaths){
			placeModulesInPath(path);
		}
		
		for(int deviceId : getCurrentModuleMap().keySet()){
			System.out.println(getFogDeviceById(deviceId).getName() + "--->"+getCurrentModuleMap().get(deviceId));
			for(String module : getCurrentModuleMap().get(deviceId)){
				createModuleInstanceOnDevice(getApplication().getModuleByName(module), getFogDeviceById(deviceId));
			}
		}
	}
	
	private List<String> getModulesToPlace(List<String> placedOperators){
		System.out.println("Placed Modules : "+placedOperators);
		Application app = getApplication();
		List<String> modulesToPlace_1 = new ArrayList<String>();
		List<String> modulesToPlace = new ArrayList<String>();
		for(AppModule module : app.getModules()){
			if(!placedOperators.contains(module.getName()))
				modulesToPlace_1.add(module.getName());
		}
		for(String moduleName : modulesToPlace_1){
			boolean toBePlaced = true;
			
			for(AppEdge edge : app.getEdges()){
				//CHECK IF OUTGOING DOWN EDGES ARE PLACED
				if(edge.getSource().equals(moduleName) && edge.getDirection()==Tuple.DOWN && !placedOperators.contains(edge.getDestination()))
					toBePlaced = false;
				//CHECK IF INCOMING UP EDGES ARE PLACED
				if(edge.getDestination().equals(moduleName) && edge.getDirection()==Tuple.UP && !placedOperators.contains(edge.getSource()))
					toBePlaced = false;
			}
			if(toBePlaced)
				modulesToPlace.add(moduleName);
		}
		System.out.println("Modules to place : "+modulesToPlace);

		return modulesToPlace;
	}
	
	protected double getRateOfSensor(String sensorType){
		for(Sensor sensor : getSensors()){
			if(sensor.getTupleType().equals(sensorType))
				return 1/sensor.getTransmitDistribution().getMeanInterTransmitTime();
		}
		return 0;
	}
	
	private void placeModulesInPath(List<Integer> path) {
		System.out.println("NEW PATH");
		// TODO Place modules in a leaf-to-root path
		if(path.size()==0)return;
		List<String> placedOperators = new ArrayList<String>();
		Map<AppEdge, Double> appEdgeToRate = new HashMap<AppEdge, Double>();
		
		for(AppEdge edge : getApplication().getEdges()){
			if(edge.isPeriodic()){
				appEdgeToRate.put(edge, 1/edge.getPeriodicity());
			}
		}
		
		for(Integer deviceId : path){
			System.out.println("On device "+CloudSim.getEntityName(deviceId));
			FogDevice device = getFogDeviceById(deviceId);
			Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
			Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);
			placedOperators.addAll(sensorsAssociated.keySet()); // ADDING ALL SENSORS TO PLACED LIST
			placedOperators.addAll(actuatorsAssociated.keySet()); // ADDING ALL ACTUATORS TO PLACED LIST
			//placedOperators.addAll(getCurrentModuleInstanceNum().get(deviceId).keySet());
			System.out.println("Currently placed operators = "+placedOperators);
			for(String sensor : sensorsAssociated.keySet()){
				for(AppEdge edge : getApplication().getEdges()){
					if(edge.getSource().equals(sensor)){
						appEdgeToRate.put(edge, sensorsAssociated.get(sensor)*getRateOfSensor(sensor));
					}
				}
			}
						
			// now updating the AppEdge rates for the entire application based on the knowledge so far
			boolean changed = true;
			while(changed){		//LOOP RUNS AS LONG AS SOMETHING NEW IS ADDED TO THE MAP
				//System.out.println(appEdgeToRate);
				changed=false;
				Map<AppEdge, Double> rateMap = new HashMap<AppEdge, Double>(appEdgeToRate);
				for(AppEdge edge : rateMap.keySet()){
					AppModule destModule = getApplication().getModuleByName(edge.getDestination());
					if(destModule == null)continue;
					Map<Pair<String, String>, SelectivityModel> map = destModule.getSelectivityMap();
					for(Pair<String, String> pair : map.keySet()){
						if(pair.getFirst().equals(edge.getTupleType())){
							double outputRate = appEdgeToRate.get(edge)*map.get(pair).getMeanRate();
							AppEdge outputEdge = getApplication().getEdgeMap().get(pair.getSecond());
							if(!appEdgeToRate.containsKey(outputEdge) || appEdgeToRate.get(outputEdge)!=outputRate){
								System.out.println(outputEdge.getSource()+"----->"+outputEdge.getDestination()+" : "+outputRate);
								changed = true;
							}
							appEdgeToRate.put(outputEdge, outputRate);
							
						}
					}
				}
			}
			
			List<String> operatorsToPlace = getModulesToPlace(placedOperators);
			
			while(operatorsToPlace.size() > 0){
				System.out.println("BEGINNING OF LOOP : Operators to Place : "+operatorsToPlace);
				String operatorName = operatorsToPlace.get(0);
				double totalCpuLoad = 0;
				System.out.println();

				//TODO IF OPERATOR IS ALREADY PLACED UPSTREAM, THEN UPDATE THE EXISTING OPERATOR
				int upsteamDeviceId = isPlacedUpstream(operatorName, path);
				System.out.println(operatorName+" XXXXX Upstream Device : "+CloudSim.getEntityName(upsteamDeviceId));
				if(upsteamDeviceId > 0){
					System.out.println(CloudSim.getEntityName(deviceId)+" : "+getCurrentModuleInstanceNum().get(upsteamDeviceId).get(operatorName)+" modules of Operator "+operatorName+" is already placed on device "+CloudSim.getEntityName(upsteamDeviceId));
					if(upsteamDeviceId==deviceId){
						placedOperators.add(operatorName);
						operatorsToPlace = getModulesToPlace(placedOperators);
						
						//TODO NOW THE OPERATOR TO PLACE IS IN THE CURRENT DEVICE. CHECK IF THE NODE CAN SUSTAIN THE OPERATOR
						for(AppEdge edge : getApplication().getEdges()){		// take all incoming edges
							if(edge.getDestination().equals(operatorName)){
								double rate = appEdgeToRate.get(edge);
								totalCpuLoad += rate*edge.getTupleCpuLength();
								/*System.out.println("Tuple type = "+edge.getTupleType());
								System.out.println("Rate = "+rate);
								System.out.println("Cpu Load = "+rate*edge.getTupleCpuLength());*/
							}
						}
						if(totalCpuLoad + getCurrentCpuLoad().get(deviceId) > device.getHost().getTotalMips()){
							/*System.out.println("Placement of operator "+operatorName+ "NOT POSSIBLE on device "+device.getName());
							System.out.println("CPU load = "+totalCpuLoad);
							System.out.println("Current CPU load = "+getCurrentCpuLoad().get(deviceId));
							System.out.println("Max mips = "+device.getHost().getTotalMips());*/
							System.out.println("Need to shift module "+operatorName+" somewhere upstream.");
							System.out.println("Operators to place = "+operatorsToPlace);
							System.out.println(getCurrentModuleInstanceNum());
							List<String> _placedOperators = shiftModuleNorth(operatorName, totalCpuLoad, deviceId, operatorsToPlace);
							for(String placedOperator : _placedOperators){
								if(!placedOperators.contains(placedOperator))
									placedOperators.add(placedOperator);
							}
							System.out.println("---------------------------------------------------");
							System.out.println("Operators to place after shift = "+operatorsToPlace);
							System.out.println("Instance map after shifting = "+getCurrentModuleInstanceNum());
						} else{
							placedOperators.add(operatorName);
							getCurrentCpuLoad().put(deviceId, getCurrentCpuLoad().get(deviceId)+totalCpuLoad);
							getCurrentModuleInstanceNum().get(deviceId).put(operatorName, getCurrentModuleInstanceNum().get(deviceId).get(operatorName)+1);
							System.out.println("You can rest in peace. "+operatorName+" can be created in "+device.getName());
						}
					}
				}else{
					System.out.println("+++++++++++++++++++++++++");
					// FINDING OUT WHETHER PLACEMENT OF OPERATOR ON DEVICE IS POSSIBLE
					for(AppEdge edge : getApplication().getEdges()){		// take all incoming edges
						if(edge.getDestination().equals(operatorName)){
							System.out.println(edge.getTupleType());
							double rate = appEdgeToRate.get(edge);
							totalCpuLoad += rate*edge.getTupleCpuLength();
							System.out.println("Tuple type = "+edge.getTupleType());
							System.out.println("Rate = "+rate);
							System.out.println("Cpu Load = "+rate*edge.getTupleCpuLength());
						}
					}
					System.out.println("Trying to place module "+operatorName);
						
					if(totalCpuLoad + getCurrentCpuLoad().get(deviceId) > device.getHost().getTotalMips()){
						System.out.println("Placement of operator "+operatorName+ "NOT POSSIBLE on device "+device.getName());
						System.out.println("CPU load = "+totalCpuLoad);
						System.out.println("Current CPU load = "+getCurrentCpuLoad().get(deviceId));
						System.out.println("Max mips = "+device.getHost().getTotalMips());
					}
					else{
						System.out.println("Placement of operator "+operatorName+ " on device "+device.getName());
						getCurrentCpuLoad().put(deviceId, totalCpuLoad + getCurrentCpuLoad().get(deviceId));

						System.out.println("Updated CPU load = "+getCurrentCpuLoad().get(deviceId));
						if(!currentModuleMap.containsKey(deviceId))
							currentModuleMap.put(deviceId, new ArrayList<String>());
						currentModuleMap.get(deviceId).add(operatorName);
						placedOperators.add(operatorName);
						operatorsToPlace = getModulesToPlace(placedOperators);
						getCurrentModuleLoadMap().get(device.getId()).put(operatorName, totalCpuLoad);
						
						int max = 1;
						for(AppEdge edge : getApplication().getEdges()){
							if(edge.getSource().equals(operatorName) && actuatorsAssociated.containsKey(edge.getDestination()))
								max = Math.max(actuatorsAssociated.get(edge.getDestination()), max);
							if(edge.getDestination().equals(operatorName) && sensorsAssociated.containsKey(edge.getSource()))
								max = Math.max(sensorsAssociated.get(edge.getSource()), max);
						}
						getCurrentModuleInstanceNum().get(deviceId).put(operatorName, max);
						System.out.println("Number of instances for operator "+operatorName+" = "+max);
						
					}
				}
			
			
				operatorsToPlace.remove(operatorName);
			}
			
		}
		
	}

	/**
	 * Shifts a module moduleName from device deviceId northwards
	 * @param moduleName
	 * @param cpuLoad cpuLoad of the module
	 * @param deviceId
	 */
	private List<String> shiftModuleNorth(String moduleName, double cpuLoad, Integer deviceId, List<String> operatorsToPlace) {
		// TODO Auto-generated method stub
		
		System.out.println("------------------------------------------------");
		System.out.println("Shifting module "+moduleName +" northwards.");
		
		List<String> modulesToShift = findModulesToShift(moduleName, deviceId);
		
		System.out.println("Modules to shift northwards : "+modulesToShift);
		
		Map<String, Integer> moduleToNumInstances = new HashMap<String, Integer>();
		double totalCpuLoad = 0;
		Map<String, Double> loadMap = new HashMap<String, Double>();
		for(String module : modulesToShift){
			loadMap.put(module, getCurrentModuleLoadMap().get(deviceId).get(module));
			moduleToNumInstances.put(module, getCurrentModuleInstanceNum().get(deviceId).get(module)+1);
			totalCpuLoad += getCurrentModuleLoadMap().get(deviceId).get(module);
			getCurrentModuleLoadMap().get(deviceId).remove(module);
			getCurrentModuleMap().get(deviceId).remove(module);
			getCurrentModuleInstanceNum().get(deviceId).remove(module);
		}
		
		
		getCurrentCpuLoad().put(deviceId, getCurrentCpuLoad().get(deviceId)-totalCpuLoad);
		loadMap.put(moduleName, loadMap.get(moduleName)+cpuLoad);
		totalCpuLoad += cpuLoad;
		
		System.out.println("Module instances to shift northwards : "+moduleToNumInstances);
		
		int id = getParentDevice(deviceId);
		while(true){
			if(id==-1){
				System.out.println("Could not place modules "+modulesToShift+" northwards.");
				break;
			}
			System.out.println("Now on device "+CloudSim.getEntityName(id));
			FogDevice fogDevice = getFogDeviceById(id);
			if(getCurrentCpuLoad().get(id) + totalCpuLoad > fogDevice.getHost().getTotalMips()){
				// keep shifting upwards
				List<String> _modulesToShift = findModulesToShift(modulesToShift, id);	// All modules in _modulesToShift are currently placed on device id
				double cpuLoadShifted = 0;		// the total cpu load shifted from device id to its parent
				for(String module : _modulesToShift){
					if(!modulesToShift.contains(module)){
						moduleToNumInstances.put(module, getCurrentModuleInstanceNum().get(id).get(module)+moduleToNumInstances.get(module));
						loadMap.put(module, getCurrentModuleLoadMap().get(id).get(module));
						cpuLoadShifted += getCurrentModuleLoadMap().get(id).get(module);
						totalCpuLoad += getCurrentModuleLoadMap().get(id).get(module);
						getCurrentModuleLoadMap().get(id).remove(module);
						getCurrentModuleMap().get(id).remove(module);
						getCurrentModuleInstanceNum().get(id).remove(module);
					}					
				}
				getCurrentCpuLoad().put(id, getCurrentCpuLoad().get(id)-cpuLoadShifted);
				
				System.out.println("CPU load after operator removal on device "+CloudSim.getEntityName(id)+" = "+getCurrentCpuLoad().get(id));
				modulesToShift = _modulesToShift;
				id = getParentDevice(id);
			} else{
				// can place the modules here (@ id)
				System.out.println("Can place modules "+modulesToShift+ "on device "+CloudSim.getEntityName(id));
				double totalLoad = 0;
				for(String module : loadMap.keySet()){
					totalLoad += loadMap.get(module);
					getCurrentModuleLoadMap().get(id).put(module, loadMap.get(module));
					getCurrentModuleMap().get(id).add(module);
					System.out.println("Final module to num instances : "+moduleToNumInstances);
					//for(String module_ : moduleToNumInstances.keySet()){
					String module_ = module;
						int initialNumInstances = 0;
						if(getCurrentModuleInstanceNum().get(id).containsKey(module_))
							initialNumInstances = getCurrentModuleInstanceNum().get(id).get(module_);
						int finalNumInstances = initialNumInstances + moduleToNumInstances.get(module_);
						System.out.println("Placing "+finalNumInstances+" on "+CloudSim.getEntityName(id));
						getCurrentModuleInstanceNum().get(id).put(module_, finalNumInstances);
					//}
				}
				getCurrentCpuLoad().put(id, totalLoad);
				System.out.println("FINALLY placed "+loadMap.keySet()+" at device "+CloudSim.getEntityName(id));
				operatorsToPlace.removeAll(loadMap.keySet());
				System.out.println("CPU load on device "+CloudSim.getEntityName(id)+" = "+getCurrentCpuLoad().get(id));
				List<String> placedOperators = new ArrayList<String>();
				for(String op : loadMap.keySet())placedOperators.add(op);
				return placedOperators;
			}	
		}
		return new ArrayList<String>();
		
	}

	private List<String> findModulesToShift(String module, Integer deviceId) {
		List<String> upstreamModules = new ArrayList<String>();
		upstreamModules.add(module);
		boolean changed = true;
		while(changed){
			changed = false;
			for(AppEdge edge : getApplication().getEdges()){
				if(upstreamModules.contains(edge.getSource()) && edge.getDirection()==Tuple.UP && getCurrentModuleMap().get(deviceId).contains(edge.getDestination()) 
						&& !upstreamModules.contains(edge.getDestination())){
					upstreamModules.add(edge.getDestination());
					changed = true;
				}
			}
		}
		return upstreamModules;
		
	}
	
	private List<String> findModulesToShift(List<String> modules, Integer deviceId) {
		List<String> upstreamModules = new ArrayList<String>();
		upstreamModules.addAll(modules);
		boolean changed = true;
		while(changed){
			changed = false;
			for(AppEdge edge : getApplication().getEdges()){
				if(upstreamModules.contains(edge.getSource()) && edge.getDirection()==Tuple.UP && getCurrentModuleMap().get(deviceId).contains(edge.getDestination()) 
						&& !upstreamModules.contains(edge.getDestination())){
					upstreamModules.add(edge.getDestination());
					changed = true;
				}
			}
		}
		return upstreamModules;
		
	}
	
	private int isPlacedUpstream(String operatorName, List<Integer> path) {
		System.out.println(path);
		for(Integer i : path)System.out.println(CloudSim.getEntityName(i));
		for(int deviceId : path){
			if(currentModuleMap.containsKey(deviceId) && currentModuleMap.get(deviceId).contains(operatorName))
				return deviceId;
		}
		return -1;
	}

	private Map<String, Integer> getAssociatedSensors(FogDevice device) {
		Map<String, Integer> endpoints = new HashMap<String, Integer>();
		for(Sensor sensor : getSensors()){
			if(sensor.getGatewayDeviceId()==device.getId()){
				if(!endpoints.containsKey(sensor.getTupleType()))
					endpoints.put(sensor.getTupleType(), 0);
				endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType())+1);
			}
		}
		return endpoints;
	}
	private Map<String, Integer> getAssociatedActuators(FogDevice device) {
		Map<String, Integer> endpoints = new HashMap<String, Integer>();
		for(Actuator actuator : getActuators()){
			if(actuator.getGatewayDeviceId()==device.getId()){
				if(!endpoints.containsKey(actuator.getActuatorType()))
					endpoints.put(actuator.getActuatorType(), 0);
				endpoints.put(actuator.getActuatorType(), endpoints.get(actuator.getActuatorType())+1);
			}
		}
		return endpoints;
	}
	
	@SuppressWarnings("serial")
	protected List<List<Integer>> getPaths(final int fogDeviceId){
		FogDevice device = (FogDevice)CloudSim.getEntity(fogDeviceId); 
		if(device.getChildrenIds().size() == 0){		
			final List<Integer> path =  (new ArrayList<Integer>(){{add(fogDeviceId);}});
			List<List<Integer>> paths = (new ArrayList<List<Integer>>(){{add(path);}});
			return paths;
		}
		List<List<Integer>> paths = new ArrayList<List<Integer>>();
		for(int childId : device.getChildrenIds()){
			List<List<Integer>> childPaths = getPaths(childId);
			for(List<Integer> childPath : childPaths)
				childPath.add(fogDeviceId);
			paths.addAll(childPaths);
		}
		return paths;
	}
	
	protected List<List<Integer>> getLeafToRootPaths(){
		FogDevice cloud=null;
		for(FogDevice device : getFogDevices()){
			if(device.getName().equals("cloud"))
				cloud = device;
		}
		return getPaths(cloud.getId());
	}
	
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}

	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	public Map<Integer, List<String>> getCurrentModuleMap() {
		return currentModuleMap;
	}

	public void setCurrentModuleMap(Map<Integer, List<String>> currentModuleMap) {
		this.currentModuleMap = currentModuleMap;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}

	public Map<Integer, Double> getCurrentCpuLoad() {
		return currentCpuLoad;
	}

	public void setCurrentCpuLoad(Map<Integer, Double> currentCpuLoad) {
		this.currentCpuLoad= currentCpuLoad;
	}

	public Map<Integer, Map<String, Double>> getCurrentModuleLoadMap() {
		return currentModuleLoadMap;
	}

	public void setCurrentModuleLoadMap(
			Map<Integer, Map<String, Double>> currentModuleLoadMap) {
		this.currentModuleLoadMap = currentModuleLoadMap;
	}

	public Map<Integer, Map<String, Integer>> getCurrentModuleInstanceNum() {
		return currentModuleInstanceNum;
	}

	public void setCurrentModuleInstanceNum(
			Map<Integer, Map<String, Integer>> currentModuleInstanceNum) {
		this.currentModuleInstanceNum = currentModuleInstanceNum;
	}

}
