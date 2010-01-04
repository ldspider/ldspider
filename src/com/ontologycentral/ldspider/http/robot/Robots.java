package com.ontologycentral.ldspider.http.robot;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.http.ConnectionManager;


/**
 * 
 * @author andhar
 *
 */
		
public class Robots {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Map<String, Robot> _robots;

	private ConnectionManager _cm;
	private ErrorHandler _eh;

	
	public Robots(ConnectionManager cm, ErrorHandler eh) {
		_cm = cm;
		_robots = Collections.synchronizedMap(new HashMap<String, Robot>());
		_eh = eh;
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}

    public boolean accessOk(URI uri) {
    	String host = uri.getAuthority();

		Robot r = null;

		if (_robots.containsKey(host)) {
			r = _robots.get(host);
    	} else {
    		r = new Robot(_cm, _eh, host);
    			
    		_robots.put(host, r);
    	}
    	
    	return r.isUriAllowed(uri);
    }
}