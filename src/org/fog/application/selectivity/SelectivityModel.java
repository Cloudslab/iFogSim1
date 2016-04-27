package org.fog.application.selectivity;

public interface SelectivityModel {

	public boolean canSelect();
	
	public double getMeanRate();
	
	public double getMaxRate();
	
}
