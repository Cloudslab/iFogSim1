package org.fog.utils.distribution;

import java.util.Random;

public abstract class Distribution {

	protected Random random;
	public abstract double getNextValue();
	
	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
}
