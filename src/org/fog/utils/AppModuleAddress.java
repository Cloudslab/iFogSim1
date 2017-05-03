package org.fog.utils;

import org.apache.commons.math3.util.Pair;

public class AppModuleAddress {
	private int fogDeviceId;
	private int vmId;
	
	public AppModuleAddress(int vmId, int fogDeviceId) {
		setVmId(vmId);
		setFogDeviceId(fogDeviceId);
	}
	
	public Pair<Integer, Integer> getAddress() {
		return new Pair<Integer, Integer>(getVmId(), getFogDeviceId());
	}
	
	public int getFogDeviceId() {
		return fogDeviceId;
	}
	public void setFogDeviceId(int fogDeviceId) {
		this.fogDeviceId = fogDeviceId;
	}
	public int getVmId() {
		return vmId;
	}
	public void setVmId(int vmId) {
		this.vmId = vmId;
	}
}
