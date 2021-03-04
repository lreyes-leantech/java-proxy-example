package com.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
	private static ThreadPoolManager instance;
	
	private ExecutorService executorService;
	
	public void execute(Runnable runnable) {
		this.executorService.execute(runnable);
	}
	
	public static ThreadPoolManager getInstance() {
		if (ThreadPoolManager.instance == null) {
			ThreadPoolManager.instance = new ThreadPoolManager();
		}
		
		return ThreadPoolManager.instance;
	}
	
	private ThreadPoolManager() {
		super();
		this.executorService = Executors.newFixedThreadPool(1000);
	}
}

