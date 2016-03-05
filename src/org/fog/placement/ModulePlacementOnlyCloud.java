package org.fog.placement;

import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;

public class ModulePlacementOnlyCloud extends ModulePlacement{
	
	public ModulePlacementOnlyCloud(List<FogDevice> fogDevices, Application application){
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		mapModules();
	}
	

	@Override
	protected void mapModules() {
		List<AppModule> modules = getApplication().getModules();
		for(AppModule module : modules){
			FogDevice cloud = getDeviceById(CloudSim.getEntityId("cloud"));
			createModuleInstanceOnDevice(module, cloud);
		}
	}
}
