package com.ontologycentral.ldspider;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDefault;


public class FetchTest {
public static void main(String[] args) throws URISyntaxException {
	List<URI> seeds = new ArrayList<URI>();
	seeds.add(new URI("http://ld2sd.deri.org/dady/publisher/test-dataset.rdf"));
	
	
	Crawler c = new Crawler(1);
	
	ErrorHandler eh = new ErrorHandlerLogger(null, null);
	c.setErrorHandler(eh);
	
	NodeCollector nc = new NodeCollector();
	c.setOutputCallback(nc);
	c.setLinkFilter(new LinkFilterDefault(eh));
	c.setFetchFilter(new FetchFilterRdfXml(eh));
	c.evaluate(seeds, 0);
	c=null;
	nc=null;
	
	c = new Crawler(1);
	
	eh = new ErrorHandlerLogger(null, null);
	c.setErrorHandler(eh);
	
	nc = new NodeCollector();
	c.setOutputCallback(nc);
	c.setLinkFilter(new LinkFilterDefault(eh));
	c.setFetchFilter(new FetchFilterRdfXml(eh));
	c.evaluate(seeds, 0);
	
	
	System.out.println(nc.getContent());
}
}

class NodeCollector implements Callback{

    
    private HashSet<Nodes> _content;

    public NodeCollector(){
	_content = new HashSet<Nodes>();
    }
    public void endDocument() {
	// TODO Auto-generated method stub
	
    }

    public void processStatement(Node[] arg0) {
	_content.add(new Nodes(arg0[0],arg0[1],arg0[2]));
	
    }

    public void startDocument() {
	// TODO Auto-generated method stub
	
    }
    
    public Set<Nodes> getContent(){
	return _content;
    }
}
