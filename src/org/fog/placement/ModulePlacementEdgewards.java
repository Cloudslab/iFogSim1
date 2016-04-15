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
		mapModules();
		
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
		List<List<Integer>> leafToRootPaths = getLeafToRootPaths();
		for(List<Integer> path : leafToRootPaths){
			placeModulesInPath(path);
		}
		
		Map<String, List<String>> mapping = moduleMapping.getModuleMapping(); 
		for(String deviceName : mapping.keySet()){
			FogDevice device = getDeviceByName(deviceName);
			List<String> modulesOnDevice = mapping.get(deviceName);
			for(String moduleName : modulesOnDevice){
				AppModule module = getApplication().getModuleByName(moduleName);
				createModuleInstanceOnDevice(module, device);
			}
		}
	}
	
	private List<String> getModulesToPlace(List<String> placedOperators){
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
		// TODO Place modules in a leaf-to-root path
		if(path.size()==0)return;
		List<String> placedOperators = new ArrayList<String>();
		Map<AppEdge, Double> appEdgeToRate = new HashMap<AppEdge, Double>();
		for(int deviceId : path){
			FogDevice device = getFogDeviceById(deviceId);
			Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
			Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);
			placedOperators.addAll(sensorsAssociated.keySet()); // ADDING ALL SENSORS TO PLACED LIST
			placedOperators.addAll(actuatorsAssociated.keySet()); // ADDING ALL ACTUATORS TO PLACED LIST
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
				changed=false;
				Map<AppEdge, Double> rateMap = new HashMap<AppEdge, Double>(appEdgeToRate);
				for(AppEdge edge : rateMap.keySet()){
					AppModule destModule = getApplication().getModuleByName(edge.getDestination());
					if(destModule == null)continue;
					Map<Pair<String, String>, Double> map = destModule.getSelectivityMap();
					for(Pair<String, String> pair : map.keySet()){
						if(pair.getFirst().equals(edge.getTupleType())){
							double selectivity = map.get(pair);
							double outputRate = appEdgeToRate.get(edge)*selectivity;
							AppEdge outputEdge = getApplication().getEdgeMap().get(pair.getSecond());
							if(!appEdgeToRate.containsKey(outputEdge) || appEdgeToRate.get(outputEdge)!=outputRate)
								changed = true;
							
							appEdgeToRate.put(outputEdge, outputRate);
							
						}
					}
				}
			}
			
			/*for(AppEdge edge : appEdgeToRate.keySet()){
				System.out.println(edge.getTupleType() + "---->" + appEdgeToRate.get(edge));
			}*/
			
			List<String> operatorsToPlace = getModulesToPlace(placedOperators);
			
			while(operatorsToPlace.size() > 0){
				String operatorName = operatorsToPlace.get(0);
				double totalCpuLoad = 0;
				System.out.println();

				for(AppEdge edge : getApplication().getEdges()){		// take all incoming edges
					if(edge.getDestination().equals(operatorName)){
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
				}
			
			
				operatorsToPlace.remove(operatorName);
			}
			
		}
		
	}

	private void updateRateMapWrtSensors(Map<AppEdge, Double> appEdgeToRate) {
		// TODO Auto-generated method stub
		
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

}
