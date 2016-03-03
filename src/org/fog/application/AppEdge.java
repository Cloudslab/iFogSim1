package org.fog.application;

public class AppEdge {
	
	public static final int SENSOR = 1;
	public static final int ACTUATOR = 2;
	public static final int MODULE = 3;
	
	private String source;
	private String destination;
	private double tupleCpuLength;
	private double tupleNwLength;
	private String tupleType;
	private int direction;
	private int edgeType;
	
	public AppEdge(){
		
	}
	
	public AppEdge(String source, String destination, double tupleCpuLength, 
			double tupleNwLength, String tupleType, int direction, int edgeType){
		setSource(source);
		setDestination(destination);
		setTupleCpuLength(tupleCpuLength);
		setTupleNwLength(tupleNwLength);
		setTupleType(tupleType);
		setDirection(direction);
		setEdgeType(edgeType);
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

	public int getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(int edgeType) {
		this.edgeType = edgeType;
	}
}
