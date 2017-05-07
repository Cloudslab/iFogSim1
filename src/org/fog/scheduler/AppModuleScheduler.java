package org.fog.scheduler;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;

public class AppModuleScheduler extends VmSchedulerTimeShared {

	public AppModuleScheduler(List<? extends Pe> pelist) {
		super(pelist);
	}
	
	@Override
	public void deallocatePesForVm(Vm vm) {
		/*System.out.println(CloudSim.clock() + " : Deallocating PE for VM "+((AppModule)vm).getName());
		Exception e = new Exception();
		for (int i = 0;i<4;i++) {
			System.out.print(e.getStackTrace()[i].getClassName()+" : "+e.getStackTrace()[i].getMethodName()+", ");
		}
		System.out.println();*/
		super.deallocatePesForVm(vm);
	}
	
	@Override
	public boolean allocatePesForVm(Vm vm, List<Double> mipsShareRequested) {
		return super.allocatePesForVm(vm, mipsShareRequested);
	}
}
