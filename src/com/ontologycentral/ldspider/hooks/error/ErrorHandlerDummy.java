package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.List;

public class ErrorHandlerDummy implements ErrorHandler {

	public List<Throwable> getErrors() {
		return null;
	}

	public void handleError(Throwable e) {
		;
	}

	public void handleError(URI u, Throwable e) {
		;
	}

	public void handleStatus(URI u, int status) {
		;
	}
}
