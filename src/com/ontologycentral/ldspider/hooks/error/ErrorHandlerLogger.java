package com.ontologycentral.ldspider.hooks.error;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.CrawlerConstants;

public class ErrorHandlerLogger implements ErrorHandler {
	Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static int RESOLUTION = 100;

	List<ObjectThrowable> _errors;

	Map<Integer, Integer> _status;
	Map<Integer, Integer> _rostatus;

	Map<String, Integer> _cache;
	Map<String, Integer> _rocache;

	Map<String, Integer> _type;
	Map<String, Integer> _rotype;

	Map<Integer, Integer> _time;
	Map<Integer, Integer> _rotime;
	
	PrintStream _logger = null;
	
	Callback _redirects = null;
	
	// XXX why redirects handling here?
	public ErrorHandlerLogger(PrintStream out, Callback redirects) {
		_logger = out;
		
		_redirects = redirects;
		
		_errors = Collections.synchronizedList(new ArrayList<ObjectThrowable>());
		
		_status = Collections.synchronizedMap(new TreeMap<Integer, Integer>());
		_rostatus = Collections.synchronizedMap(new TreeMap<Integer, Integer>());

		_cache = Collections.synchronizedMap(new TreeMap<String, Integer>());
		_rocache = Collections.synchronizedMap(new TreeMap<String, Integer>());
		
		_type = Collections.synchronizedMap(new TreeMap<String, Integer>());
		_rotype = Collections.synchronizedMap(new TreeMap<String, Integer>());
		
		_time = Collections.synchronizedMap(new TreeMap<Integer, Integer>());
		_rotime = Collections.synchronizedMap(new TreeMap<Integer, Integer>());
	}

	public void handleError(Throwable e) {
		handleError(null, e);
	}

	public void handleError(URI u, Throwable e) {
		_log.info("ERROR: " + e.getMessage() + ": " + u);

		if (e.getMessage() == null) {
			e.printStackTrace();
		}

		ObjectThrowable ut = new ObjectThrowable(u, e);

		_errors.add(ut);
	}

	public void handleStatus(URI u, int status, Header[] headers, long duration, long contentLength) {
		String type = null;
		String cache = null;

		if (headers != null) {
			for (Header h : headers) {
				String name = h.getName().toLowerCase();
				String value = h.getValue();

				if ("content-type".equals(name)) {
					type = value;
					if (type.indexOf(';') > 0) {
						type = type.substring(0, type.indexOf(';'));
					}
				} else if ("x-cache".equals(name)) {
					cache = value.substring(0, value.indexOf(' '));
				}
			}
		}

		if ("/robots.txt".equals(u.getPath())) {
			increment(_rostatus, status);
			increment(_rocache, cache);
			increment(_rotype, type);
			
			int tbracket = (int)((float)duration/(float)RESOLUTION);
			increment(_rotime, tbracket);
		} else {
			increment(_status, status);
			increment(_cache, cache);
			increment(_type, type);

			int tbracket = (int)((float)duration/(float)RESOLUTION);
			increment(_time, tbracket);
		}

		if (_logger != null) {
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
	
	void increment(Map m, Object key) {
		if (key != null) {
			Integer count = (Integer)m.get(key);
			if (count == null) {
				m.put(key, 1);
			} else {
				count++;
				m.put(key, count);
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("robots.txt lookups\n");
		sb.append(toStringBuffer(_rostatus));
		sb.append("\nrobots.txt caching\n");
		sb.append(toStringBuffer(_rocache));
		sb.append("\nrobots.txt content types\n");
		sb.append(toStringBuffer(_rotype));

		sb.append("\nlookup time\n");

		for (Map.Entry<Integer, Integer> en : _rotime.entrySet()) {
			int start = en.getKey() * RESOLUTION;
			sb.append(start + "-" + (start+(RESOLUTION-1)) + ": " + en.getValue() + "\n");
		}

		sb.append("\nlookups\n");
		sb.append(toStringBuffer(_status));
		sb.append("\ncaching\n");
		sb.append(toStringBuffer(_cache));
		sb.append("\ncontent types\n");
		sb.append(toStringBuffer(_type));

		sb.append("\nlookup time \n");

		for (Map.Entry<Integer, Integer> en : _time.entrySet()) {
			int start = en.getKey() * RESOLUTION;
			sb.append(start + "-" + (start+(RESOLUTION-1)) + ": " + en.getValue() + "\n");
		}
		
		sb.append("\n");
		
		return sb.toString();
	}
	
	public StringBuffer toStringBuffer(Map map) {
		StringBuffer sb = new StringBuffer();
		
		int sum = 0;
		for (Object o : map.entrySet()) {
			Map.Entry en = (Map.Entry)o;
			sb.append(en.getKey() + ": " + en.getValue() + "\n");
			sum += (Integer)en.getValue();
		}

		sb.append("total: ");
		sb.append(sum);
		sb.append("\n");
		
		return sb;
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

	public Iterator<ObjectThrowable> iterator() {
		return _errors.iterator();
	}

	/**
	 * return only "real" lookups, no robots.txt lookups and no filters w/o lookups
	 */
	public long lookups() {
		long size = 0;
		for (Integer status : _status.keySet()) {
			if (status != CrawlerConstants.SKIP_SUFFIX && status != CrawlerConstants.SKIP_ROBOTS) {
				size += _status.get(status);
			}
		}

		return size;
	}
}