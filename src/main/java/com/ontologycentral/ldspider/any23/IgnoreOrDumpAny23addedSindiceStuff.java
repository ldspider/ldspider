package com.ontologycentral.ldspider.any23;

import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.filter.ExtractionContextBlocker;
import org.deri.any23.vocab.SINDICE;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Any23 adds triples that state e.g. the date or the size of the processed
 * file. This {@link TripleHandler} drops such triples so the output of a file
 * that has been parsed two times and has not changed in the meantime, produce
 * the same output.
 * 
 * @author "Tobias Kaefer"
 * 
 */

public class IgnoreOrDumpAny23addedSindiceStuff implements TripleHandler {

	private final ExtractionContextBlocker blocker;
	private final TripleHandler headerTripleHandler;
	private final boolean dumpHeaders;

	public IgnoreOrDumpAny23addedSindiceStuff(TripleHandler wrapped) {
		this(null, false, wrapped);
	}

	public IgnoreOrDumpAny23addedSindiceStuff(
			TripleHandler headerTripleHandler, boolean dumpHeaders,
			TripleHandler wrapped) {
		blocker = new ExtractionContextBlocker(wrapped);
		this.headerTripleHandler = headerTripleHandler;
		this.dumpHeaders = dumpHeaders;

	}

	public void startDocument(URI documentURI) throws TripleHandlerException {
		blocker.startDocument(documentURI);
		blocker.unblockDocument();
	}

	public void openContext(ExtractionContext context)
			throws TripleHandlerException {
		blocker.openContext(context);
	}

	public void receiveTriple(Resource s, URI p, Value o, URI g,
			ExtractionContext context) throws TripleHandlerException {
		if (p.stringValue().startsWith(SINDICE.NS))
			if (dumpHeaders)
				headerTripleHandler.receiveTriple(s, p, o, g, context);
			else
				return;
		else
			blocker.receiveTriple(s, p, o, g, context);
	}

	public void receiveNamespace(String prefix, String uri,
			ExtractionContext context) throws TripleHandlerException {
		blocker.receiveNamespace(prefix, uri, context);
	}

	public void closeContext(ExtractionContext context)
			throws TripleHandlerException {
		blocker.closeContext(context);
	}

	public void endDocument(URI documentURI) throws TripleHandlerException {
		blocker.endDocument(documentURI);
	}

	public void setContentLength(long contentLength) {
		blocker.setContentLength(contentLength);
	}

	public void close() throws TripleHandlerException {
		blocker.close();
	}

}
