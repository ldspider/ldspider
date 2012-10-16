package com.ontologycentral.ldspider.hooks.content;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Wraps a {@link ContentHandler} and will claim it could handle every mime type
 * in the processing. However, it is "honest" when it comes to
 * {@link #getMimeTypes()}.
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

	public String[] getMimeTypes() {
		return _ch.getMimeTypes();
	}

}
