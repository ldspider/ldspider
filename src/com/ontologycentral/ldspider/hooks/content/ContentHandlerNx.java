package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 * Handles N-TRIPLES and N-QUADS documents.
 * 
 * @author RobertIsele
 */
public class ContentHandlerNx implements ContentHandler {
	
	private final Logger _log = Logger.getLogger(this.getClass().getName());

	String[] mimeTypes = {"text/plain", "text/x-nquads" };
	
	public boolean canHandle(String mime) {
		return mime.contains("text/plain") || mime.contains("text/x-nquads");
	}

	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
//		try {
			NxParser nxp  = new NxParser(source);
			while (nxp.hasNext()) {
			callback.processStatement(nxp.next());
			}
			return true;
//		} catch (ParseException e) {
//			_log.log(Level.INFO, "Could not parse document", e);
//			return false;
//		} catch (IOException e) {
//			_log.log(Level.WARNING, "Could not read document", e);
//			return false;
//		}
	}

	public String[] getMimeTypes() {
		return mimeTypes;
	}
	
	
}
