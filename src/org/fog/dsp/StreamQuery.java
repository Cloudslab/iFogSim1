package org.fog.dsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fog.entities.Sensor;
import org.fog.entities.StreamOperator;
import org.fog.utils.GeoCoverage;
import org.fog.utils.OperatorEdge;

public class StreamQuery {

	private List<StreamOperator> operators;
	private Map<String, String> edges;
	private GeoCoverage geoCoverage;
	private String queryId;
	private List<OperatorEdge> operatorEdges;
	private List<Sensor> sensors;
	
	public StreamQuery(String queryId, List<StreamOperator> operators, Map<String, String> edges, GeoCoverage geoCoverage, List<OperatorEdge> operatorEdges){
		this.operators = operators;
		this.setOperatorEdges(operatorEdges);
		this.edges = edges;
		this.geoCoverage = geoCoverage;
		setQueryId(queryId);
		setSensors(new ArrayList<Sensor>());
	}
	
	public StreamOperator getOperatorByName(String name){
		for(StreamOperator op : operators){
			if(op.getName().equals(name))
				return op;
		}
		return null;
	}

	public boolean isAncestorOperator(String ancestor, String descendant){
		String op = descendant;
		while(op != null){
			if(op.equals(ancestor) && !op.equals(descendant))
				return true;
			op = getEdges().get(op);
		}
		return false;
	}
	
	public List<List<String>> getEndToEndPaths(){
		List<List<String>> paths = new ArrayList<List<String>>();
		for(String leaf : getLeaves()){
			List<String> path = new ArrayList<String>();
			String op = leaf;
			while(op != null){
				path.add(op);
				op = getEdges().get(op);
			}
			paths.add(path);
		}
		return paths;
	}
	
	public StreamOperator getOperatorForSensor(String sensorTye){
		for(String leaf : getLeaves()){
			if(getOperatorByName(leaf).getSensorName().equals(sensorTye))
				return getOperatorByName(leaf);
		}
		return null;
	}
	
	public double getSelectivity(String operator, String srcOperator){
		for(OperatorEdge edge : operatorEdges){
			if(edge.getSrc().equals(srcOperator) && edge.getDst().equals(operator))
				return edge.getSelectivity();
		}
		return 0;
	}
	
	public void registerSensor(Sensor sensor){
		getSensors().add(sensor);
	}
	
	public double getTupleCpuLengthOfSensor(String sensorType){
		for(Sensor sensor : getSensors()){
			if(sensorType.equals(sensor.getSensorType())){
				return sensor.getTupleCpuSize();
			}
		}
		return 0;
	}
	
	public double getTupleNwLengthOfSensor(String sensorType){
		for(Sensor sensor : getSensors()){
			if(sensorType.equals(sensor.getSensorType())){
				return sensor.getTupleNwSize();
			}
		}
		return 0;
	}
	
	public StreamOperator getNextOperator(String name){
		return getOperatorByName(edges.get(name));
	}
	
	public List<String> getLeaves(){
		List<String> leaves = new ArrayList<String>();
		for(StreamOperator op : operators){
			String opName = op.getName();
			if(!edges.containsValue(opName)){
				leaves.add(opName);
			}
		}
		return leaves;
	}
	
	public boolean isLeafOperator(String operatorName){
		if(edges.containsValue(operatorName))
			return false;
		else
			return true;
		
	}
	
	public List<String> getAllChildren(String name){
		List<String> children = new ArrayList<String>();
		for(String operatorName : edges.keySet()){
			if(edges.get(operatorName).equals(name))
				children.add(operatorName);
		}
		if(isLeafOperator(name))
			children.add("sensor-"+getOperatorByName(name).getSensorName()+"-");
		return children;
	}
	
	public String getParentOperator(String name){
		return edges.get(name);
	}
	
	public List<StreamOperator> getOperators() {
		return operators;
	}
	
	public void setOperators(List<StreamOperator> operators) {
		this.operators = operators;
	}

	public Map<String, String> getEdges() {
		return edges;
	}

	public void setEdges(Map<String, String> edges) {
		this.edges = edges;
	}

	public GeoCoverage getGeoCoverage() {
		return geoCoverage;
	}

	public void setGeoCoverage(GeoCoverage geoCoverage) {
		this.geoCoverage = geoCoverage;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public List<OperatorEdge> getOperatorEdges() {
		return operatorEdges;
	}

	public void setOperatorEdges(List<OperatorEdge> operatorEdges) {
		this.operatorEdges = operatorEdges;
	}

	public boolean isSensor(String operator) {
		if(operator.startsWith("sensor"))
			return true;
		else
			return false;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}
	
	
}
