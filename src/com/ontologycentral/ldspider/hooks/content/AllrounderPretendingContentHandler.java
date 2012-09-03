package com.ontologycentral.ldspider.hooks.content;

import java.io.InputStream;
import java.net.URI;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Wraps a {@link ContentHandler} and will claim it could handle every mime
 * type.
 */
public class AllrounderPretendingContentHandler implements ContentHandler {

	ContentHandler _ch;

	/**
	 * Wraps a {@link ContentHandler} and will claim it could handle every mime
	 * type.
	 * 
	 * @param ch
	 *            the ContentHandler to be wrapped.
	 */
	public AllrounderPretendingContentHandler(ContentHandler ch) {
		_ch = ch;
	}

	/**
	 * Claims to be able to handle everything.
	 * 
	 * @return always true
	 */
	public boolean canHandle(String mime) {
		return true;
	}

	/**
	 * Delegates the handling.
	 */
	public boolean handle(URI uri, String mime, InputStream source,
			Callback callback) {
		return _ch.handle(uri, mime, source, callback);
	}

}
