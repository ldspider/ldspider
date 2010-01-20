package com.ontologycentral.ldspider.hooks.error;

public class ObjectThrowable {
	Object _u;
	Throwable _e;
	
	public ObjectThrowable(Object u, Throwable e) {
		_u = u;
		_e = e;
	}
	
	public Object getObject() {
		return _u;
	}
	
	public Throwable getThrowable() {
		return _e;
	}
}
