package com.ontologycentral.ldspider.http;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NumericLiteral;
import org.semanticweb.yars.nx.Quad;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.util.Callbacks;

public class Headers {
	final static String httpNS = "http://www.w3.org/2006/http#";

	public static final Resource HEADERINFO = new Resource("http://code.google.com/p/ldspider/ns#headerInfo");

	static final String[] HEADERFIELDS = {
		"Accept",
		"Accept-Charset",
	    "Accept-Encoding",
	    "Connection",
	    "Content-Encoding",
	    "Content-Length",
	    "Content-Location",
	    "Content-Type",
	    "Date",
	    "ETag",
	    "Host",
	    "Last-Modified",
	    "Location",
	    "MIME-Version",
	    "Server",
	    "Accept",
	    "Content-Base",
	    "Link",
	    "Expires"
    };
	
	static final Resource[] PRED_HEADERS = {
		new Resource(httpNS+"accept"),
		new Resource(httpNS+"accept-charset"),
		new Resource(httpNS+"accept-encoding"),
		new Resource(httpNS+"connection"),
		new Resource(httpNS+"content-encoding"),
		new Resource(httpNS+"content-length"),
		new Resource(httpNS+"content-location"),
		new Resource(httpNS+"content-type"),
		new Resource(httpNS+"date"),
		new Resource(httpNS+"etag"),
		new Resource(httpNS+"host"),
		new Resource(httpNS+"last-modified"),
		new Resource(httpNS+"location"),
		new Resource(httpNS+"mime-version"),
		new Resource(httpNS+"server"),
		new Resource(httpNS+"accept"),
		new Resource(httpNS+"content-base"),
		new Resource(httpNS+"link"),
		new Resource(httpNS+"expires")		
	};
	
	public static Map<String, Resource> HEADER_MAP = null;

	public Headers(URI uri, int status, Header[] headerFields, Callbacks cbs) {
		if (HEADER_MAP == null) {
			HEADER_MAP = new HashMap<String, Resource>();

			for (int i = 0 ; i < HEADERFIELDS.length; i++) {
				String h = HEADERFIELDS[i];
				HEADER_MAP.put(h, PRED_HEADERS[i]);
			}			
		}
		
		BNode bNode = new BNode("header" + uri.hashCode()+ "-" + System.currentTimeMillis());
		
		Resource ruri = new Resource(uri.toString());
		
		cbs.processStatement(new Node[] { ruri, HEADERINFO, bNode, ruri } );
		cbs.processStatement(new Node[] { bNode,
				           new Resource(httpNS+"responseCode"),
				           new NumericLiteral(new Integer(status)),
				           ruri } );
		
		for (int i = 0; i < headerFields.length; i++) {
			if (HEADER_MAP.containsKey(headerFields[i].getName())) {
				cbs.processStatement(new Node[] {
						bNode, HEADER_MAP.get(headerFields[i].getName()), new Literal(Literal.escapeForNx(headerFields[i].getValue())), ruri
					});
			}
		}		
	}
}
