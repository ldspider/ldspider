package com.ontologycentral.ldspider.any23;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.deri.any23.extractor.ExtractorRegistry;
import org.deri.any23.mime.MIMEType;

import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;

/**
 * A {@link FetchFilter} that lets URIs pass whose mime type can be handled by
 * any23 in general. Doesn't take any23 instances into account that have been
 * restricted in terms of extractors. Use
 * {@link com.ontologycentral.ldspider.hooks.fetch.FetchFilterMimeTypesOfContentHandler
 * FetchFilterMimeTypesOfContentHandler} if you use that feature.
 */
public class FetchFilterAny23 implements FetchFilter {
	public boolean fetchOk(URI u, int status, HttpEntity hen) {
		Header ct = hen.getContentType();
		if (ct != null)
			return !ExtractorRegistry.getInstance().getExtractorGroup()
					.filterByMIMEType(MIMEType.parse(ct.getValue())).isEmpty();
		else
			return false;

	}

}
