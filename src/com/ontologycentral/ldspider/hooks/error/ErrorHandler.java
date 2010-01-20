package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.Iterator;

public interface ErrorHandler {
	public void handleError(URI u, Throwable e);
	public void handleStatus(URI u, int status, String type, long duration, long contentLength);
	public void handleRedirect(URI from, URI to, int status);
	public long lookups();
	public void close();
	public Iterator<ObjectThrowable> iterator();
}
