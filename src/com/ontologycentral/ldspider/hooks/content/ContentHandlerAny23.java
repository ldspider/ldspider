package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Communicates with an Any23 server to handle all kinds of documents.
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
		HttpURLConnection connection = null;
		try {
			//Open a new HTTP connection
			URL url = new URL(_any23Endpoint + "/rdfxml");
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", mime);
			
			//Post document
			OutputStream outputStream = connection.getOutputStream();
			byte[] buffer = new byte[128];
			while(true) {
				int c = source.read(buffer);
				if(c != -1)
					outputStream.write(buffer, 0, c);
				else
					break;
			}
			outputStream.close();

			//Handle response
			return _rdfHandler.handle(uri, "application/rdf+xml", connection.getInputStream(), callback);
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not issue request to any23 for " + uri, e);
			return false;
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
	}
}
