package com.ontologycentral.ldspider.hooks.content;

import java.io.InputStream;
import java.net.URI;

import org.semanticweb.yars.nx.parser.Callback;

/**
 * Combines multiples handlers into one.
 * 
 * @author RobertIsele
 */
public class ContentHandlers implements ContentHandler {

	private final ContentHandler[] _handlers;

	public ContentHandlers(ContentHandler... handlers) {
		_handlers = handlers;
	}

	public boolean canHandle(String mime) {
		for(ContentHandler handler : _handlers) {
			if(handler.canHandle(mime)) return true;
		}
		return false;
	}

	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		for(ContentHandler handler : _handlers) {
			if(handler.canHandle(mime) && handler.handle(uri, mime, source, callback)) {
				return true;
			}
		}
		return false;
	}
}
