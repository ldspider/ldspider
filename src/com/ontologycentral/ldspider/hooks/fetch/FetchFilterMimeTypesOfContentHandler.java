package com.ontologycentral.ldspider.hooks.fetch;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * A {@link FetchFilter} that lets URIs pass whose mime types can be handled by
 * the {@link ContentHandler} specified.
 */

public class FetchFilterMimeTypesOfContentHandler implements FetchFilter {
	ContentHandler _ch;
	ErrorHandler _eh = null;

	public FetchFilterMimeTypesOfContentHandler(ContentHandler ch) {
		_ch = ch;
	}

	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if (ct != null) {
			String mime = hen.getContentType().getValue();
			if (_ch.canHandle(mime))
				return true;
			else
				return false;
		} else {
			if (_eh != null)
				_eh.handleError(u, new IOException(
						"no content type available for " + u));
			return false;
		}

	}

	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}

}
