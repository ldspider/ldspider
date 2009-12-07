package com.ontologycentral.ldspider.hooks.fetch;

import java.net.URI;

import org.apache.http.HttpEntity;

public class FetchFilterAllow implements FetchFilter {
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		return false;
	}
}
