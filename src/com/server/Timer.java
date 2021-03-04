package com.server;

import java.util.TimerTask;

import com.interfaces.TimeoutListener;


public class Timer {
	private java.util.Timer timer;
	private TimeoutListener timeoutListener;
	private boolean timeout;
	
	public Timer() {
		super();
		this.timeout = false;
	}
	
	public void schedule(Long millis) {
		if (this.timer != null) {
			this.cancel();
			this.timer = null;
		}
		
		this.timeout = false;
		
		this.timer = new java.util.Timer();
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timeout();
			}
		}, millis);
	}
	
	private void timeout() {
		this.cancel();
		
		this.timeout = true;
		
		if (this.timeoutListener != null) {
			this.timeoutListener.timeout();
		}
	}

	public void cancel() {
		this.timer.cancel();
	}

	public boolean isTimeout() {
		return timeout;
	}
	
}
