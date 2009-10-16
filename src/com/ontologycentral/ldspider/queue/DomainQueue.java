package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DomainQueue extends ConcurrentLinkedQueue<URI> implements Delayed {
	private static final long serialVersionUID = 1L;

	static long MILLION = 1000000;

	long trigger;
	String name;
	
	DomainQueue(String name, long ms) { 
		this.name = name;
		trigger = System.nanoTime() + (ms * MILLION);
	}
	
	public int compareTo(Delayed d) {
		long i = trigger;
		long j = ((DomainQueue)d).trigger;
		int returnValue;
		if (i < j) {
			returnValue = -1;
		} else if (i > j) {
			returnValue = 1;
		} else {
			returnValue = 0;
		}
		return returnValue;
	}
	
	public boolean equals(Object other) {
		return ((DomainQueue)other).trigger == trigger;
	}
	
	public long getDelay(TimeUnit unit) {
		long n = trigger - System.nanoTime();
		return unit.convert(n, TimeUnit.NANOSECONDS);
	}
	
	public long getTriggerTime() {
		return trigger;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + " / " + String.valueOf(trigger);
	}
}