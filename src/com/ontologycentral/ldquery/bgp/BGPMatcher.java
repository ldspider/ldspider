package com.ontologycentral.ldquery.bgp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

public class BGPMatcher {
	Node[] _bgp;
	Triple _t;
	int _var = 0;

	public BGPMatcher(BasicPattern bp) {
		_t = bp.get(0);
		
		_bgp = new Node[3];
				
		if (_t.getSubject().isURI()) {
			_bgp[0] = new Resource(_t.getSubject().getURI());
		} else if (_t.getSubject().isVariable()) {
			_bgp[0] = new Variable("v"+_var++);
		}
		
		if (_t.getPredicate().isURI()) {
			_bgp[1] = new Resource(_t.getPredicate().getURI());
		} else if (_t.getPredicate().isVariable()) {
			_bgp[1] = new Variable("v"+_var++);
		}
		
		if (_t.getObject().isURI()) {
			_bgp[2] = new Resource(_t.getObject().getURI());
		} else if (_t.getObject().isVariable()) {
			_bgp[2] = new Variable("v"+_var++);
		}
	}
	
	public List<Binding> filter(Set<Node[]> stmtSet) {
        List<Binding> results = new ArrayList<Binding>() ;

		for(Node[] stmt: stmtSet) {
			if (match(stmt)) {
				Binding b = new BindingMap();

				if (_t.getPredicate().isVariable()) {
					Var v = Var.alloc(_t.getPredicate().getName());
					b.add(v, com.hp.hpl.jena.graph.Node.createURI(((Resource)stmt[1]).toString()));
				}
				
				if (_t.getObject().isVariable()) {
					Var v = Var.alloc(_t.getObject().getName());
					if (stmt[2] instanceof Resource) {
						b.add(v, com.hp.hpl.jena.graph.Node.createURI(((Resource)stmt[2]).toString()));
					} else if (stmt[2] instanceof Literal) {
						b.add(v, com.hp.hpl.jena.graph.Node.createLiteral(((Literal)stmt[2]).toString()));
					} else if (stmt[2] instanceof BNode) {
						b.add(v, com.hp.hpl.jena.graph.Node.createAnon(new AnonId(((BNode)stmt[2]).toString())));
					}
				}
				
				results.add(b);
			}
		}

		return results;
	}
	
	boolean match(Node[] nx) {
		for (int i = 0; i < _bgp.length; i++) {
			Node n = _bgp[i];
			if (!(n instanceof Variable)) {
				if (!n.equals(nx[i])) {
					return false;
				}
			}
		}

		return true;
	}
}