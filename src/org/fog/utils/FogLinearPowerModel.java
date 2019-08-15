package org.fog.utils;

import org.cloudbus.cloudsim.power.models.PowerModel;

/**
 * The Class PowerModelLinear.
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class FogLinearPowerModel implements PowerModel {

	/** The max power. */
	private double maxPower;

	/** The constant. */
	private double constant;

	/** The static power. */
	private double staticPower;

	/**
	 * Instantiates a new linear power model.
	 * 
	 * @param maxPower the max power
	 * @param staticPower the static power
	 */
	public FogLinearPowerModel(double maxPower, double staticPower) {
		setMaxPower(maxPower);
		setStaticPower(staticPower);
		setConstant((maxPower - getStaticPower()) / 100);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.PowerModel#getPower(double)
	 */
	@Override
	public double getPower(double utilization) throws IllegalArgumentException {
		if (utilization < 0 || utilization > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		return getStaticPower() + getConstant() * utilization * 100;
	}

	/**
	 * Gets the max power.
	 * 
	 * @return the max power
	 */
	public double getMaxPower() {
		return maxPower;
	}

	/**
	 * Sets the max power.
	 * 
	 * @param maxPower the new max power
	 */
	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * Gets the constant.
	 * 
	 * @return the constant
	 */
	protected double getConstant() {
		return constant;
	}

	/**
	 * Sets the constant.
	 * 
	 * @param constant the new constant
	 */
	protected void setConstant(double constant) {
		this.constant = constant;
	}

	/**
	 * Gets the static power.
	 * 
	 * @return the static power
	 */
	public double getStaticPower() {
		return staticPower;
	}

	/**
	 * Sets the static power.
	 * 
	 * @param staticPower the new static power
	 */
	protected void setStaticPower(double staticPower) {
		this.staticPower = staticPower;
	}

}
