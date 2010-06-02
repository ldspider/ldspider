package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackCount;

/**
 * Communicates with an Any23 server to handle all kinds of documents.
 * 
 * @author RobertIsele
 */
public class ContentHandlerAny23 implements ContentHandler {

	/** The default endpoint of Any23 */
	public static final URI DEFAULT_ENDPOINT = URI.create("http://127.0.0.1:8080");
	
	/**
	 * Requests to Any23 can be made either using HTTP GET or using HTTP POST.
   * POST is more a efficient as Any23 does not have to make an additional GET request to the provided URI.
   * Currently GET is used, because Any23 can not resolve relative URIs for POST requests (just uses http://any23.org/tmp instead)  
   */
	public static final boolean usePOST = false;
	
	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	private final URI _any23Endpoint;
	
	private final ContentHandler _responseHandler;
	
	/**
	 * Creates a new Any23 handler using the default endpoint. 
	 */
	public ContentHandlerAny23() {
		_any23Endpoint = DEFAULT_ENDPOINT;
		_responseHandler = new ContentHandlerNx();
	}
	
	/**
	 * Creates a new Any23 handler.
	 * 
	 * @param any23Endpoint URL of the Any23 server
	 */
	public ContentHandlerAny23(URI any23Endpoint) {
		_any23Endpoint = any23Endpoint;
		_responseHandler = new ContentHandlerNx();
	}
	
	/**
	 * Checks if the configured Any23 Server is working.
	 * 
	 * @return True, if the Server is working. False, otherwise.
	 */
	public boolean checkServer() {
		CallbackCount cb = new CallbackCount();
		return handleGet(_any23Endpoint, cb) && cb.getStmts() > 0;
	}
	
	/**
	 * Determines if this content handler can handle documents of a specific MIME type.
	 * 
	 * @param mime MIME type
	 * @return Always returns true, as we will try to handle any kind of document using Any23.
	 */
	public boolean canHandle(String mime) {
		return true;
	}
	
	/**
	 * Handles a document by issuing a request to the Any23 server.
	 * 
	 * @param uri The URI of the document
	 * @param mime The MIME type of the document
	 * @param source The input document as stream
	 * @param callback The callback which will receive the extracted statements
	 * @return True, if the document has been handled. False, if the handler could not handle the document.
	 */
	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		if(usePOST)
			return handlePost(uri, mime, source, callback);
		else
			return handleGet(uri, callback);
	}
	
	/**
	 * Handles a document by issuing a HTTP GET request to the Any23 server.
	 * 
	 * @param uri The URI of the document to be parsed by the server.
	 * @param callback The callback which will receive the extracted statements
	 * @return True, if the document has been handled. False, if the handler could not handle the document.
	 */
	private boolean handleGet(URI uri, Callback callback) {
		InputStream stream = null;
		try {
			URL url = new URL(_any23Endpoint + "/nquads/" + uri);
			stream = url.openStream();
			return _responseHandler.handle(uri, "text/x-nquads", stream, callback);
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read any23 response", e);
			return false;
		}
		finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Handles a document by issuing a HTTP POST request to the Any23 server.
	 * 
	 * @param uri The URI of the document
	 * @param mime The MIME type of the document
	 * @param source The input document as stream
	 * @param callback The callback which will receive the extracted statements
	 * @return True, if the document has been handled. False, if the handler could not handle the document.
	 */
	private boolean handlePost(URI uri, String mime, InputStream source, Callback callback) {
		HttpURLConnection connection = null;
		try {
			//Open a new HTTP connection
			URL url = new URL(_any23Endpoint + "/nquads");
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
			return _responseHandler.handle(uri, "text/x-nquads", connection.getInputStream(), callback);
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
