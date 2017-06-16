/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Fog Simulation) Toolkit for Modeling and Simulation of Fog Computing
 *
 */
package org.fog.placement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.ActuatorCharacteristics;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.SensorCharacteristics;

/**
 * Abstract class that performs placement of application modules on physical topology.
 * Each application has its own placement policy.
 * 
 * @author Harshit Gupta
 * @since iFogSim 2.0
 */
public abstract class ModulePlacementPolicy {
	
	public static int ONLY_CLOUD = 1;
	public static int EDGEWARDS = 2;
	public static int USER_MAPPING = 3;
	
	/**
	 * List of fog devices on which modules can be placed
	 */
	private List<FogDevice> fogDevices;
	
	/**
	 * Application for which placement needs to be done
	 */
	private Application application;
	
	/**
	 * Map from module name to list of fog device IDs it is to be placed on
	 */
	private Map<String, List<Integer>> moduleToDeviceMap;
	
	/**
	 * Map from fog device ID to a list of modules that are to be placed on it
	 */
	private Map<Integer, List<AppModule>> deviceToModuleMap;
	
	/**
	 * Map from fog device ID to its characteristics
	 */
	private Map<Integer, FogDeviceCharacteristics> fogDeviceCharacteristics;

	/**
	 * Map from sensor ID to its characteristics
	 */
	private Map<Integer, SensorCharacteristics> sensorCharacteristics;

	/**
	 * Map from actuator ID to its characteristics
	 */
	private Map<Integer, ActuatorCharacteristics> actuatorCharacteristics;
	
	protected ModulePlacementPolicy() {
		setFogDeviceCharacteristics(new HashMap<Integer, FogDeviceCharacteristics>());
		setSensorCharacteristics(new HashMap<Integer, SensorCharacteristics>());
		setActuatorCharacteristics(new HashMap<Integer, ActuatorCharacteristics>());
	}

	/**
	 * Compute policy-specific placement of application modules on fog devices.
	 * An abstract method, needs to be implemented by specific policies. 
	 * @param fogDeviceCharacteristics
	 * @param sensorCharacteristics
	 * @param actuatorCharacteristics
	 * @return
	 */
	public abstract List<ModulePlacement> computeModulePlacements(List<FogDeviceCharacteristics> fogDeviceCharacteristics, 
			List<SensorCharacteristics> sensorCharacteristics, List<ActuatorCharacteristics> actuatorCharacteristics);
	
	/**
	 * Check if application module can be launched on fog device
	 * @param fogDevice
	 * @param module
	 * @return
	 */
	protected boolean canBeCreated(FogDevice fogDevice, AppModule module){
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(module);
	}
	
	protected int getParentDevice(int fogDeviceId){
		return ((FogDevice)CloudSim.getEntity(fogDeviceId)).getParentId();
	}
	
	protected FogDevice getFogDeviceById(int fogDeviceId){
		return (FogDevice)CloudSim.getEntity(fogDeviceId);
	}
	
	protected boolean createModuleInstanceOnDevice(AppModule _module, final FogDevice device, int instanceCount){
		return false;
	}
	
	/*protected boolean createModuleInstanceOnDevice(AppModule _module, final FogDevice device){
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
	}*/
	
	protected FogDevice getDeviceByName(String deviceName) {
		for(FogDevice dev : getFogDevices()){
			if(dev.getName().equals(deviceName))
				return dev;
		}
		return null;
	}
	
	protected FogDevice getDeviceById(int id){
		for(FogDevice dev : getFogDevices()){
			if(dev.getId() == id)
				return dev;
		}
		return null;
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

	public Map<Integer, FogDeviceCharacteristics> getFogDeviceCharacteristics() {
		return fogDeviceCharacteristics;
	}

	public void setFogDeviceCharacteristics(Map<Integer, FogDeviceCharacteristics> fogDeviceCharacteristics) {
		this.fogDeviceCharacteristics = fogDeviceCharacteristics;
	}

	public Map<Integer, SensorCharacteristics> getSensorCharacteristics() {
		return sensorCharacteristics;
	}

	public void setSensorCharacteristics(Map<Integer, SensorCharacteristics> sensorCharacteristics) {
		this.sensorCharacteristics = sensorCharacteristics;
	}

	public Map<Integer, ActuatorCharacteristics> getActuatorCharacteristics() {
		return actuatorCharacteristics;
	}

	public void setActuatorCharacteristics(Map<Integer, ActuatorCharacteristics> actuatorCharacteristics) {
		this.actuatorCharacteristics = actuatorCharacteristics;
	}
}
