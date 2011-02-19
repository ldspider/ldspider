package com.ontologycentral.ldspider.hooks.error;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;

import com.ontologycentral.ldspider.frontier.Frontier;


public class ErrorHandlerRounds extends ErrorHandlerLogger {
	Map<Node, Node> _links;

	PrintStream _out;
	int _round = 0;
	
	public ErrorHandlerRounds(PrintStream log, PrintStream vis, Callback redirects) {
		super(log, redirects);
		_out = vis;
	}
	
	public void close() {
		super.close();
		_out.close();
	}
	
	public void handleStatus(URI u, int status, Header[] headers, long duration, long contentLength) {
		super.handleStatus(u, status, headers, duration, contentLength);
		
		synchronized(this) {
			if (status == 200) {
				Resource r = new Resource(NxParser.escapeForNx(u.toString()));
				_out.print(r.toN3());
				_out.println(" .");
			}
		}
	}

	public void handleLink(Node from, Node to) {
		try {
			URI touri = new URI(to.toString());
			touri = Frontier.normalise(touri);
			to = new Resource(touri.toString());
			
			synchronized(this) {
				_out.print(from.toN3());
				_out.print(" ");
				_out.print(to.toN3());
				_out.println(" .");
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleNextRound() {
		_out.print("# next round ");
		_out.println(++_round);
	}
}
