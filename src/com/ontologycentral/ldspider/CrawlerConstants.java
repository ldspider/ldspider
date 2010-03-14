package com.ontologycentral.ldspider;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class CrawlerConstants {
	public static final String USERAGENT_NAME = "ldspider";
	public static final String USERAGENT_URI = "http://code.google.com/p/ldspider/wiki/Robots";

	public static final String USERAGENT_LINE = USERAGENT_NAME + " (" + USERAGENT_URI + ")";
	
	// http://www.w3.org/TR/owl-ref/#MIMEType
	public static final String[] MIMETYPES = { "application/rdf+xml", "application/xml" };
	public static final String[] FILESUFFIXES = { ".rdf", ".owl" };
	
	public static final Header[] HEADERS = {
		new BasicHeader("Accept", MIMETYPES[0] + ", " + MIMETYPES[1]),
		new BasicHeader("User-Agent", USERAGENT_LINE),
		new BasicHeader("Accept-Encoding", "gzip")
	};
	
	public static String[] BLACKLIST = { ".txt", ".html", ".jpg", ".pdf", ".htm", ".png", ".jpeg", ".gif" };
	
	public static final int CONNECTION_TIMEOUT = 4000;
	public static final int SOCKET_TIMEOUT = 8000;

	public static final int MAX_CONNECTIONS_PER_THREAD = 32;
	
	public static final int RETRIES = 0;

	public static final int MAX_REDIRECTS = 1;

	// default values
	public static final int DEFAULT_NB_THREADS = 2;
	public static final int DEFAULT_NB_ROUNDS = 2;
	public static final int DEFAULT_NB_URIS = Integer.MAX_VALUE;
	
	// avoid hammering plds
	public static final long MIN_DELAY = 2000;
	// should be adapted based on no of URIs in queue
	public static final long MAX_DELAY = 50*MIN_DELAY;
	
	// our status codes
	public static final int SKIP_SUFFIX = 497;
	public static final int SKIP_ROBOTS = 498;
	public static final int SKIP_MIMETYPE = 499;
}
