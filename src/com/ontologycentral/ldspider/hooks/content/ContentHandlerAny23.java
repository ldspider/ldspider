package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Uses an Any23 server to handle all kinds of documents.
 * 
 * @author RobertIsele
 */
public class ContentHandlerAny23 implements ContentHandler {

	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public static final String DEFAULT_ENDPOINT = "http://127.0.0.1:8080";
	
	private final String _any23Endpoint;
	
	private final ContentHandler _rdfHandler;
	
	/**
	 * Creates a new Any23 handler using the default endpoint. 
	 */
	public ContentHandlerAny23() {
		_any23Endpoint = DEFAULT_ENDPOINT;
		_rdfHandler = new ContentHandlerRdfXml();
	}
	
	/**
	 * Creates a new Any23 handler.
	 * 
	 * @param any23Endpoint URL of the Any23 server
	 */
	public ContentHandlerAny23(String any23Endpoint) {
		_any23Endpoint = any23Endpoint;
		_rdfHandler = new ContentHandlerRdfXml();
	}
	
	public boolean canHandle(String mime) {
		return true;
	}

	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		try {
			//TODO this is not yet optimal as any23 will issue an additional HTTP get on the given URI
			URL url = new URL(_any23Endpoint + "/rdfxml/" + uri);
			return _rdfHandler.handle(uri, "application/rdf+xml", url.openStream(), callback);
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read any23 response", e);
			return false;
		}
	}
}
