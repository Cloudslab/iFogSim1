package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleMapping {


	protected Map<String, Map<String, Integer>> moduleMapping;
	
	public static ModuleMapping createModuleMapping(){
		return new ModuleMapping();
	}

	public Map<String, Map<String, Integer>> getModuleMapping() {
		return moduleMapping;
	}
	
	public void setModuleMapping(Map<String, Map<String, Integer>> moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	protected ModuleMapping(){
		setModuleMapping(new HashMap<String, Map<String, Integer>>());
	}
	
	public void addModuleToDevice(String moduleName, String deviceName){
		addModuleToDevice(moduleName, deviceName, 1);
	}
	
	public void addModuleToDevice(String moduleName, String deviceName, int instanceCount){
		if(!getModuleMapping().containsKey(deviceName))
			getModuleMapping().put(deviceName, new HashMap<String, Integer>());
		if(!getModuleMapping().get(deviceName).containsKey(moduleName))
			getModuleMapping().get(deviceName).put(moduleName, instanceCount);
	}
	
}
