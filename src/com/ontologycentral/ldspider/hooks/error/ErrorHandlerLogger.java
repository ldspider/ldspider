package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ErrorHandlerLogger implements ErrorHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	List<Throwable> _errors;
	
	Map<Integer, Integer> _status;
	
	public ErrorHandlerLogger() {
		_errors = new ArrayList<Throwable>();
		
		_status = new HashMap<Integer, Integer>();
	}

	public void handleError(Throwable e) {
		handleError(null, e);
	}

	public synchronized void handleStatus(URI u, int status) {
		Integer count = _status.get(status);
		if (count == null) {
			_status.put(status, 1);
		} else {
			count++;
			_status.put(status, count);
		}
	}
	
	public void handleError(URI u, Throwable e) {
//		if (e.getMessage() == null) {
//			e.printStackTrace();
//		}
		_log.info(e.getMessage() + ": " + u);
		
		_errors.add(e);
	}

	public List<Throwable> getErrors() {
		return _errors;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (Map.Entry<Integer, Integer> en : _status.entrySet()) {
			sb.append(en.getKey() + ": " + en.getValue() + "\n");
		}
		
		return sb.toString();
	}
}
