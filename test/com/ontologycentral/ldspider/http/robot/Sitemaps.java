package com.ontologycentral.ldspider.http.robot;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.http.ConnectionManager;


/**
 * 
 * @author andhar
 *
 */
		
public class Sitemaps {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Set<String> _seen;

	private ConnectionManager _cm;
	
    private ErrorHandler _eh;

	public Sitemaps(ConnectionManager cm) {
		_cm = cm;
		
		_eh = new ErrorHandlerDummy();
		
		_seen = Collections.synchronizedSet(new HashSet<String>());
	}	
	
    public void setErrorHandler(ErrorHandler eh) {
        _eh = eh;
    }
    
    public List<URI> getSitemapUris(URI uri) {
    	String host = uri.getAuthority();

		if (!_seen.contains(host)) {
			Sitemap s = new Sitemap(_cm, _eh, host);
    	
			_seen.add(host);
			
			return s.getURIs();
    	}
    	
		return null;
    }
}