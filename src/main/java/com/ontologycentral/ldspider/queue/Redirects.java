package com.ontologycentral.ldspider.queue;

import java.io.Serializable;
import java.net.URI;

public interface Redirects extends Serializable{

	public void put(URI from, URI to);
	
	public URI getRedirect(URI from);
}