package com.ontologycentral.ldspider.hooks.content;

import java.io.InputStream;
import java.net.URI;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Handles page content and extracts rdf statements from it. 
 * 
 * @author RobertIsele
 */
public interface ContentHandler {

	/**
	 * Determines if this content handler can handle documents of a specific MIME type.
	 * 
	 * @param mime MIME type
	 * @return True, if this content handler can handle documents with the given MIME type. False, otherwise.
	 */
	boolean canHandle(String mime);
	
	/**
	 * Handles a document.
	 * 
	 * @param uri The URI of the document
	 * @param mime The MIME type of the document
	 * @param source The input document as stream
	 * @param callback The callback which will receive the extracted statements
	 * @return True, if the document has been handled. False, if the handler could not handle the document.
	 */
	boolean handle(URI uri, String mime, InputStream source, Callback callback);
}
