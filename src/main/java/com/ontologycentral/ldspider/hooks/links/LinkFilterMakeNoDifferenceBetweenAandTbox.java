package com.ontologycentral.ldspider.hooks.links;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.frontier.Frontier;

public class LinkFilterMakeNoDifferenceBetweenAandTbox extends
		LinkFilterDefault {

	public LinkFilterMakeNoDifferenceBetweenAandTbox(Frontier f) {
		super(f);
	}

	@Override
	protected synchronized void addABox(Node[] nx, int i) {
		;
	}

	@Override
	protected synchronized void addTBox(Node[] nx, int i) {
		;
	}
}
