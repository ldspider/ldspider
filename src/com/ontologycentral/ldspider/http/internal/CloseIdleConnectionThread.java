package com.ontologycentral.ldspider.http.internal;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.conn.ClientConnectionManager;

public class CloseIdleConnectionThread extends Thread{
	private final static Logger log = Logger.getLogger(CloseIdleConnectionThread.class.getSimpleName());
	
	
	private ClientConnectionManager _cm;
	private long _st;
	private boolean _run;

	public CloseIdleConnectionThread(ClientConnectionManager cm , long sleepTime) {
		_cm = cm; 
		_st = sleepTime;
		
		log.info("Initialised "+CloseIdleConnectionThread.class.getSimpleName()+" with sleepTime "+_st+" ms");
	}

	public void run() {
		log.info("Starting "+CloseIdleConnectionThread.class.getSimpleName());
		_run = true;
		
		while(_run) {
			log.info("Closing expired and idle connections");
			_cm.closeExpiredConnections();
			_cm.closeIdleConnections(0L, TimeUnit.SECONDS);

			try {
				Thread.sleep(_st);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		log.info("Stopped "+CloseIdleConnectionThread.class.getSimpleName());
	}
	
	public void shutdown() {
		_run = false;
		log.info("Stopping "+CloseIdleConnectionThread.class.getSimpleName());
		interrupt();
	}
}
