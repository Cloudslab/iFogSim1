package org.fog.application.selectivity;

import org.cloudbus.cloudsim.core.CloudSim;

public class BurstySelectivity implements SelectivityModel{

	double burstLowPeriod;
	double burstHighPeriod;
	
	double firstHighTime;
	
	public BurstySelectivity(double burstLowPeriod, double burstHighPeriod, double firstHighTime){
		setBurstLowPeriod(burstLowPeriod);
		setBurstHighPeriod(burstHighPeriod);
		setFirstHighTime(firstHighTime);
	}
	
	@Override
	public boolean canSelect() {
		double time = CloudSim.clock() + getFirstHighTime();
		double burstPeriod = getBurstHighPeriod()+getBurstLowPeriod();
		double burstStartTime = burstPeriod*((int)(time/burstPeriod));
		if(time <= burstStartTime + getBurstHighPeriod())
			return true;
		else
			return false;
	}

	@Override
	public double getMeanRate() {
		return getBurstHighPeriod()/(getBurstHighPeriod()+getBurstLowPeriod());
	}

	@Override
	public double getMaxRate() {
		return 1;
	}

	public double getBurstLowPeriod() {
		return burstLowPeriod;
	}

	public void setBurstLowPeriod(double burstLowPeriod) {
		this.burstLowPeriod = burstLowPeriod;
	}

	public double getBurstHighPeriod() {
		return burstHighPeriod;
	}

	public void setBurstHighPeriod(double burstHighPeriod) {
		this.burstHighPeriod = burstHighPeriod;
	}

	public double getFirstHighTime() {
		return firstHighTime;
	}

	public void setFirstHighTime(double firstHighTime) {
		this.firstHighTime = firstHighTime;
	}

}
