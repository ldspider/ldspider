package com.ontologycentral.ldspider.frontier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;

public abstract class Frontier {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	String[] _suffixes = { };
	ErrorHandler _eh;
	
	public Frontier() {
		_eh = new ErrorHandlerDummy();
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}
	
	public void setBlacklist(String[] suffixes) {
		_suffixes = suffixes;
	}
	
	URI process(URI u) {
		if (u == null || u.getScheme() == null) {
			return null;
		}
	
		if (!(u.getScheme().equals("http") || u.getScheme().equals("https"))) {
			_log.info("skipping " + u + ", " + u.getScheme() + " != http(s)");
			return null;
		}

		try {
			u = normalise(u);
		} catch (URISyntaxException e) {
			_log.info("skipping " + u +  ", not parsable");
			return null;
		}

		for (String suffix : _suffixes) {
			if (u.getPath().endsWith(suffix)) {
				_log.info("skipping " + u + ", suffix " + suffix + " blacklisted");
				_eh.handleStatus(u, 497, null, 0, -1);
				return null;
			}
		}
		
		return u;
	}
	
	public static URI normalise(URI u) throws URISyntaxException {
		String path = u.getPath();
		if (path == null || path.length() == 0) {
			path = "/";
		} else if (path.endsWith("/index.html")) {
			path = path.substring(0, path.length()-10);
		} else if (path.endsWith("/index.htm") || path.endsWith("/index.php") || path.endsWith("/index.asp")) {
			path = path.substring(0, path.length()-9);
		}

		if (u.getHost() == null) {
			throw new URISyntaxException("no host in ", u.toString());
		}

		// remove fragment
		URI norm = new URI(u.getScheme().toLowerCase(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(), null);

		return norm.normalize();
	}
	public abstract void add(URI u);
	public abstract void addAll(Collection<URI> c);
	public abstract void remove(URI u);
	public abstract void removeAll(Collection<URI> c);
	public abstract Iterator<URI> iterator();
}
