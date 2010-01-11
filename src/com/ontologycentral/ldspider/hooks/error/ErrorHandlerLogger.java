package com.ontologycentral.ldspider.hooks.error;

import java.io.PrintStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

public class ErrorHandlerLogger implements ErrorHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());

	List<URIThrowable> _errors;
	
	Map<Integer, Integer> _status;
	
	PrintStream _logger = null;
	
	Callback _redirects = null;

	//[10/Oct/2000:13:55:36 -0700]
	final SimpleDateFormat APACHEDATEFORMAT = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	
	public ErrorHandlerLogger(PrintStream out, Callback redirects) {
		_logger = out;
		
		_redirects = redirects;
		
		_errors = Collections.synchronizedList(new ArrayList<URIThrowable>());
		
		_status = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	}

	public void handleError(Throwable e) {
		handleError(null, e);
	}
	
	public void handleError(URI u, Throwable e) {
		_log.info("ERROR: " + e.getMessage() + ": " + u);

		if (e.getMessage() == null) {
			e.printStackTrace();
		}
		
		URIThrowable ut = new URIThrowable();
		ut._u = u;
		ut._e = e;
		
		_errors.add(ut);
	}

	public void handleStatus(URI u, int status, String type, long duration, long contentLength) {
		Integer count = _status.get(status);
		if (count == null) {
			_status.put(status, 1);
		} else {
			count++;
			_status.put(status, count);
		}
		
		if (_logger != null) {
			if (type != null && type.indexOf(';') > 0) {
				type = type.substring(0, type.indexOf(';'));
			}
			
			StringBuilder sb = new StringBuilder();

			// common.log: 127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
			// native.log: time elapsed remotehost code/status bytes method URL rfc931 peerstatus/peerhost type
			// 1262626658.480     13 127.0.0.1 TCP_HIT/200 594 GET http://umbrich.net/robots.txt - NONE/- text/plain
			sb.append(System.currentTimeMillis()/1000);
			sb.append(" ");
			sb.append(duration);
			sb.append(" 127.0.0.1 TCP_HIT/");
			sb.append(status);
			sb.append(" ");
			sb.append(contentLength);
			sb.append(" GET ");
			sb.append(u);
			sb.append(" - NONE/- ");
			sb.append(type);

			synchronized(this) {
				_logger.println(sb.toString());
				_logger.flush();
			}
		}
	}

//	public List<Throwable> getErrors() {
//		return _errors;
//	}
	
	public long lookups() {
		long size = 0;
		for (Integer i : _status.values()) {
			size += i;
		}

		return size;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (Map.Entry<Integer, Integer> en : _status.entrySet()) {
			sb.append(en.getKey() + ": " + en.getValue() + "\n");
		}
		
		return sb.toString();
	}
	
	public void close() {
		if(_logger != null) {
			_logger.close();
		}
	}

	public void handleRedirect(URI from, URI to, int status) {
		if (_redirects != null) {
			Node[] nx = new Node[2];

			nx[0] = new Resource(from.toString());
			nx[1] = new Resource(to.toString());

			_redirects.processStatement(nx);		
		}
	}
	
//	private String getDateAsISO8601String(Date date) {
//		String result = APACHEDATEFORMAT.format(date);
//		//convert YYYYMMDDTHH:mm:ss+HH00 into YYYYMMDDTHH:mm:ss+HH:00
//		//- note the added colon for the Timezone
//		return result;
//	}
//
//	private String getDateAsISO8601String() {
//		return getDateAsISO8601String(new Date());
//	}
}

class URIThrowable {
	URI _u;
	Throwable _e;
}