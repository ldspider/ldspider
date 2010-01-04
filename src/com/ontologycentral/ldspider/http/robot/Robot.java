package com.ontologycentral.ldspider.http.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.http.ConnectionManager;

/**
 * 
 * @author andhar
 * @author juumb
 *
 */
		
public class Robot {
	Logger _log = Logger.getLogger(this.getClass().getName());

	String _robotstxt = null;
	String _host = null;
	
	public Robot(ConnectionManager cm, ErrorHandler eh, String host) {
		_host = host;
		
    	try {
    		URI u = new URI( "http://" + host + "/robots.txt" );
			HttpGet hget = new HttpGet(u);

			HttpResponse hres = cm.connect(hget);
			HttpEntity hen = hres.getEntity();

			int status = hres.getStatusLine().getStatusCode();
			
			if (status == HttpStatus.SC_OK) {
				if (hen != null) {
					String robotstxt = EntityUtils.toString(hen);
					if (robotstxt != null) {
						_robotstxt = filterPolicy(robotstxt);
					}
				} else {
					_log.info("HttpEntity for " + u + " is null");
				}
			} else {
				_log.info("no robots.txt for " + host);
			}
			
			if (hen != null) {
				hen.consumeContent();
				eh.handleStatus(u, status, hen.getContentLength());
			} else {
				eh.handleStatus(u, status, -1);				
			}
		} catch (URISyntaxException e) {
			_log.fine(e.getMessage());
		} catch (ClientProtocolException e) {
			_log.fine(e.getMessage());
		} catch (IOException e) {
			_log.fine(e.getMessage());
		}
	}
	
	private String filterPolicy(String robotstxt) {
		boolean parse = Boolean.FALSE;
	    if(robotstxt == null) return null;
	    StringBuffer minSB = new StringBuffer(5000);
	    
	    StringReader sr = new StringReader(robotstxt);
	    BufferedReader br = new BufferedReader(sr);
	    
		Pattern pattern = Pattern.compile("[lL][dD][sS][pP][iI][dD][eE][rR]");
		Matcher matcher = pattern.matcher(robotstxt);
		boolean mcAgent = matcher.find();
		
	    String line = null;
	    try {
	    	while ((line = br.readLine()) != null) {
	       	line = line.trim().toLowerCase();
		    // check if the rules are for all user agents or if not, if
		    // there is a rule for MultiCrawler
	    		if (line.startsWith("user-agent")) {
	    			//get the agent name
	    			line = line.substring(line.indexOf(":")+1).trim();
	       			matcher = pattern.matcher(line);
	    			if(mcAgent && line.equals("*")) {
	    				//multicrawler user agent is in the robotsTXT , so do not parse the * rules
	    				parse = false;
	    			} else if(line.equals("*")) {
	    				//no mcAgent but "*"
	    				parse = true;
	    			} else if(matcher.find()) {
	    				parse = true;
	    			} else {
	    				parse = false;
	    			}
	    		}
	    		if (parse && line.startsWith("disallow")) {
	    			minSB.append(line.substring(line.indexOf(":")+1).trim()+"\n");  
	    		}
	    	}
	    } catch (IOException e) {
	    	return null;
	    }
	    
	    return minSB.toString();
	}
	
	/**
	 * Check the uri against the robots.txt policy of the uri-domain
	 * @param uri
	 * @return boolean - true if it is allow to crawl this uri, false else
	 */
	public boolean isUriAllowed(URI u) {
		String host = u.getHost();

		try {
			//if robots.txt is empty or not available ALLOW TO CRAWL
			if (_robotstxt == null || _robotstxt.length() == 0) {
				return true;
			}

			String file = u.getPath();

			StringReader sr = new StringReader(_robotstxt);
			BufferedReader br = new BufferedReader(sr);

			String line = null;
			while ((line = br.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.length()==0) {
					return true;
				} else if (!line.startsWith("/")) {
					line = "/" + line;
				}

				if (file.startsWith(line)) {
					return true;
				}
			}
			
			br.close();
			sr.close();
		} catch (IOException e) {
			System.err.println("ROBOTS> IOException parsing robots.txt from database" + e.getMessage());
		}

		// we assume: ocurred errors or no disallow line ALLOW TO CRAWL
		return true;
	}
}