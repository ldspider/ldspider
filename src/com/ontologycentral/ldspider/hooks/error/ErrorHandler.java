package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.List;

public interface ErrorHandler {
	public void handleError(Throwable e);
	public void handleError(URI u, Throwable e);
	public void handleStatus(URI u, int status);

	public List<Throwable> getErrors();
}
