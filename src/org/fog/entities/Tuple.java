package org.fog.entities;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

public class Tuple extends Cloudlet{

	private String queryId;
	private String destOperatorId;
	private String srcOperatorId;
	private String sensorName;
	private int actualTupleId;
	private double emitTime;
	private String sensorType;
	public Tuple(String queryId, int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		setQueryId(queryId);
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getDestOperatorId() {
		return destOperatorId;
	}

	public void setDestOperatorId(String destOperatorId) {
		this.destOperatorId = destOperatorId;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public int getActualTupleId() {
		return actualTupleId;
	}

	public void setActualTupleId(int actualTupleId) {
		this.actualTupleId = actualTupleId;
	}

	public String getSrcOperatorId() {
		return srcOperatorId;
	}

	public void setSrcOperatorId(String srcOperatorId) {
		this.srcOperatorId = srcOperatorId;
	}

	public double getEmitTime() {
		return emitTime;
	}

	public void setEmitTime(double emitTime) {
		this.emitTime = emitTime;
	}

	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

}
