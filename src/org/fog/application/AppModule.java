package org.fog.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.AppModuleAddress;
import org.fog.utils.FogUtils;

/**
 * Class representing an application module, the processing elements of the application model of iFogSim.
 * @author Harshit Gupta
 *
 */
public class AppModule extends PowerVm{
	private String name;
	private String appId;
	private Map<Pair<String, String>, SelectivityModel> selectivityMap;
	
	/**
	 * A map from the AppModules sending tuples UP to this module to their instance IDs.
	 * If a new instance ID is detected, the number of instances is incremented.  
	 */
	private Map<String, List<Integer>> downInstanceIdsMaps;
	
	/**
	 * Map from tuple type to addresses of modules that will receive those messages
	 */
	private Map<String, List<AppModuleAddress>> destModules;
	
	/**
	 * Number of instances of this module
	 */
	private int numInstances;
	
	/**
	 * Mapping from tupleType emitted by this AppModule to Actuators subscribing to that tupleType
	 */
	private Map<String, List<Integer>> actuatorSubscriptions;
	
	public AppModule(
			int id,
			String name,
			String appId,
			int userId,
			double mips,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler,
			Map<Pair<String, String>, SelectivityModel> selectivityMap) {
		super(id, userId, mips, 1, ram, bw, size, 1, vmm, cloudletScheduler, 300);
		setName(name);
		setId(id);
		setAppId(appId);
		setUserId(userId);
		setUid(getUid(userId, id));
		setMips(mips);
		setNumberOfPes(1);
		setRam(ram);
		setBw(bw);
		setSize(size);
		setVmm(vmm);
		setCloudletScheduler(cloudletScheduler);
		setInMigration(false);
		setBeingInstantiated(true);
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
		setSelectivityMap(selectivityMap);
		setActuatorSubscriptions(new HashMap<String, List<Integer>>());
		setNumInstances(0);
		setDownInstanceIdsMaps(new HashMap<String, List<Integer>>());
		setDestModules(new HashMap<String, List<AppModuleAddress>>());
	}
	
	public AppModule(AppModule operator) {
		super(FogUtils.generateEntityId(), operator.getUserId(), operator.getMips(), 1, operator.getRam(), operator.getBw(), operator.getSize(), 1, operator.getVmm(), new TupleScheduler(operator.getMips(), 1), operator.getSchedulingInterval());
		setName(operator.getName());
		setAppId(operator.getAppId());
		setInMigration(false);
		setBeingInstantiated(true);
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
		setSelectivityMap(operator.getSelectivityMap());
		setDownInstanceIdsMaps(new HashMap<String, List<Integer>>());
		setDestModules(new HashMap<String, List<AppModuleAddress>>());
		setActuatorSubscriptions(new HashMap<String, List<Integer>>());
	}
	
	public void subscribeActuator(int id, String tupleType){
		if(!getActuatorSubscriptions().containsKey(tupleType))
			getActuatorSubscriptions().put(tupleType, new ArrayList<Integer>());
		getActuatorSubscriptions().get(tupleType).add(id);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<Pair<String, String>, SelectivityModel> getSelectivityMap() {
		return selectivityMap;
	}
	public void setSelectivityMap(Map<Pair<String, String>, SelectivityModel> selectivityMap) {
		this.selectivityMap = selectivityMap;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public Map<String, List<Integer>> getActuatorSubscriptions() {
		return actuatorSubscriptions;
	}
	public void setActuatorSubscriptions(Map<String, List<Integer>> actuatorSubscriptions) {
		this.actuatorSubscriptions = actuatorSubscriptions;
	}
	public Map<String, List<Integer>> getDownInstanceIdsMaps() {
		return downInstanceIdsMaps;
	}
	public void setDownInstanceIdsMaps(Map<String, List<Integer>> downInstanceIdsMaps) {
		this.downInstanceIdsMaps = downInstanceIdsMaps;
	}
	public int getNumInstances() {
		return numInstances;
	}
	public void setNumInstances(int numInstances) {
		this.numInstances = numInstances;
	}
	public Map<String, List<AppModuleAddress>> getDestModules() {
		return destModules;
	}
	public void setDestModules(Map<String, List<AppModuleAddress>> destModules) {
		this.destModules = destModules;
	}
	public void addDestModule(String edge, AppModuleAddress destModule) {
		if (! getDestModules().containsKey(edge))
			getDestModules().put(edge, new ArrayList<AppModuleAddress>());
		getDestModules().get(edge).add(destModule);
	}
}
