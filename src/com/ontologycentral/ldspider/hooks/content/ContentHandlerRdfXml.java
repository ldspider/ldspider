package com.ontologycentral.ldspider.hooks.content;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 * Handles RDF/XML documents.
 * 
 * @author RobertIsele
 */
public class ContentHandlerRdfXml implements ContentHandler {

	private final Logger _log = Logger.getLogger(this.getClass().getName());
	
	public boolean canHandle(String mime) {
		return mime.contains("application/rdf+xml");
	}

	public boolean handle(URI uri, String mime, InputStream source, Callback callback) {
		try {
			new RDFXMLParser(source, true, true, uri.toString(), callback, new Resource(uri.toString()));
			return true;
		} catch (ParseException e) {
			_log.log(Level.INFO, "Could not parse document", e);
			return false;
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read document", e);
			return false;
		}
	}

}
