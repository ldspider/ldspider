package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.Iterator;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;

public interface ErrorHandler {
	public void handleError(URI u, Throwable e);
	//public void handleStatus(URI u, int status, String type, long duration, long contentLength);
	public void handleStatus(URI u, int status, Header[] headers, long duration, long contentLength);
	public void handleRedirect(URI from, URI to, int status);
	public void handleLink(Node from, Node to);
	public void handleNextRound();
	public long lookups();
	public void close();
	public Iterator<ObjectThrowable> iterator();
}
