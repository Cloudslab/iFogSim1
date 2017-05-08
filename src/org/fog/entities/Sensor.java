/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */

package org.fog.entities;

import java.util.ArrayList;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.utils.AppModuleAddress;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.Distribution;

public class Sensor extends SimEntity{
	private static String LOG_TAG = "SENSOR";
	
	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long outputSize;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private String destModuleName;
	private Distribution transmitDistribution;
	private int controllerId;
	private double latency;
	private SensorCharacteristics characteristics;
	private Application application;
	private AppModuleAddress destModuleAddr;
	private EndDevice device;
	private int endDeviceId;
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, 
			Distribution transmitDistribution, int cpuLength, int nwLength, String tupleType, String destModuleName, Application application) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setDestModuleName(destModuleName);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
		setApplication(application);
		setCharacteristics(new SensorCharacteristics(getId(), appId, tupleType, transmitDistribution, cpuLength, nwLength, geoLocation));
		setDestModuleAddr(null);
		setEndDeviceId(-1);
	}
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, double latency, GeoLocation geoLocation, 
			Distribution transmitDistribution, String tupleType, Application application) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
		setApplication(application);
		
		AppEdge _edge = null;
		for(AppEdge edge : getApplication().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		int cpuLength = (int) _edge.getTupleCpuLength();
		int nwLength = (int) _edge.getTupleNwLength();
		
		setCharacteristics(new SensorCharacteristics(getId(), appId, tupleType, transmitDistribution, cpuLength, nwLength, geoLocation));
		setDestModuleAddr(null);
		setEndDeviceId(-1);
	}
	
	public Sensor(String name, String tupleType, int userId, String appId, Distribution transmitDistribution, Application application) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		setUserId(userId);
		setApplication(application);
		
		AppEdge _edge = null;
		for(AppEdge edge : getApplication().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		int cpuLength = (int) _edge.getTupleCpuLength();
		int nwLength = (int) _edge.getTupleNwLength();
		
		setCharacteristics(new SensorCharacteristics(getId(), appId, tupleType, transmitDistribution, cpuLength, nwLength, null));
		setDestModuleAddr(null);
		setEndDeviceId(-1);
	}
	
	
	/**
	 * This constructor is called from the code that generates PhysicalTopology from JSON
	 * @param name
	 * @param tupleType
	 * @param string 
	 * @param userId
	 * @param appId
	 * @param transmitDistribution
	 */
	public Sensor(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		setUserId(userId);
		setDestModuleAddr(null);
		setEndDeviceId(-1);
	}
	
	public void transmit(){
		if (getDestModuleAddr() == null) return;
		
		AppEdge _edge = null;
		for(AppEdge edge : getApplication().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		long cpuLength = (long) _edge.getTupleCpuLength();
		long nwLength = (long) _edge.getTupleNwLength();
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, outputSize, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		
		tuple.setDestModuleName(_edge.getDestination());
		tuple.setSrcModuleName(getSensorName());
		Logger.debug(LOG_TAG, getName(), "Sending tuple with tupleId = "+tuple.getCloudletId());

		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
		tuple.setActualTupleId(actualTupleId);
		
		//TODO Correct these
		//sendTuple(tuple, getDestModuleAddr().getFogDeviceId(), getDestModuleAddr().getVmId());
		getDevice().sendTuple(tuple, getDestModuleAddr().getFogDeviceId(), getDestModuleAddr().getVmId());
	}
	
	protected void sendTuple(Tuple tuple, int dstDeviceId, int dstVmId) {
		tuple.setVmId(dstVmId);
		tuple.setSourceDeviceId(getId());
		tuple.setDestinationDeviceId(dstDeviceId);
		send(dstDeviceId, getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
	}
	
	private int updateTimings(String src, String dest){
		Application application = getApplication();
		for(AppLoop loop : application.getLoops()){
			if(loop.hasEdge(src, dest)){
				
				int tupleId = TimeKeeper.getInstance().getUniqueId();
				if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				return tupleId;
			}
		}
		return -1;
	}
	
	@Override
	public void startEntity() {
		System.out.println("Starting sensor with ID "+getId());
		//send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
		send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			System.out.println(getName()+" received charac req");
			int srcId = ((Integer) ev.getData()).intValue();
			sendNow(srcId, ev.getTag(), getCharacteristics());
			break;
		case FogEvents.TUPLE_ACK:
			//transmit(transmitDistribution.getNextValue());
			break;
		case FogEvents.EMIT_TUPLE:
			transmit();
			send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
			break;
		case FogEvents.ENDPOINT_CONNECTION:
			AppModuleAddress addr = (AppModuleAddress) ev.getData();
			processSensorConnection(addr);
			break;
		}
			
	}

	private void processSensorConnection(AppModuleAddress addr) {
		setDestModuleAddr(addr);
	}

	@Override
	public void shutdownEntity() {
		
	}

	public int getGatewayDeviceId() {
		return gatewayDeviceId;
	}

	public void setGatewayDeviceId(int gatewayDeviceId) {
		this.gatewayDeviceId = gatewayDeviceId;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

	public SensorCharacteristics getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(SensorCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public AppModuleAddress getDestModuleAddr() {
		return destModuleAddr;
	}

	public void setDestModuleAddr(AppModuleAddress destModuleAddr) {
		this.destModuleAddr = destModuleAddr;
	}

	public EndDevice getDevice() {
		return device;
	}

	public void setDevice(EndDevice device) {
		this.device = device;
	}

	public int getEndDeviceId() {
		return endDeviceId;
	}

	public void setEndDeviceId(int endDeviceId) {
		this.endDeviceId = endDeviceId;
	}

}
