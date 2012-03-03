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
	
	public static String[] BLACKLIST = { ".txt", ".html", ".xhtml", ".json", ".ttl", ".nt", ".jpg", ".pdf", ".htm", ".png", ".jpeg", ".gif" };

	public static String[] SITES_NO_RDF = { "wikipedia.org", "wikimedia.org", "slideshare.net", "imdb.com", "twimg.com", "dblp.uni-trier.de", "flickr.com", "amazon.com", "last.fm" };

	public static String[] SITES_SLOW = { "l3s.de", "semantictweet.com", "kaufkauf.net", "rpi.edu", "uniprot.org", "geonames.org", "dbtune" };
	public static final int SLOW_DIV = 20;

	public static int CONNECTION_TIMEOUT = 16*1000;
	public static int SOCKET_TIMEOUT = 16*1000;

	public static final int MAX_CONNECTIONS_PER_THREAD = 32;
	
	public static final int RETRIES = 0;

	public static final int MAX_REDIRECTS = 1;

	// default values
	public static final int DEFAULT_NB_THREADS = 2;
	public static final int DEFAULT_NB_ROUNDS = 2;
	public static final int DEFAULT_NB_URIS = Integer.MAX_VALUE;
	
	// avoid hammering plds
	public static final long MIN_DELAY = 2*10;
	// for bfs queue: max time after plds get re-visited
	public static final long MAX_DELAY = 2*MIN_DELAY;
	
	// close idle connections
	public static final int CLOSE_IDLE = 60000;
	
	// our status codes
	public static final int SKIP_SUFFIX = 497;
	public static final int SKIP_ROBOTS = 498;
	public static final int SKIP_MIMETYPE = 499;
}
