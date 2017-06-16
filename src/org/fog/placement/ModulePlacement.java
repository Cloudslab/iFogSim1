/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */
package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing the placement of one instance of application. One instance means all modules of application will have 1 instance running. 
 * All the module placements across these objects are independent, i.e. if > 1 modules map to the same Fog Device, they will have separate VMs.
 * TODO Future work would be to define Modules that are merge-able. 
 * @author Harshit Gupta
 * @since iFogSim Toolkit 2.0
 */
public class ModulePlacement {
	/**
	 * Map between sensorType and list of IDs of sensors of that type
	 */
	private Map<String, List<Integer>> sensorIds;
	
	/**
	 * Map between actuatorType and list of IDs of actuators of that type
	 */
	private Map<String, List<Integer>> actuatorIds;
	
	/**
	 * Map between module name and ID of fog device it is placed on
	 */
	private Map<String, Integer> placementMap;
	
	public ModulePlacement() {
		setSensorIds(new HashMap<String, List<Integer>>());
		setActuatorIds(new HashMap<String, List<Integer>>());
		setPlacementMap(new HashMap<String, Integer>());
	}
	
	public void addSensorId(String sensorType, Integer sensorId) {
		if (!getSensorIds().containsKey(sensorType))
			getSensorIds().put(sensorType, new ArrayList<Integer>());
		getSensorIds().get(sensorType).add(sensorId);
	}
	
	public void addActuatorId(String actuatorType, Integer actuatorId) {
		if (!getActuatorIds().containsKey(actuatorType))
			getActuatorIds().put(actuatorType, new ArrayList<Integer>());
		getActuatorIds().get(actuatorType).add(actuatorId);
	}
	
	/**
	 * Specify the mapping of an application module on a fog device
	 * @param moduleName name of application module to be mapped
	 * @param deviceId fog device ID on which module is to be mapped
	 */
	public void addMapping(String moduleName, Integer deviceId) {
		placementMap.put(moduleName, deviceId);
	}
	
	/**
	 * Gets fog device ID on which a module has been mapped in this ModulePlacement
	 * @param moduleName name of module
	 * @return
	 */
	public Integer getMappedDeviceId(String moduleName) {
		if (placementMap.containsKey(moduleName))
			return placementMap.get(moduleName);
		else 
			return -1;
	}
	
	public Map<String, List<Integer>> getSensorIds() {
		return sensorIds;
	}
	public void setSensorIds(Map<String, List<Integer>> sensorIds) {
		this.sensorIds = sensorIds;
	}
	public Map<String, List<Integer>>getActuatorIds() {
		return actuatorIds;
	}
	public void setActuatorIds(Map<String, List<Integer>> actuatorIds) {
		this.actuatorIds = actuatorIds;
	}
	public Map<String, Integer> getPlacementMap() {
		return placementMap;
	}
	public void setPlacementMap(Map<String, Integer> placementMap) {
		this.placementMap = placementMap;
	}
}
