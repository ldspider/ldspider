package com.ontologycentral.ldspider.hooks.error;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.util.NxUtil;

import com.ontologycentral.ldspider.frontier.Frontier;


public class ErrorHandlerRounds extends ErrorHandlerLogger {
	Map<Node, Node> _links;

	Appendable _out;
	int _round = 0;
	
	public ErrorHandlerRounds(Appendable log, Appendable vis, Callback redirects) {
		super(log, redirects);
		_out = vis;
	}
	
	public void close() {
		super.close();
		if (_out instanceof Closeable)
			try {
				((Closeable) _out).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public void handleStatus(URI u, int status, Header[] headers, long duration, long contentLength) {
		super.handleStatus(u, status, headers, duration, contentLength);
		
		if (status == 200) {
			Resource r = new Resource(NxUtil.escapeForNx(u.toString()));
			synchronized (this) {
				try {
					_out.append(r.toN3());
					_out.append(" .\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void handleLink(Node from, Node to) {
		try {
			URI touri = new URI(to.toString());
			touri = Frontier.normalise(touri);
			to = new Resource(touri.toString());
			
			synchronized(this) {
				_out.append(from.toN3());
				_out.append(" ");
				_out.append(to.toN3());
				_out.append(" .\n");
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleNextRound() {
		try {
			_out.append("# next round ");
			_out.append(Integer.toString(++_round));
			_out.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
