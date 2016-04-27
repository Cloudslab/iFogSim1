package org.fog.application.selectivity;

public class FractionalSelectivity implements SelectivityModel{

	double selectivity;
	
	public FractionalSelectivity(double selectivity){
		setSelectivity(selectivity);
	}
	public double getSelectivity() {
		return selectivity;
	}
	public void setSelectivity(double selectivity) {
		this.selectivity = selectivity;
	}
	@Override
	public boolean canSelect() {
		if(Math.random() < getSelectivity())
			return true;
		return false;
	}
	@Override
	public double getMeanRate() {
		return getSelectivity();
	}
	@Override
	public double getMaxRate() {
		return getSelectivity();
	}
	
}
