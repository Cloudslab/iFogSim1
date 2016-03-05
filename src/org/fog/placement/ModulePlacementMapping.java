package org.fog.placement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;

public class ModulePlacementMapping extends ModulePlacement{

	private ModuleMapping moduleMapping;
	
	@Override
	protected void mapModules() {
		//TODO Sending module instances to fog devices
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

	public ModulePlacementMapping(List<FogDevice> fogDevices, Application application, 
			ModuleMapping moduleMapping){
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setModuleMapping(moduleMapping);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		mapModules();
	}
	
	
	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}
	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	
}
