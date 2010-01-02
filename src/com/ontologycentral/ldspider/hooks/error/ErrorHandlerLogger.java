package com.ontologycentral.ldspider.hooks.error;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ErrorHandlerLogger implements ErrorHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	List<URIThrowable> _errors;
	
	Map<Integer, Integer> _status;
	
	BufferedWriter _logger = null;

	//[10/Oct/2000:13:55:36 -0700]
	final SimpleDateFormat APACHEDATEFORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	
	public ErrorHandlerLogger(String fname) {
		_errors = Collections.synchronizedList(new ArrayList<URIThrowable>());
		
		_status = Collections.synchronizedMap(new HashMap<Integer, Integer>());

		if (fname != null) {
			try {
				_logger = new BufferedWriter(new FileWriter(new File(fname)));
			} catch (IOException e) {
				_logger = null;
				e.printStackTrace();
			}
		}
	}

	public void handleError(Throwable e) {
		handleError(null, e);
	}

	public synchronized void handleStatus(URI u, int status, long contentLength) {
		Integer count = _status.get(status);
		if (count == null) {
			_status.put(status, 1);
		} else {
			count++;
			_status.put(status, count);
		}
		
		if (_logger != null) {
			StringBuilder sb = new StringBuilder();

			//127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
			sb.append(u.getHost());
			sb.append(" - - [");
			sb.append(getDateAsISO8601String());
			sb.append("] ");
			sb.append("\"GET ");
			sb.append(u.getPath());
			sb.append(" HTTP\" ");
			sb.append(status);
			sb.append(" ");
			sb.append(contentLength);
			sb.append("\n");

			try {
				_logger.write(sb.toString());
				_logger.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void handleError(URI u, Throwable e) {
//		if (e.getMessage() == null) {
//			e.printStackTrace();
//		}
		_log.info(e.getMessage() + ": " + u);
		
		URIThrowable ut = new URIThrowable();
		ut._u = u;
		ut._e = e;
		
		_errors.add(ut);
	}

//	public List<Throwable> getErrors() {
//		return _errors;
//	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (Map.Entry<Integer, Integer> en : _status.entrySet()) {
			sb.append(en.getKey() + ": " + en.getValue() + "\n");
		}
		
		return sb.toString();
	}
	
	public void close() {
		if(_logger != null){
			try {
				_logger.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getDateAsISO8601String(Date date) {
		String result = APACHEDATEFORMAT.format(date);
		//convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
		//- note the added colon for the Timezone
		return result;
	}

	private String getDateAsISO8601String() {
		return getDateAsISO8601String(new Date());
	}
}

class URIThrowable {
	URI _u;
	Throwable _e;
}