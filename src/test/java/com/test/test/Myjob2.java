package com.test.test;


import org.apache.log4j.Logger;

public class Myjob2 {
	private Logger logger = Logger.getLogger(getClass());
	
	private static int counter = 0;
	
	public void work() {
		logger.info("定时任务二："+counter++);
    }
}
