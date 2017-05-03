package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing the placement of one instance of application.
 * All the module placements across these objects are independent, i.e. if > 1 modules map to the same Fog Device, they will have separate VMs.
 * TODO Future work would be to define Modules that are merge-able. 
 * @author Harshit Gupta
 * @since iFogSim Toolkit 2.0
 */
public class ModulePlacement {
	private List<Integer> sensorIds;
	private List<Integer> actuatorIds;
	
	/**
	 * Map between module name and ID of fog device it is placed on
	 */
	private Map<String, Integer> placementMap;
	
	public ModulePlacement() {
		setSensorIds(new ArrayList<Integer>());
		setActuatorIds(new ArrayList<Integer>());
		setPlacementMap(new HashMap<String, Integer>());
	}
	
	public void addSensorId(Integer sensorId) {
		getSensorIds().add(sensorId);
	}
	
	public void addActuatorId(Integer actuatorId) {
		getActuatorIds().add(actuatorId);
	}
	
	public Integer getMappedDeviceId(String moduleName) {
		if (placementMap.containsKey(moduleName))
			return placementMap.get(moduleName);
		else 
			return -1;
	}
	
	public List<Integer> getSensorIds() {
		return sensorIds;
	}
	public void setSensorIds(List<Integer> sensorIds) {
		this.sensorIds = sensorIds;
	}
	public List<Integer> getActuatorIds() {
		return actuatorIds;
	}
	public void setActuatorIds(List<Integer> actuatorIds) {
		this.actuatorIds = actuatorIds;
	}
	public Map<String, Integer> getPlacementMap() {
		return placementMap;
	}
	public void setPlacementMap(Map<String, Integer> placementMap) {
		this.placementMap = placementMap;
	}
}
