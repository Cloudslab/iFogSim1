package org.fog.placement;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;
public class Controller extends SimEntity {

	public static boolean ONLY_CLOUD = false;
	public static int using_fresult = -1;
	private FileWriter output = null;
	private List<FogDevice> fogDevices;
	private List<Sensor> sensors;
	private List<Actuator> actuators;
	private String result_fpath = null;

	private Map<String, Application> applications;
	private Map<String, Integer> appLaunchDelays;
	private Map<String, ModulePlacement> appModulePlacementPolicy;

	public Controller(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators) {
		super(name);
		this.applications = new HashMap<String, Application>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setAppModulePlacementPolicy(new HashMap<String, ModulePlacement>());
		for (FogDevice fogDevice : fogDevices) {
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setActuators(actuators);
		setSensors(sensors);
		connectWithLatencies();
	}

	public Controller(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators,
			int using_fresult) {
		super(name);
		this.applications = new HashMap<String, Application>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setAppModulePlacementPolicy(new HashMap<String, ModulePlacement>());
		for (FogDevice fogDevice : fogDevices) {
			fogDevice.setControllerId(getId());
		}
		setFogDevices(fogDevices);
		setActuators(actuators);
		setSensors(sensors);
		connectWithLatencies();
		this.using_fresult = using_fresult;
	}

	private FogDevice getFogDeviceById(int id) {
		for (FogDevice fogDevice : getFogDevices()) {
			if (id == fogDevice.getId())
				return fogDevice;
		}
		return null;
	}

	public String getResult_fpath() {
		return result_fpath;
	}

	public void setResult_fpath(String result_fpath) {
		this.result_fpath = result_fpath;
	}

	private void connectWithLatencies() {
		for (FogDevice fogDevice : getFogDevices()) {
			FogDevice parent = getFogDeviceById(fogDevice.getParentId());
			if (parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}

	@Override
	public void startEntity() {
		for (String appId : applications.keySet()) {
			Log.printLine(appId);
			if (getAppLaunchDelays().get(appId) == 0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);

		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);

		for (FogDevice dev : getFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

	}

	private void processStop() {
		CloudSim.stopSimulation();
//		if(this.using_fresult == 1) {
		if (false) {
			makeResultOutput();
		} else {
			printTimeDetails();
			printPowerDetails();
//			printCostDetails();
//			printNetworkUsageDetails();
		}
		System.out.println("Simulation end!");
		CloudSim.stopSimulation();
		CloudSim.terminateSimulation();
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.STOP_SIMULATION:
			processStop();
			break;

		}
	}

	private void printNetworkUsageDetails() {
		System.out
				.println("Total network usage = " + NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME);
	}

	private void makeResultOutput() {
		try {
			output = new FileWriter(this.result_fpath);
		} catch (IOException e) {
			Log.printLine("failed to open result file.\n");
			System.exit(1);
		}
		try {
			ArrayList<String> data = new ArrayList<String>();
			output.append("execution_time");
			String temp = Double.toString(
					Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime());
			data.add(temp);
			output.append(",");
			for (Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()) {
				output.append("Average Latency of Control Loop : " + getStringForLoopId(loopId).replace(",", "_"));
				data.add(Double.toString(TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId)));
				output.append(",");
			}
			for (FogDevice fogDevice : getFogDevices()) {
				output.append(fogDevice.getName());
				data.add(Double.toString(fogDevice.getEnergyConsumption()));
				output.append(",");
			}
			output.append("Total_network_usage");
			data.add(Double.toString(NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME));
			output.append("");
			output.append("\n");

			// TODO : have to modify for multi time simulation
			output.append(String.join(",", data));
			output.append("\n");

			output.flush();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something wrong occured when write result file!\n");
			System.exit(1);
		}
	}

	private FogDevice getCloud() {
		for (FogDevice dev : getFogDevices())
			if (dev.getName().equals("cloud"))
				return dev;
		return null;
	}

	private void printCostDetails() {
		System.out.println("Cost of execution in cloud = " + getCloud().getTotalCost());
	}

	private void printPowerDetails() {
		double avg = 0;
		double count = 0;
		org.fog.test.perfeval.ClassInfo class_info = new org.fog.test.perfeval.ClassInfo();
		if(class_info.CLASS_NUM != 5) {
			for (FogDevice fogDevice : getFogDevices()) {
//				 System.out.println(fogDevice.getName() + " : Energy Consumed ="+fogDevice.getEnergyConsumption());
//				 System.out.println(fogDevice.getEnergyConsumption());
				if (count++ >= 2) {
					avg += fogDevice.getEnergyConsumption();
				}
			}
			System.out.println(getFogDevices().get(0).getEnergyConsumption());
			System.out.println(getFogDevices().get(1).getEnergyConsumption());
			System.out.println(avg / (count - 2));			
		}else {
			for (FogDevice fogDevice : getFogDevices()) {
//				 System.out.println(fogDevice.getName() + " : Energy Consumed ="+fogDevice.getEnergyConsumption());
//				 System.out.println(fogDevice.getEnergyConsumption());
				if (count++ >= 1) {
					avg += fogDevice.getEnergyConsumption();
				}
			}
			System.out.println(getFogDevices().get(0).getEnergyConsumption());
			System.out.println(avg / (count - 1));						
		}
	}

	private String getStringForLoopId(int loopId) {
		for (String appId : getApplications().keySet()) {
			Application app = getApplications().get(appId);
			for (AppLoop loop : app.getLoops()) {
				if (loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}

	private void printTimeDetails() {
//		System.out.println("=========================================");
//		System.out.println("============== RESULTS ==================");
//		System.out.println("=========================================");
//		System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
//		System.out.println("=========================================");
		double avg = 0;
		double count = 0;
//		System.out.println("APPLICATION LOOP DELAYS");
//		System.out.println("=========================================");
		for (Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()) {
			System.out.println(getStringForLoopId(loopId) + " ---> "
					+ TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
			count++;
			avg += TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId);
		}
		System.out.println(avg / count);
//		System.out.println("=========================================");
//		System.out.println("TUPLE CPU EXECUTION DELAY");
//		System.out.println("=========================================");

		for (String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()) {
//			System.out.println(
//					tupleType + " ---> " + TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
//			System.out.println(TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}
//		System.out.println("=========================================");

	}

	protected void manageResources() {
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}

	private void processTupleFinished(SimEvent ev) {
	}

	@Override
	public void shutdownEntity() {
	}

	public void submitApplication(Application application, int delay, ModulePlacement modulePlacement) {
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
		getAppModulePlacementPolicy().put(application.getAppId(), modulePlacement);

		for (Sensor sensor : sensors) {
			sensor.setApp(getApplications().get(sensor.getAppId()));
		}
		for (Actuator ac : actuators) {
			ac.setApp(getApplications().get(ac.getAppId()));
		}

		for (AppEdge edge : application.getEdges()) {
			if (edge.getEdgeType() == AppEdge.ACTUATOR) {
				String moduleName = edge.getSource();
				for (Actuator actuator : getActuators()) {
					if (actuator.getActuatorType().equalsIgnoreCase(edge.getDestination()))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(),
								edge.getTupleType());
				}
			}
		}
	}

	public void submitApplication(Application application, ModulePlacement modulePlacement) {
		submitApplication(application, 0, modulePlacement);
	}

	private void processAppSubmit(SimEvent ev) {
		Application app = (Application) ev.getData();
		processAppSubmit(app);
	}

	private void processAppSubmit(Application application) {
		System.out.println(CloudSim.clock() + " Submitted application " + application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getApplications().put(application.getAppId(), application);

		ModulePlacement modulePlacement = getAppModulePlacementPolicy().get(application.getAppId());
		for (FogDevice fogDevice : fogDevices) {
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}

		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		for (Integer deviceId : deviceToModuleMap.keySet()) {
			for (AppModule module : deviceToModuleMap.get(deviceId)) {
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}
	}

	public List<FogDevice> getFogDevices() {
		return fogDevices;
	}

	public void setFogDevices(List<FogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, Application> getApplications() {
		return applications;
	}

	public void setApplications(Map<String, Application> applications) {
		this.applications = applications;
	}

	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		for (Sensor sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<Actuator> getActuators() {
		return actuators;
	}

	public void setActuators(List<Actuator> actuators) {
		this.actuators = actuators;
	}

	public Map<String, ModulePlacement> getAppModulePlacementPolicy() {
		return appModulePlacementPolicy;
	}

	public void setAppModulePlacementPolicy(Map<String, ModulePlacement> appModulePlacementPolicy) {
		this.appModulePlacementPolicy = appModulePlacementPolicy;
	}
}