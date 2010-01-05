package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;

public interface ErrorHandler {
	public void handleError(URI u, Throwable e);
	public void handleStatus(URI u, int status, String type, long duration, long contentLength);
	public long lookups();
	public void close();
}
