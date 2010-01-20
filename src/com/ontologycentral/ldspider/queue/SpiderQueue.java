package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;



public abstract class SpiderQueue {
	Logger _log = Logger.getLogger(this.getClass().getName());

	public abstract void schedule();
//	public abstract boolean addFrontier(URI u);
	public abstract URI poll();
	public abstract void setRedirect(URI from, URI to, int status);
	public abstract int size();
	
	String[] _blacklist = { ".txt", ".html", ".jpg", ".pdf", ".htm", ".png", ".jpeg", ".gif" };
	
	/**
	 * Add URI to frontier
	 * 
	 * @param u
	 */
	public boolean addFrontier(URI u) {
		if (u == null || u.getScheme() == null) {
			return false;
		}
		
		if (!(u.getScheme().equals("http"))) {
			_log.info(u.getScheme() + " != http, skipping " + u);
			return false;
		}
		
		try {
			u = normalise(u);
		} catch (URISyntaxException e) {
			_log.info(u +  " not parsable, skipping " + u);
			return false;
		}
		
		for (String suffix : _blacklist) {
			if (u.getPath().endsWith(suffix)) {
				return false;
			}
		}

		return true;
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
}
