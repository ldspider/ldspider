package com.ontologycentral.ldspider.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NumericLiteral;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.util.NxUtil;

public class Headers {
	
	static Logger _log = Logger
			.getLogger(Headers.class.getName());
	
	public static enum Treatment {
		INCLUDE, DUMP, DROP
	}
	
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
	
	static Map<String, Resource> HEADER_MAP = null;

	public static void processHeaders(URI uri, int status, Header[] headerFields, Callback cb) {
		if (HEADER_MAP == null) {
			HEADER_MAP = new HashMap<String, Resource>();

			for (int i = 0 ; i < HEADERFIELDS.length; i++) {
				String h = HEADERFIELDS[i];
				HEADER_MAP.put(h, PRED_HEADERS[i]);
			}			
		}
		
		BNode bNode = new BNode("header" + Math.abs(uri.hashCode()) + System.currentTimeMillis());
		
		Resource ruri;
		try {
			// decoding
			uri = new URI(
					uri.getScheme(), uri.getAuthority(), uri.getPath(), uri
					.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			_log.info("bad URI:" + uri); 
		}
		
		ruri = new Resource(NxUtil.escapeForNx(uri.toString()));

		cb.processStatement(new Node[] { ruri, HEADERINFO, bNode, ruri });
		cb.processStatement(new Node[] { bNode,
				new Resource(httpNS + "responseCode"),
				new NumericLiteral(Integer.valueOf(status)), ruri });

		for (int i = 0; i < headerFields.length; i++) {
			if (HEADER_MAP.containsKey(headerFields[i].getName())) {
				
				Node value;
				Resource predicate = HEADER_MAP.get(headerFields[i].getName());
				if (predicate.equals(HEADER_MAP.get("Content-Location"))) {
					value = new Resource(NxUtil.escapeForNx(uri.resolve(
							headerFields[i].getValue()).toString()));
				} else
					value = new Literal(NxUtil.escapeForNx(headerFields[i]
							.getValue()));
				cb.processStatement(new Node[] { bNode, predicate, value, ruri });
			}
		}
	}
}