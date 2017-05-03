package org.fog.entities;

import org.fog.utils.GeoLocation;
import org.fog.utils.distribution.Distribution;

public class SensorCharacteristics {

	private int id;
	private String appId;
	private String tupleType;
	private Distribution transmitDistribution;
	private int cpuLength;
	private int nwLength;
	private GeoLocation geoLocation;
	
	public SensorCharacteristics(int id, String appId, String tupleType, Distribution transmitDistribution, 
			int cpuLength, int nwLength, GeoLocation geoLocation) {
		setId(id);
		setAppId(appId);
		setTupleType(tupleType);
		setTransmitDistribution(transmitDistribution);
		setCpuLength(cpuLength);
		setNwLength(nwLength);
		setGeoLocation(geoLocation);
	}
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getCpuLength() {
		return cpuLength;
	}

	public void setCpuLength(int cpuLength) {
		this.cpuLength = cpuLength;
	}

	public int getNwLength() {
		return nwLength;
	}

	public void setNwLength(int nwLength) {
		this.nwLength = nwLength;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
