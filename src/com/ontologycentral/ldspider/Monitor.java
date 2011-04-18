package com.ontologycentral.ldspider;

import java.io.PrintStream;
import java.util.List;

public class Monitor extends Thread {
	int _sleeptime;	
	private final List<Thread> _threads;
	PrintStream _pw;
	
	boolean _stop;
	
	public Monitor(List<Thread> threads, PrintStream pw, int sleeptime) {
		_threads = threads;
		_pw = pw;
		_stop = false;
		_sleeptime = sleeptime;
	}
	
	public void shutdown() {
		_stop = true;
		interrupt();
	}

	public void run() {
		while (_stop != true) {
			for (Thread t : _threads) {
				_pw.println(t.getName());
			}

			try {
				Thread.sleep(_sleeptime);
			} catch (InterruptedException e) {
				if (_stop == false) {
					e.printStackTrace();
				}
			}
		}
	}
}