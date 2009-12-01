package com.ontologycentral.ldspider.tld;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import com.ontologycentral.ldspider.http.ConnectionManager;

public class TldManager {
    private static TldManager _tldm;

    private static Logger _log = Logger.getLogger(TldManager.class.getName());

    HashMap<String, Tld> TLDs = null;	// map of tlds to their properties

    public TldManager(InputStream is) {
	TLDs = new HashMap<String, Tld>();
	if (is != null) {
	    try {
		readList(is);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    // return the PLD for a URL, e.g. for www.oxfam.org.uk, return oxfam.org.uk
    public String getPLD(URI url) {
	if (!url.getScheme().equals("http")) {
	    _log.fine("scheme not http " + url);
	    return null;
	}
	try {
	    String host = url.getHost();
	    String[] parts = host.split("\\.");
	    if (parts.length == 2) {
		return host;
	    }
	    String tld = host.substring(host.lastIndexOf(".") + 1, host.length());
	    Tld current = TLDs.get(tld);
	    if(current == null) {
		_log.info("no host " + url);
		return null;
	    }

	    if (parts.length == 3) {
		if (current.getHasOneLvlSffxes()) {
		    if(current.getAddlTwoLvlSffxes().contains(parts[1] + "." + parts[2])) {
			return host;
		    }
		    else if (current.getExcptnlThreeLvlDomains().contains(host)) {
			return host;
		    }
		    else {
			return parts[1] + "." + parts[2];
		    }
		}

		if (current.getHasTwoLvlSffxes()) {
		    if(current.getExcptnlTwoLvlDomains().contains(parts[1] + "." + parts[2])) {
			return parts[1] + "." + parts[2];
		    }
		    else {
			return host;
		    }
		}

		if(current.getAddlTwoLvlSffxes().contains(parts[1] + "." + parts[2])) {
		    return host;
		}

		if (current.getExcptnlThreeLvlDomains().contains(host)) {
		    return host;
		}
	    }

	    host = parts[parts.length-4] + "." + parts[parts.length-3] + "." + parts[parts.length-2] + "." +
	    parts[parts.length-1];
	    parts = host.split("\\.");

	    // SPECIAL CASE : .US locality domains e.g. *.*.tx.us
	    if (parts[3].equals("us") && parts[2].length() == 2 && 
		    current.getAddlTwoLvlSffxes().contains(parts[2] + "." + parts[3])) {
		return host;
	    }

	    if (current.getHasOneLvlSffxes()) {
		if (current.getAddlThreeLvlSffxes().contains(parts[1] + "." + parts[2] + "." + parts[3])) {
		    return host;
		}
		else if(current.getAddlTwoLvlSffxes().contains(parts[2] + "." + parts[3])) {
		    return parts[1] + "." + parts[2] + "." + parts[3];
		}
		else if (current.getExcptnlThreeLvlDomains().contains(parts[1] + "." + parts[2] + "." + parts[3])) {
		    return parts[1] + "." + parts[2] + "." + parts[3];
		}
		else if (current.getAddlWildcardThreeLvlSffxes().contains(parts[2] + "." + parts[3])) {
		    return host;
		}
		else {
		    return parts[2] + "." + parts[3];
		}
	    }

	    if (current.getHasTwoLvlSffxes()) {
		if(current.getExcptnlTwoLvlDomains().contains(parts[2] + "." + parts[3])) {
		    return parts[2] + "." + parts[3];
		}
		else if (current.getAddlThreeLvlSffxes().contains(parts[1] + "." + parts[2] + "." + parts[3])) {
		    return host;
		}
		else if(current.getExcptnlThreeLvlDomains().contains(parts[1] + "." + parts[2] + "." + parts[3])) {
		    return parts[1] + "." + parts[2] + "." + parts[3];
		}
		else if (current.getAddlWildcardThreeLvlSffxes().contains(parts[2] + "." + parts[3])) {
		    return host;
		}
		else {
		    return parts[1] + "." + parts[2] + "." + parts[3];
		}
	    }

	    if(current.getAddlTwoLvlSffxes().contains(parts[2] + "." + parts[3])) {
		return parts[1] + "." + parts[2] + "." + parts[3];
	    }

	    if (current.getExcptnlThreeLvlDomains().contains(parts[1] + "." + parts[2] + "." + parts[3])) {
		return parts[1] + "." + parts[2] + "." + parts[3];
	    }
	}catch(Exception e) {
	    System.err.println("error: " + e.getMessage() + url);
	}

	return null;
    }

    // read list of rules from file
    public void readList(String file) throws IOException {
	readList(new FileInputStream(file));
    }


    private void readList(InputStream is) throws IOException {
	BufferedReader in = new BufferedReader(new InputStreamReader(is));
	String line;
	Pattern newTldP = Pattern.compile("// ([a-z][a-z]+) : .*");
	Matcher newTldM;
	Tld current = null;
	String tld = "";

	while ((line = in.readLine()) != null) {
	    if (line.trim().isEmpty()) {
		continue;
	    }

	    // if we come to a new section for a new tld
	    // e.g. "//ie : http://en.wikipedia.org/wiki/.ie"
	    if ((newTldM = newTldP.matcher(line)).matches()) {
		tld = newTldM.group(1);
		current = new Tld(tld);
		TLDs.put(tld, current);
	    }
	    else if(current!=null){
		// if line is stating that suffix can be one-level
		// e.g. "ie"
		if (line.equals(tld)) {
		    current.setHasOneLvlSffxes();
		}
		// if line is stating that any two-level suffix can be a suffix
		// e.g. "*.au"
		else if (line.equals("*." + tld)) {
		    current.setHasTwoLvlSffxes();
		}
		// if line is stating additional two-level suffix
		// e.g. "com.fr"
		else if (line.matches("[a-z0-9-]+\\." + tld)) {
		    current.addAddlTwoLvlSffx(line);
		}
		// if line is stating additional three-level suffix
		// e.g. "nsw.edu.au"
		else if (line.matches("[a-z0-9-]+\\.[a-z0-9-]+\\." + tld)) {
		    current.addAddlThreeLvlSffx(line);
		}
		// if line is stating exceptional two-level domain
		// e.g. "!bl.uk"
		else if (line.matches("![a-z0-9-]+\\." + tld)) {
		    current.addExcptnlTwoLvlDomain(line.substring(1, line.length()));
		}
		// if line is stating that for some two-levels any three-level can be a suffix
		// e.g. "*.sch.uk"
		else if (line.matches("\\*\\.[a-z0-9-]+\\." + tld)) {
		    current.addAddlWildcardThreeLvlSffx(line.substring(2, line.length()));
		}
		// if line is stating exceptional three-level domain
		// e.g. "!metro.tokyo.jp"
		else if (line.matches("![a-z0-9-]+\\.[a-z0-9-]+\\." + tld)) {
		    current.addExcptnlThreeLvlDomain(line.substring(1, line.length()));
		}
	    }
	}		
    }

    public static void init(ConnectionManager cm) throws URISyntaxException{
	URI tu = new URI("http://mxr.mozilla.org/mozilla-central/source/netwerk/dns/src/effective_tld_names.dat?raw=1");
	HttpResponse hres;
	try {
	    hres = cm.connect(tu);

	    int status = hres.getStatusLine().getStatusCode();
	    if (status == 200) {
		HttpEntity hen = hres.getEntity();
		if (hen != null) {
		    _tldm = new TldManager(hen.getContent());
		} else {
		    _log.info("hen == null?");
		    _tldm = null;
		}
	    } else {
		_log.info("status " + status + " for " + tu);
		_tldm = null;
	    }
	} catch (ClientProtocolException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}finally{
	    if(_tldm == null){
		//read from local file 
		_tldm = new TldManager(TldManager.class.getResourceAsStream("tld.dat"));
	    }
	}

    }

    public static TldManager getInstance() {
	return _tldm;
    }
}

