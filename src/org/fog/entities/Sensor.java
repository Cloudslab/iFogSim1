package org.fog.entities;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.TupleEmitTimes;

public class Sensor extends SimEntity{

	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long length;
	private long fileSize;
	private long outputSize;
	private double lastTransmitTime = -1;
	private double transmitInterval;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private int tupleCpuSize;
	private int tupleNwSize;
	private String spoutName;
	
	public Sensor(String name, int userId, String appId, int gatewayDeviceId, GeoLocation geoLocation, double transmitInterval, int cpuLength, int nwLength, String tupleType, String spoutName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.length = cpuLength;
		this.fileSize = nwLength;
		this.outputSize = 3;
		this.setTransmitInterval(transmitInterval);
		setUserId(userId);
		setTupleCpuSize(cpuLength);
		setTupleNwSize(nwLength);
		setSpoutName(spoutName);
	}
	
	public void transmit(double delay){
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, length, 1, fileSize, outputSize, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		tuple.setActualTupleId(FogUtils.generateActualTupleId());
		//System.out.println((CloudSim.clock()+delay)+" : Sensor "+getName()+" sending actual tuple id "+tuple.getActualTupleId());
		//System.out.println(CloudSim.getEntityName(gatewayDeviceId));
		tuple.setDestModuleName(getSpoutName());
		tuple.setSrcModuleName(getSensorName());
		//tuple.setEmitTime(CloudSim.clock()+delay);
		//TupleEmitTimes.getInstance().setEmitTime(tuple.getActualTupleId(), CloudSim.clock()+delay);
		send(gatewayDeviceId, delay, FogEvents.TUPLE_ARRIVAL,tuple);
		
		lastTransmitTime = CloudSim.clock();
	}
	
	@Override
	public void startEntity() {
		send(gatewayDeviceId, Math.random()*10+CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ACK:
			//System.out.println("Tuple ack received at time \t"+CloudSim.clock());
			transmit(transmitInterval);
			break;
		}
			
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

	public double getTransmitInterval() {
		return transmitInterval;
	}

	public void setTransmitInterval(double transmitInterval) {
		this.transmitInterval = transmitInterval;
	}

	public double getLastTransmitTime() {
		return lastTransmitTime;
	}

	public void setLastTransmitTime(double lastTransmitTime) {
		this.lastTransmitTime = lastTransmitTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTupleCpuSize() {
		return tupleCpuSize;
	}

	public void setTupleCpuSize(int tupleCpuSize) {
		this.tupleCpuSize = tupleCpuSize;
	}

	public int getTupleNwSize() {
		return tupleNwSize;
	}

	public void setTupleNwSize(int tupleNwSize) {
		this.tupleNwSize = tupleNwSize;
	}

	public String getSpoutName() {
		return spoutName;
	}

	public void setSpoutName(String spoutName) {
		this.spoutName = spoutName;
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

}
