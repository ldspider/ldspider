package com.ontologycentral.ldspider.hooks.content;

import java.net.URI;
import org.apache.http.Header;

/**
 * Holds provenance information about a dataset.
 * 
 * @author RobertIsele
 */
public class Provenance {

	private final URI uri;
	
	private final Header[] httpHeaders;

	private final int httpStatus;
	
	/**
	 * @param uri The URI of the dataset.
	 * @param httpHeaders The HTTP Headers which have been returned along with the dataset.
	 * @param httpStatus The HTTP Status which have been returned along with the dataset.
	 */
	public Provenance(URI uri, Header[] httpHeaders, int httpStatus) {
		this.uri = uri;
		this.httpHeaders = httpHeaders;
		this.httpStatus = httpStatus;
	}

	/**
	 * @return The URI of the dataset.
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * @return The HTTP Headers which have been returned along with the dataset. 
	 */
	public Header[] getHttpHeaders() {
		return httpHeaders;
	}

	/**
	 * @return The HTTP Status which have been returned along with the dataset.
	 */
	public int getHttpStatus() {
		return httpStatus;
	}
}
