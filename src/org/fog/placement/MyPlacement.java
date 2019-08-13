package org.fog.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.MyApplication;
import org.fog.entities.MyFogDevice;

public abstract class MyPlacement {
	
	
	public static int ONLY_CLOUD = 1;
	public static int EDGEWARDS = 2;
	public static int USER_MAPPING = 3;
	
	private List<MyFogDevice> fogDevices;
	private MyApplication application;
	private Map<String, List<Integer>> moduleToDeviceMap;
	private Map<Integer, List<AppModule>> deviceToModuleMap;
	private Map<Integer, Map<String, Integer>> moduleInstanceCountMap;
	
	protected abstract void mapModules();
	
	protected boolean canBeCreated(MyFogDevice fogDevice, AppModule module){
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(module);
	}
	
	protected int getParentDevice(int fogDeviceId){
		return ((MyFogDevice)CloudSim.getEntity(fogDeviceId)).getParentId();
	}
	
	protected MyFogDevice getMyFogDeviceById(int fogDeviceId){
		return (MyFogDevice)CloudSim.getEntity(fogDeviceId);
	}
	
	protected boolean createModuleInstanceOnDevice(AppModule _module, final MyFogDevice device, int instanceCount){
		return false;
	}
	
	protected boolean createModuleInstanceOnDevice(AppModule _module, final MyFogDevice device){
		AppModule module = null;
		if(getModuleToDeviceMap().containsKey(_module.getName()))
			module = new AppModule(_module);
		else
			module = _module;
			
		if(canBeCreated(device, module)){
			System.out.println("Creating "+module.getName()+" on device "+device.getName());
			
			if(!getDeviceToModuleMap().containsKey(device.getId()))
				getDeviceToModuleMap().put(device.getId(), new ArrayList<AppModule>());
			getDeviceToModuleMap().get(device.getId()).add(module);
			
			if(!getModuleToDeviceMap().containsKey(module.getName()))
				getModuleToDeviceMap().put(module.getName(), new ArrayList<Integer>());
			getModuleToDeviceMap().get(module.getName()).add(device.getId());
			return true;
		} else {
			System.err.println("Module "+module.getName()+" cannot be created on device "+device.getName());
			System.err.println("Terminating");
			return false;
		}
	}
	
	protected MyFogDevice getDeviceByName(String deviceName) {
		for(MyFogDevice dev : getMyFogDevices()){
			if(dev.getName().equals(deviceName))
				return dev;
		}
		return null;
	}
	
	protected MyFogDevice getDeviceById(int id){
		for(MyFogDevice dev : getMyFogDevices()){
			if(dev.getId() == id)
				return dev;
		}
		return null;
	}
	
	public List<MyFogDevice> getMyFogDevices() {
		return fogDevices;
	}

	public void setMyFogDevices(List<MyFogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public MyApplication getMyApplication() {
		return application;
	}

	public void setMyApplication(MyApplication application) {
		this.application = application;
	}

	public Map<String, List<Integer>> getModuleToDeviceMap() {
		return moduleToDeviceMap;
	}

	public void setModuleToDeviceMap(Map<String, List<Integer>> moduleToDeviceMap) {
		this.moduleToDeviceMap = moduleToDeviceMap;
	}

	public Map<Integer, List<AppModule>> getDeviceToModuleMap() {
		return deviceToModuleMap;
	}

	public void setDeviceToModuleMap(Map<Integer, List<AppModule>> deviceToModuleMap) {
		this.deviceToModuleMap = deviceToModuleMap;
	}

	public Map<Integer, Map<String, Integer>> getModuleInstanceCountMap() {
		return moduleInstanceCountMap;
	}

	public void setModuleInstanceCountMap(Map<Integer, Map<String, Integer>> moduleInstanceCountMap) {
		this.moduleInstanceCountMap = moduleInstanceCountMap;
	}

}
