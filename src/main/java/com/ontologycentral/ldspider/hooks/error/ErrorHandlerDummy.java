package com.ontologycentral.ldspider.hooks.error;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;

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

	public void handleRedirect(URI from, URI to, int status) {
		;
	}

	public Iterator<ObjectThrowable> iterator() {
		return new ArrayList<ObjectThrowable>().iterator();
	}

	public void handleStatus(URI u, int status, Header[] headers, long duration, long contentLength) {
		;
	}

	public void handleLink(Node from, Node to) {
		// TODO Auto-generated method stub
		
	}

	public void handleNextRound() {
		// TODO Auto-generated method stub
		
	}
}
