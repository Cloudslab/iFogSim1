/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.fog.entities;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;

/**
 * Vm represents a VM: it runs inside a Host, sharing hostList with other VMs. It processes
 * cloudlets. This processing happens according to a policy, defined by the CloudletScheduler. Each
 * VM has a owner, which can submit cloudlets to the VM to be executed
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class StreamOperator extends Vm{

	private String name;
	private GeoCoverage geoCoverage;
	private String sensorName;
	private String queryId;
	private double expansionRatio;
	private double fileExpansionRatio;
	private double tupleLength;
	private double sensorRate;
	private String sensorType;
	
	private double tupleFileLength;
	public StreamOperator(
			int id,
			String name,
			GeoCoverage geoCoverage,
			String sensorName,
			String queryId,
			int userId,
			double mips,
			int ram,
			long bw,
			long size,
			String vmm,
			CloudletScheduler cloudletScheduler,
			double expansionRatio,
			double fileExpansionRatio, 
			double tupleLength, 
			double tupleFileSize,
			double sensorRate) {
		super(id, userId, mips, 1, ram, bw, size, vmm, cloudletScheduler);
		setName(name);
		setId(id);
		setGeoCoverage(geoCoverage);
		setSensorName(sensorName);
		setQueryId(queryId);
		setUserId(userId);
		setUid(getUid(userId, id));
		setMips(mips);
		setNumberOfPes(1);
		setRam(ram);
		setBw(bw);
		setSize(size);
		setVmm(vmm);
		setCloudletScheduler(cloudletScheduler);
		setExpansionRatio(expansionRatio);
		setFileExpansionRatio(fileExpansionRatio);
		setTupleFileLength(tupleFileSize);
		setTupleLength(tupleLength);
		setInMigration(false);
		setBeingInstantiated(true);
		setSensorRate(sensorRate);
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
	}
	public StreamOperator(StreamOperator operator) {
		super(FogUtils.generateEntityId(), operator.getUserId(), operator.getMips(), 1, operator.getRam(), operator.getBw(), operator.getSize(), operator.getVmm(), new TupleScheduler(operator.getMips(), 1));
		setName(operator.getName());
		setGeoCoverage(operator.getGeoCoverage());
		setSensorName(operator.getSensorName());
		setQueryId(operator.getQueryId());
		setExpansionRatio(operator.getExpansionRatio());
		setFileExpansionRatio(operator.getFileExpansionRatio());
		setTupleFileLength(operator.getTupleFileLength());
		setTupleLength(operator.getTupleLength());
		setInMigration(false);
		setBeingInstantiated(true);
		setSensorRate(operator.getSensorRate());
		setCurrentAllocatedBw(0);
		setCurrentAllocatedMips(null);
		setCurrentAllocatedRam(0);
		setCurrentAllocatedSize(0);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public GeoCoverage getGeoCoverage() {
		return geoCoverage;
	}
	public void setGeoCoverage(GeoCoverage geoCoverage) {
		this.geoCoverage = geoCoverage;
	}
	public String getSensorName() {
		return sensorName;
	}
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public double getExpansionRatio() {
		return expansionRatio;
	}
	public void setExpansionRatio(double expansionRatio) {
		this.expansionRatio = expansionRatio;
	}
	public double getFileExpansionRatio() {
		return fileExpansionRatio;
	}
	public void setFileExpansionRatio(double fileExpansionRatio) {
		this.fileExpansionRatio = fileExpansionRatio;
	}
	public double getTupleLength() {
		return tupleLength;
	}
	public void setTupleLength(double tupleLength) {
		this.tupleLength = tupleLength;
	}
	public double getTupleFileLength() {
		return tupleFileLength;
	}
	public void setTupleFileLength(double tupleFileLength) {
		this.tupleFileLength = tupleFileLength;
	}
	public double getSensorRate() {
		return sensorRate;
	}
	public void setSensorRate(double sensorRate) {
		this.sensorRate = sensorRate;
	}
	public String getSensorType() {
		return sensorType;
	}
	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}
}
