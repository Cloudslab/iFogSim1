package org.fog.policy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.fog.entities.StreamOperator;

public class StreamOperatorAllocationPolicy extends VmAllocationPolicy{

	private Host fogHost;
	
	private List<Integer> streamOperatorIds;
	
	public StreamOperatorAllocationPolicy(List<? extends Host> list) {
		super(list);
		if(list.size()==1)
			fogHost = list.get(0);
		streamOperatorIds = new ArrayList<Integer>();
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		Host host = fogHost;
		boolean result = host.vmCreate(vm);
		//System.out.println("Trying to create "+((StreamOperator)vm).getName()+" in device "+host.getDatacenter().getName()+" ---> "+result);
		if (result) { // if vm were succesfully created in the host
			getStreamOperatorIds().add(vm.getId());
		}
		
		return result;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		boolean result = host.vmCreate(vm);

		if (result) { // if vm were succesfully created in the host
			getStreamOperatorIds().add(vm.getId());
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

	public List<Integer> getStreamOperatorIds() {
		return streamOperatorIds;
	}

	public void setStreamOperatorIds(List<Integer> streamOperatorIds) {
		this.streamOperatorIds = streamOperatorIds;
	}

}
