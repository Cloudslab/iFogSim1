package org.fog.gui.core;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cloudbus.cloudsim.sdn.graph.core.Coordinates;

/** Panel that displays a graph */
public class GraphView extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel canvas;
	private Graph graph;
	private final int ARR_SIZE = 10;

	private Image imgDefault;
	private Image imgHost;
	private Image imgSensor;
	private Image imgSwitch;
	private Image imgAppModule;
	private Image imgActuator;
	private Image imgSensorModule;
	private Image imgActuatorModule;
	
	public GraphView(final Graph graph) {

		this.graph = graph;
		
		imgHost = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/host.png"));
		imgSwitch = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/disk.png"));
		imgAppModule = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/module.png"));
		imgSensor = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/sensor.png"));
		imgActuator = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/actuator.png"));
		imgSensorModule = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/sensorModule.png"));
		imgActuatorModule = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/images/actuatorModule.png"));
		
		initComponents();
	}

	private void initComponents() {

		canvas = new JPanel() {
			
			@Override
			public void paint(Graphics g) {

				if (graph.getAdjacencyList() == null) {
					return;
				}

				Map<Node, Coordinates> coordForNodes = new HashMap<Node, Coordinates>();

				int offsetX = canvas.getWidth() / 2;
				int offsetY = canvas.getHeight() / 2;
				System.out.println("sys:"+canvas.getWidth() + ":" + canvas.getHeight());

				int height = 40;
				int width = 40;
				double angle = 2 * Math.PI / graph.getAdjacencyList().keySet().size();
				int radius = offsetY / 2 - 20;
				FontMetrics f = g.getFontMetrics();
				int nodeHeight = Math.max(height, f.getHeight());
				int nodeWidth = nodeHeight;

				int maxLevel=-1, minLevel=1000;
				Map<Integer, List<Node>> levelMap = new HashMap<Integer, List<Node>>();
				List<Node> endpoints = new ArrayList<Node>(); 
				for (Node node : graph.getAdjacencyList().keySet()) {
					if(node.getType().equals("FOG_DEVICE")){
						int level = ((FogDeviceGui)node).getLevel();
						if(!levelMap.containsKey(level))
							levelMap.put(level, new ArrayList<Node>());
						levelMap.get(level).add(node);
						
						if(level > maxLevel)
							maxLevel = level;
						if(level < minLevel)
							minLevel = level;
					} else if(node.getType().equals("SENSOR") || node.getType().equals("ACTUATOR")){
						endpoints.add(node);
					}
				}
				
				double yDist = canvas.getHeight()/(maxLevel-minLevel+3);
				int k=1;
				for(int i=minLevel;i<=maxLevel;i++, k++){
					double xDist = canvas.getWidth()/(levelMap.get(i).size()+1);
					
					for(int j=1;j<=levelMap.get(i).size();j++){
						System.out.println(levelMap);
						Node node = levelMap.get(i).get(j-1);
						int x = (int)xDist*j;
						int y = (int)yDist*k;
						coordForNodes.put(node, new Coordinates(x, y));
						node.setCoordinate(new Coordinates(x, y));
					}
				}
				
				double xDist = canvas.getWidth()/(endpoints.size()+1);
				for(int i=0;i<endpoints.size();i++){
					Node node = endpoints.get(i);
					int x = (int)xDist*(i+1);
					int y = (int)yDist*k;
					coordForNodes.put(node, new Coordinates(x, y));
					node.setCoordinate(new Coordinates(x, y));
				}
				
				int i = 0;
				for (Node node : graph.getAdjacencyList().keySet()) {
					if(node.getType().equals("FOG_DEVICE")||node.getType().equals("SENSOR")||node.getType().equals("ACTUATOR"))
						continue;
					// calculate coordinates
					int x = Double.valueOf(offsetX + Math.cos(i * angle) * radius).intValue();
					int y = Double.valueOf(offsetY + Math.sin(i * angle) * radius).intValue();
					//System.out.println(i+":"+x+"-"+y);

					coordForNodes.put(node, new Coordinates(x, y));
					node.setCoordinate(new Coordinates(x, y));
					i++;
				}

				
				
				
				Map<Node, List<Node>> drawnList = new HashMap<Node, List<Node>>();
				
				
				
				for (Entry<Node, Coordinates> entry : coordForNodes.entrySet()) {
					// first paint a single node for testing.
					g.setColor(Color.black);
					// int nodeWidth = Math.max(width, f.stringWidth(entry.getKey().getNodeText()) + width / 2);

					Coordinates wrapper = entry.getValue();
					String nodeName = entry.getKey().getName();
					switch(entry.getKey().getType()){
						case "host":
							g.drawImage(imgHost, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							break;
						case "APP_MODULE":
							g.drawImage(imgAppModule, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
						case "core":
						case "edge":
							g.drawImage(imgSwitch, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							break;
						case "FOG_DEVICE":
							g.drawImage(imgHost, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
						case "SENSOR":
							g.drawImage(imgSensor, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
						case "ACTUATOR":
							g.drawImage(imgActuator, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
						case "SENSOR_MODULE":
							g.drawImage(imgSensorModule, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
						case "ACTUATOR_MODULE":
							g.drawImage(imgActuatorModule, wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight, this);
							g.drawString(nodeName, wrapper.getX() - f.stringWidth(nodeName) / 2, wrapper.getY() + nodeHeight);
							break;
					}
					//g.setColor(Color.white);
					//g.fillOval(wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight);
					//g.setColor(Color.black);
					//g.drawOval(wrapper.getX() - nodeWidth / 2, wrapper.getY() - nodeHeight / 2, nodeWidth, nodeHeight);
					//System.out.println((wrapper.getX())+" "+(wrapper.getY()));

					//g.drawString(entry.getKey().getName(), wrapper.getX() - f.stringWidth(entry.getKey().getName()) / 2, wrapper.getY() + f.getHeight() / 2);

				}

				
				
				// draw edges first
				// TODO: we draw one edge two times at the moment because we have an undirected graph. But this
				// shouldn`t matter because we have the same edge costs and no one will see in. Perhaps refactor later.
				for (Entry<Node, List<Edge>> entry : graph.getAdjacencyList().entrySet()) {

					Coordinates startNode = coordForNodes.get(entry.getKey());

					for (Edge edge : entry.getValue()) {
/*
						// if other direction was drawn already continue
						if (drawnList.containsKey(edge.getNode()) && drawnList.get(edge.getNode()).contains(entry.getKey())) {
							continue;
						}
*/
						Coordinates targetNode = coordForNodes.get(edge.getNode());
						System.out.println("Target Node : "+edge.getNode().getName());
						g.setColor(Color.RED);
						//g.drawLine(startNode.getX(), startNode.getY(), targetNode.getX(), targetNode.getY());
						drawArrow(g, startNode.getX(), startNode.getY(), targetNode.getX(), targetNode.getY());
						// add drawn edges to the drawnList
						if (drawnList.containsKey(entry.getKey())) {
							drawnList.get(entry.getKey()).add(edge.getNode());
						} else {
							List<Node> nodes = new ArrayList<Node>();
							nodes.add(edge.getNode());
							drawnList.put(entry.getKey(), nodes);
						}

						// if (startNode.getX() - targetNode.getX() < 0) {

						// int tx = 0;
						// int ty = 0;
						// double gradient = (targetNode.getY() - startNode.getY()) /
						// (targetNode.getX() - startNode.getX());
						// LOGGER.log(Level.INFO, "Gradient: " + gradient);

						// if (startNode.getX() == targetNode.getX()) {
						// tx = targetNode.getX();
						// } else {
						// if ((startNode.getX() - targetNode.getX()) < 0) {
						// tx = targetNode.getX() - Double.valueOf((nodeHeight / 2)).intValue();
						// } else {
						// tx = targetNode.getX() + Double.valueOf((nodeHeight / 2)).intValue();
						// }
						// }
						// if (startNode.getY() == targetNode.getY()) {
						// ty = targetNode.getY();
						// } else {
						// if ((startNode.getY() - targetNode.getY()) < 0) {
						// ty = targetNode.getY() - Double.valueOf((nodeHeight / 2)).intValue();
						// } else {
						// ty = targetNode.getY() + Double.valueOf((nodeHeight / 2)).intValue();
						// }
						// }

						// drawArrow(g, startNode.getX(), startNode.getY(), tx, ty);

						// draw edge costs
						int labelX = (startNode.getX() - targetNode.getX()) / 2;
						int labelY = (startNode.getY() - targetNode.getY()) / 2;

						labelX *= -1;
						labelY *= -1;

						labelX += startNode.getX();
						labelY += startNode.getY();

						//g.setColor(Color.BLACK);
						//g.drawString(String.valueOf(edge.getInfo()), labelX - f.stringWidth(String.valueOf(edge.getInfo())) / 2, labelY + f.getHeight() / 2);
					}
				}
			}
		};
		JScrollPane scrollPane = new JScrollPane(canvas);

//		canvas.setBorder(BorderFactory.createLineBorder(Color.GREEN));
//		scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		// scrollPane.setPreferredSize(new Dimension(200, 200));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(scrollPane);
	}

	private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
		Graphics2D g = (Graphics2D) g1.create();
		System.out.println("Drawing arrow");
		double dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
		at.concatenate(AffineTransform.getRotateInstance(angle));
		g.transform(at);

		// Draw horizontal arrow starting in (0, 0)
		QuadCurve2D.Double curve = new QuadCurve2D.Double(0,0,50+0.5*len,50,len,0);
		g.draw(curve);
		g.fillPolygon(new int[] { len, len - ARR_SIZE, len - ARR_SIZE, len }, new int[] { 0, -ARR_SIZE, ARR_SIZE, 0 }, 4);
	}
	
	public void setGraph(Graph newGraph){
		this.graph = newGraph;
		/*this.graph.clearGraph();
		for (Entry<Node, List<Edge>> entry : newGraph.getAdjacencyList().entrySet()) {
			graph.addNode(entry.getKey());
			for (Edge edge : entry.getValue()) {
				graph.addEdge(entry.getKey(), edge);
			}
		}*/
	}

}
