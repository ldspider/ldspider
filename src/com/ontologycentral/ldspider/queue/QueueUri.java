package com.ontologycentral.ldspider.queue;

import java.net.URI;

public class QueueUri {
	int _redirect;
	URI _u;
	
	public QueueUri(URI u) {
		_u = u;
		_redirect = -1;
	}
	
	public QueueUri(URI u, int redirect) {
		_u = u;
		_redirect = redirect;
	}

	public int getRedirect() {
		return _redirect;
	}
	
	public int hashCode() {
		return _u.hashCode();
	}
	
	public void setURI(URI u) {
		_u = u;
	}
	
	public URI getURI() {
		return _u;
	}
	
	public boolean equals(QueueUri u) {
		return _u.equals(u.getURI());
	}
}