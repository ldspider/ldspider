package com.ontologycentral.ldspider.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;

public class CloseIdleConnectionThread extends Thread{

	
	
	private ClientConnectionManager _cm;
	private long _st;
	private boolean _run;

	public CloseIdleConnectionThread(ClientConnectionManager cm , long sleepTime) {
		_cm = cm; 
		_st = sleepTime;
	}
	@Override
	public void run() {
		_run = true;
		while(_run){
			_cm.closeExpiredConnections();
			_cm.closeIdleConnections(0L, TimeUnit.SECONDS);
			try {
				Thread.sleep(_st);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void shutdown(){
		_run = false;
	}
}
