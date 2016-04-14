package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

public class ModulePlacementEdgewards extends ModulePlacement{
	
	protected ModuleMapping moduleMapping;
	protected List<Sensor> sensors;
	protected List<Actuator> actuators;
	
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
		mapModules();
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
	
	private List<String> getOperatorsToPlace(List<String> placedOperators){
		Application app = getApplication();
		
		return null;
	}
	
	private void placeModulesInPath(List<Integer> path) {
		// TODO Place modules in a leaf-to-root path
		if(path.size()==0)return;
		List<String> placedOperators = new ArrayList<String>();
		for(int deviceId : path){
			FogDevice device = getFogDeviceById(deviceId);
			Map<String, Integer> endpointsAssociated = getAssociatedEndpoints(device);
			placedOperators.addAll(endpointsAssociated.keySet());
			
			List<String> operatorsToPlace = getOperatorsToPlace(placedOperators);
			
		}
		
	}

	private Map<String, Integer> getAssociatedEndpoints(FogDevice device) {
		Map<String, Integer> endpoints = new HashMap<String, Integer>();
		for(Sensor sensor : getSensors()){
			if(sensor.getGatewayDeviceId()==device.getId()){
				if(!endpoints.containsKey(sensor.getTupleType()))
					endpoints.put(sensor.getTupleType(), 0);
				endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType())+1);
			}
		}
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


}
