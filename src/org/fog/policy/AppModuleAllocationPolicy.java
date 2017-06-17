/*
 * Title:        iFogSim Toolkit
 * Description:  iFogSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 *
 */
package org.fog.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;

/**
 * Allocation policy for application modules on a fog device.
 * @author Harshit Gupta
 */
public class AppModuleAllocationPolicy extends VmAllocationPolicy{

	/**
	 * The physical machine in the fog device.
	 * Every fog device is modeled as a datacenter with a single host.
	 */
	private Host fogHost;
	
	/**
	 * IDs of application modules running on the fog device. 
	 * ID of an app module is actually the ID of a VM, since AppModule class extends Vm.
	 */
	private List<Integer> appModuleIds;
	
	public AppModuleAllocationPolicy(List<? extends Host> list) {
		super(list);
		if(list.size()==1)
			fogHost = list.get(0);
		appModuleIds = new ArrayList<Integer>();
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = fogHost; // since fog device has only one physical machine
		boolean result = host.vmCreate(vm); // try allocating resources for the VM in given host
		if (result) { // if vm were succesfully created in the host
			getAppModuleIdsIds().add(vm.getId());
		}
		return result;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		boolean result = host.vmCreate(vm);
		if (result) { // if vm were succesfully created in the host
			getAppModuleIdsIds().add(vm.getId());
		}
		
		return result;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(
			List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		if (fogHost != null) {
			fogHost.vmDestroy(vm);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return fogHost;
	}

	@Override
	public Host getHost(int vmId, int userId) {
			return fogHost;
	}

	public Host getFogHost() {
		return fogHost;
	}

	public void setFogHost(Host fogHost) {
		this.fogHost = fogHost;
	}

	public List<Integer> getAppModuleIdsIds() {
		return appModuleIds;
	}

	public void setAppModuleIds(List<Integer> appModuleIds) {
		this.appModuleIds = appModuleIds;
	}

}
