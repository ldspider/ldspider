package com.ontologycentral.ldspider.lookup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;

public class Redirects {
	Logger _log = Logger.getLogger(this.getClass().getName());

	Map<URI, URI> _map;
	
	public Redirects() {
		_map = Collections.synchronizedMap(new Hashtable<URI, URI>());
	}
	
	public Redirects(Iterator<Node[]> nxp) {
		this();
		
		_log.info("reading redirects file...");
		
		while (nxp.hasNext()) {
			Node[] nx = nxp.next();
			
			try {
				URI f = new URI(nx[0].toString());
				URI t = new URI(nx[1].toString());

				_map.put(f, t);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void put(URI from, URI to) {
		_map.put(from, to);
	}
	
	public URI getRedir(URI from) {
		if (from.getFragment() != null) {
			try {
				URI to = new URI(from.getScheme(), null, from.getAuthority(), from.getPort(), from.getPath(), from.getQuery(), null);
				return to;
			} catch (URISyntaxException e) {
				_log.info(e.getMessage() + " " + from);
			}			
		}

		URI to = _map.get(from);
		if (to != null) {
			return to;
		}
		
		return from;
	}
	
	public void store(FileOutputStream fos) throws IOException {
		for (Map.Entry<URI, URI> en : _map.entrySet()) {
			fos.write(("<" + en.getKey() + "> <" + en.getValue() + "> .\n").getBytes());
		}
		
		fos.close();
	}
}
