package org.fog.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Consts;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.entities.Tuple;

public class TupleScheduler extends CloudletSchedulerTimeShared{

	private double mips;
	private int numPes;
	
	public TupleScheduler(double mips, int numberOfPes) {
		//super(mips, numberOfPes);
		super();
		setMips(mips);
		setNumPes(numberOfPes);
	}
	
	@Override
	public List<Double> getCurrentRequestedMips() {
		// TODO Auto-generated method stub
		if (getCloudletExecList().size() > 0) {
			List<Double> mipsShare = new ArrayList<Double>();
			for(int i=0;i<getNumPes();i++) 
				mipsShare.add(getMips());
			return mipsShare;
		} else {
			return new ArrayList<Double>(); 
		}
	}
	
	/*@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
		setCurrentMipsShare(mipsShare);
		double timeSpam = currentTime - getPreviousTime();

		for (ResCloudlet rcl : getCloudletExecList()) {
			rcl.updateCloudletFinishedSoFar((long) (getCapacity(mipsShare) * timeSpam * rcl.getNumberOfPes() * Consts.MILLION));
		}

		if (getCloudletExecList().size() == 0) {
			setPreviousTime(currentTime);
			return 0.0;
		}

		// check finished cloudlets
		double nextEvent = Double.MAX_VALUE;
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			long remainingLength = rcl.getRemainingCloudletLength();
			Tuple tuple = (Tuple) rcl.getCloudlet();
			System.out.println(CloudSim.clock() + " : remaining length of " + tuple.getTupleType() + "= "+remainingLength);
			if (remainingLength == 0) {// finished: remove from the list
				toRemove.add(rcl);
				cloudletFinish(rcl);
				continue;
			}
		}
		getCloudletExecList().removeAll(toRemove);

		// estimate finish time of cloudlets
		for (ResCloudlet rcl : getCloudletExecList()) {
			double estimatedFinishTime = currentTime
					+ (rcl.getRemainingCloudletLength() / (getCapacity(mipsShare) * rcl.getNumberOfPes()));
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}

			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}

		setPreviousTime(currentTime);
		return nextEvent;
	}*/

	
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		ResCloudlet rcl = new ResCloudlet(cloudlet);
		rcl.setCloudletStatus(Cloudlet.INEXEC);
		for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
			rcl.setMachineAndPeId(0, i);
		}

		getCloudletExecList().add(rcl);

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		List<Double> mipsShare = new ArrayList<Double>();
		for(int i=0;i<getNumPes();i++) 
			mipsShare.add(getMips());
		setCurrentMipsShare(mipsShare);
		double extraSize = getCapacity(getCurrentMipsShare()) * fileTransferTime;
		long length = (long) (cloudlet.getCloudletLength() + extraSize);
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / getCapacity(getCurrentMipsShare());
	}

	public double getMips() {
		return mips;
	}

	public void setMips(double mips) {
		this.mips = mips;
	}

	public void setNumPes(int numPes) {
		this.numPes = numPes;
	}
	
	public int getNumPes() {
		return numPes;
	}
}
