package org.fog.scheduler;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;

public class AppModuleScheduler extends VmSchedulerTimeShared {

	public AppModuleScheduler(List<? extends Pe> pelist) {
		super(pelist);
	}
	
	@Override
	public void deallocatePesForVm(Vm vm) {
		System.out.println(CloudSim.clock() + " : Deallocating PE for VM "+((AppModule)vm).getName());
		Exception e = new Exception();
		for (int i = 0;i<4;i++) {
			System.out.print(e.getStackTrace()[i].getClassName()+" : "+e.getStackTrace()[i].getMethodName()+", ");
		}
		System.out.println();
		// TODO Auto-generated method stub
		super.deallocatePesForVm(vm);
	}
	
	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
		// TODO Auto-generated method stub
		System.out.format("Allocating MIPS. MIPS share requested by %s = %s\n", ((AppModule)vm).getName(), mipsShareRequested.toString());
		Exception e = new Exception();
		for (int i = 0;i<4;i++) {
			System.out.print(e.getStackTrace()[i].getClassName()+" : "+e.getStackTrace()[i].getMethodName()+", ");
		}
		System.out.println();
		return super.allocatePesForVm(vm, mipsShareRequested);
	}
	
	/*public void updateAllocatedMips(List<Vm> vms) {
		deallocatePesForAllVms();
		for (Vm vm : vms) {
			if (vm.getCurrentRequestedTotalMips() > 0) {
				List<Double> mipsShare = new ArrayList<Double>();
				for (int i =0 ; i < vm.getNumberOfPes() ; i++)
					mipsShare.add(vm.getMips());
				allocatePesForVm(vm, mipsShare);
			}
		}
	}*/
	
	/*public void updateAllocatedMips(List<Vm> vms, int vmId) {
		deallocatePesForAllVms();
		for (Vm vm : vms) {
			if (vm.getCurrentRequestedTotalMips() > 0 || vm.getId() == vmId) {
				List<Double> mipsShare = new ArrayList<Double>();
				for (int i =0 ; i < vm.getNumberOfPes() ; i++)
					mipsShare.add(vm.getMips());
				allocatePesForVm(vm, mipsShare);
			}
		}
	}*/
}
