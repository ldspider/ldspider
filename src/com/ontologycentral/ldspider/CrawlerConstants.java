package com.ontologycentral.ldspider;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class CrawlerConstants {
	public static final String USERAGENT = "webms (http://code.google.com/p/webms/wiki/Robots)";
	public static final Header[] HEADERS = {
		new BasicHeader("Accept", "application/rdf+xml"),
		new BasicHeader("User-Agent", USERAGENT),	
	};
	
	public static final int CONNECTION_TIMEOUT = 5000;
	public static final int SOCKET_TIMEOUT = 10000;

	public static final int MAX_CONNECTIONS_PER_THREAD = 32;
	
	public static final int RETRIES = 0;
	
	// default values
	public static final int DEFAULT_NB_THREADS = 2;
	public static final int DEFAULT_NB_ROUNDS = 2;
	public static final int DEFAULT_NB_URIS = Integer.MAX_VALUE;
	
	// avoid hammering plds
	public static final long DELAY = 500;
}
