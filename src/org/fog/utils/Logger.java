package org.fog.utils;

import org.cloudbus.cloudsim.core.CloudSim;

public class Logger {
	
	public static final int ERROR = 1;
	public static final int DEBUG = 0;
	
	public static int LOG_LEVEL = Logger.DEBUG;
	
	public static void setLogLevel(int level){
		Logger.LOG_LEVEL = level;
	}
	
	public static void debug(String name, String message){
		if(Logger.LOG_LEVEL >= Logger.DEBUG)
			System.out.println(CloudSim.clock()+" : "+name+" : "+message);
	}
	public static void error(String name, String message){
		if(Logger.LOG_LEVEL >= Logger.ERROR)
			System.out.println(CloudSim.clock()+" : "+name+" : "+message);
	}
	
}
