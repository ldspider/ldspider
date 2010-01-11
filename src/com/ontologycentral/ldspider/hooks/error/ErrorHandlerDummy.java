package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;

public class ErrorHandlerDummy implements ErrorHandler {

	public void handleError(URI u, Throwable e) {
		;
	}
	
	public void close() {
		;
	}

	public long lookups() {
		return 0;
	}

	public void handleStatus(URI u, int status, String type, long duration,	long contentLength) {
		;
	}

	public void handleRedirect(URI from, URI to, int status) {
		;
	}
}
