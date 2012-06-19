package com.ontologycentral.ldspider.http.robot;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.protocol.HTTP;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerDummy;
import com.ontologycentral.ldspider.http.ConnectionManager;


/**
 * 
 * @author andhar
 *
 */
		
public class Robots {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Map<String, Robot> _robots;

	private ConnectionManager _cm;
	
    private ErrorHandler _eh;

	public Robots(ConnectionManager cm) {
		_cm = cm;
		
		_eh = new ErrorHandlerDummy();
		
		_robots = Collections.synchronizedMap(new HashMap<String, Robot>());
	}	
	
    public void setErrorHandler(ErrorHandler eh) {
        _eh = eh;
    }
    
    public boolean accessOk(URI uri) {
    	URI hostUri;
		try {
			if (uri.getPort() < 0)
				// The URI has no port specified. The most likely case.
				hostUri = new URI(uri.getScheme(), uri.getAuthority(), null,
						null, null);
			else if ((uri.getPort() == 80 && uri.getScheme().equalsIgnoreCase(
					"http"))
					|| (uri.getPort() == 443 && uri.getScheme()
							.equalsIgnoreCase("https"))
					|| (uri.getPort() == 21 && uri.getScheme()
							.equalsIgnoreCase("ftp")))
				// The URI has a port specified which is the default for its scheme.
				hostUri = new URI(uri.getScheme(), uri.getUserInfo(),
						uri.getHost(), -1, null, null, null);
			else
				// The URI has a port specified which is to remain part of
				// authority in the caching of different instances of Robot.
				hostUri = new URI(uri.getScheme(), uri.getAuthority(), null,
						null, null);
			
		} catch (URISyntaxException e1) {
			_log.fine(e1.getMessage() + " " + uri);
			return false;
		}

		Robot r = null;

		if (_robots.containsKey(hostUri.toString())) {
			r = _robots.get(hostUri.toString());
    	} else {
    		r = new Robot(_cm, _eh, hostUri);
    			
    		_robots.put(hostUri.toString(), r);
    	}
    	
		URL url = null;
		try {
			url = uri.toURL();
		} catch (MalformedURLException e) {
			_log.info(e.getMessage() + uri);
			return false;
		}
		
    	return r.isUrlAllowed(url);
    }
}