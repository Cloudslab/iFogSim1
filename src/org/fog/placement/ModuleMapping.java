package org.fog.placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleMapping {

	protected Map<String, List<String>> moduleMapping;
	
	public static ModuleMapping createModuleMapping(){
		return new ModuleMapping();
	}

	public Map<String, List<String>> getModuleMapping() {
		return moduleMapping;
	}
	
	public void setModuleMapping(Map<String, List<String>> moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	protected ModuleMapping(){
		setModuleMapping(new HashMap<String, List<String>>());
	}
	
	public void addModuleToDevice(String moduleName, String deviceName){
		if(!getModuleMapping().containsKey(deviceName))
			getModuleMapping().put(deviceName, new ArrayList<String>());
		getModuleMapping().get(deviceName).add(moduleName);
	}
	
}
