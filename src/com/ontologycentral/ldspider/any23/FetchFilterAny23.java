package com.ontologycentral.ldspider.any23;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.mime.MIMEType;

import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;

public class FetchFilterAny23 implements FetchFilter {

	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if (ExtractorRegistry.getInstance().getExtractorGroup()
				.filterByMIMEType(MIMEType.parse(ct.getValue())) != null)
			return true;
		return false;

	}

}
