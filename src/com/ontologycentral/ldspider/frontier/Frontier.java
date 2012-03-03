package com.ontologycentral.ldspider.frontier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;

public abstract class Frontier implements Iterable<URI> {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	String[] _suffixes = { };
	ErrorHandler _eh;
	
	public Frontier() {
		_eh = new ErrorHandlerDummy();
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}
	
	URI process(URI u) {
		if (u == null || u.getScheme() == null) {
			return null;
		}
	
		if (!(u.getScheme().equalsIgnoreCase("http") || u.getScheme().equalsIgnoreCase("https"))) {
			_log.fine("skipping " + u + ", " + u.getScheme() + " != http(s)");
			return null;
		}

		try {
			u = normalise(u);
		} catch (URISyntaxException e) {
			_log.fine("skipping " + u +  ", not parsable");
			return null;
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
			throw new URISyntaxException(u.toString(), "no host in");
		}

		// remove fragment
		URI norm = new URI(u.getScheme().toLowerCase(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(), null);

		return norm.normalize();
	}
	
	public abstract void add(URI u);

	public void addAll(Collection<URI> c) {
		for (URI u : c) {
			add(u);
		}
		c = null;
	}

//	public abstract void remove(URI u);
	public abstract void removeAll(Collection<URI> c);
	public abstract void reset();
	public abstract Iterator<URI> iterator();
}
