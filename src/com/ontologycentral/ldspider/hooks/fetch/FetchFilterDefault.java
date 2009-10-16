package com.ontologycentral.ldspider.hooks.fetch;

import java.net.URI;

import org.apache.http.HttpEntity;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class FetchFilterDefault implements FetchFilter {
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		return true;
	}
}
