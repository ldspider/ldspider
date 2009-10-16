package com.ontologycentral.ldspider.hooks.fetch;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class FetchFilterRdfXml implements FetchFilter {
	ErrorHandler _eh;
	public FetchFilterRdfXml(ErrorHandler eh) {
		_eh = eh;	
	}
	
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if (ct != null) {
			if (ct.getValue().contains("application/rdf+xml")) {
				return true;
			}
		} else {
			Exception e = new IOException("no content type available for " + u);
			_eh.handleError(e);
		}
		
		return false;
	}
}
