package com.ontologycentral.ldspider.queue;

import java.net.URI;
import java.net.URISyntaxException;



public abstract class SpiderQueue {
	public abstract void schedule(int maxuris);
	public abstract void addFrontier(URI u);
	public abstract URI poll();
	public abstract void setRedirect(URI from, URI to);
	public abstract URI obtainRedirect(URI from);

	
	public URI normalise(URI u) throws URISyntaxException {
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

		URI norm = new URI(u.getScheme().toLowerCase(),
				u.getUserInfo(), u.getHost().toLowerCase(), u.getPort(),
				path, u.getQuery(),
				u.getFragment());

		return norm.normalize();
	}
}
