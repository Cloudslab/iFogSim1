package org.fog.scheduler;

import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;

public class StreamOperatorScheduler extends VmSchedulerTimeSharedOverSubscription{

	public StreamOperatorScheduler(List<? extends Pe> pelist) {
		super(pelist);
	}
}
