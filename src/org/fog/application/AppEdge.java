package org.fog.application;

public class AppEdge {
	private String source;
	private String destination;
	private double tupleCpuLength;
	private double tupleNwLength;
	private String tupleType;
	private int direction;
	
	public AppEdge(){
		
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public double getTupleCpuLength() {
		return tupleCpuLength;
	}
	public void setTupleCpuLength(double tupleCpuLength) {
		this.tupleCpuLength = tupleCpuLength;
	}
	public double getTupleNwLength() {
		return tupleNwLength;
	}
	public void setTupleNwLength(double tupleNwLength) {
		this.tupleNwLength = tupleNwLength;
	}
	public String getTupleType() {
		return tupleType;
	}
	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	
}
