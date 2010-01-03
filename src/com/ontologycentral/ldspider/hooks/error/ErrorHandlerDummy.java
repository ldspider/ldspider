package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;

public class ErrorHandlerDummy implements ErrorHandler {

	public void handleError(URI u, Throwable e) {
		;
	}

	public void handleStatus(URI u, int status, long contentLength) {
		;
	}
	
	public void close() {
		;
	}

	public long lookups() {
		return 0;
	}
}
