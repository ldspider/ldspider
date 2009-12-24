package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.List;

public class ErrorHandlerDummy implements ErrorHandler {

	public void handleError(URI u, Throwable e) {
		;
	}

	public void handleStatus(URI u, int status) {
		;
	}
}
