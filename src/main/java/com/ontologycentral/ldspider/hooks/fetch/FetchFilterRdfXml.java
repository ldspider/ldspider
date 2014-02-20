package com.ontologycentral.ldspider.hooks.fetch;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class FetchFilterRdfXml implements FetchFilter {
	Logger _log = Logger.getLogger(this.getClass().getName());

	ErrorHandler _eh;
	
	public FetchFilterRdfXml() {
		;
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;	
	}
	
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if (ct != null) {
			for (String mt : CrawlerConstants.MIMETYPES) {
				if (ct.getValue().contains(mt)) {
					return true;
				}
			}
			_log.fine("ct " + u + " " + ct.getValue());
		} else {
			Exception e = new IOException("no content type available for " + u);
			_eh.handleError(u, e);
		}
		
		for (String suffix : CrawlerConstants.FILESUFFIXES) {
			if (u.getPath().endsWith(suffix)) {
				return true;
			}
		}
		
		return false;
	}
}
