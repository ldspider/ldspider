package com.ontologycentral.ldspider;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class CrawlerConstants {
	public static final String USERAGENT = "webms (http://code.google.com/p/webms/wiki/Robots)";
	public static final Header[] HEADERS = {
		new BasicHeader("Accept", "application/rdf+xml"),
		new BasicHeader("User-Agent", USERAGENT),	
	};
	
	public static final int CONNECTION_TIMEOUT = 2000;
	public static final int SOCKET_TIMEOUT = 1000;

	public static final int MAX_CONNECTIONS = 1024;
	
	public static final int RETRIES = 0;
	
	// expire pages after (ms)
	public final static long EXPIRES_MINUTE = 60*1000;
	public final static long EXPIRES_HOUR = 60*EXPIRES_MINUTE;
	public final static long EXPIRES_DAY = 24*EXPIRES_HOUR;
	public final static long EXPIRES_WEEK = 7*EXPIRES_DAY;
	
	// default values
	public static final int DEFAULT_NB_THREADS = 2;
	public static final int DEFAULT_NB_ROUNDS = 2;
}
