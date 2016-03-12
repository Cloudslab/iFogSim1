package org.fog.gui.core;

import java.io.Serializable;

public class Actuator extends Node implements Serializable{

	private static final long serialVersionUID = 4087896123649020073L;

	private String name;
	
	public Actuator(String name){
		super(name, "ACTUATOR");
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Actuator []";
	}
	
}
