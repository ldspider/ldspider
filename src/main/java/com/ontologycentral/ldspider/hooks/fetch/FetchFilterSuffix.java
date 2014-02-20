package com.ontologycentral.ldspider.hooks.fetch;

import java.net.URI;

import org.apache.http.HttpEntity;

public class FetchFilterSuffix implements FetchFilter {
	String[] _suffixes = { };

	public FetchFilterSuffix(String[] suffixes) {
		_suffixes = suffixes;
	}

	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		for (String suffix : _suffixes) {
			if (u.getPath().endsWith(suffix)) {
				return false;
			}
		}
		
		return true;
	}
}
