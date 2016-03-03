package org.fog.dsp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;

public class OperatorPlacementOnlyCloud {
	private List<FogDevice> fogDevices;
	private Application application;
	private Map<String, Integer> moduleToDeviceMap;
	
	public OperatorPlacementOnlyCloud(List<FogDevice> fogDevices, Application application){
		this.setFogDevices(fogDevices);
		this.setApplication(application);
		this.moduleToDeviceMap = new HashMap<String, Integer>();
		List<AppModule> modules = application.getModules();
		for(AppModule module : modules){
			FogDevice currentDevice = getDeviceById(CloudSim.getEntityId("cloud"));
			if(canBeCreated(currentDevice, module)){
				
				moduleToDeviceMap.put(module.getName(), currentDevice.getId());
			}
		}		
	}
	
	private FogDevice getDeviceById(int id){
		for(FogDevice dev : fogDevices){
			if(dev.getId() == id)
				return dev;
		}
		return null;
	}
	
	private boolean canBeCreated(FogDevice fogDevice, AppModule module){
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(module);
	}
	
	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Map<String, Integer> getModuleToDeviceMap() {
		return moduleToDeviceMap;
	}

	public void setModuleToDeviceMap(Map<String, Integer> moduleToDeviceMap) {
		this.moduleToDeviceMap = moduleToDeviceMap;
	}

}
