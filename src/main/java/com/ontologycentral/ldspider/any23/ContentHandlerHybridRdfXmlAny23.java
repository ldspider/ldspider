package com.ontologycentral.ldspider.any23;

import org.deri.any23.writer.TripleHandler;

import com.ontologycentral.ldspider.hooks.content.AllrounderPretendingContentHandler;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.content.ContentHandlers;
import com.ontologycentral.ldspider.http.Headers.Treatment;

/**
 * A content handler that uses the ContentHandlerRdfXml for RDFXML and Any23
 * otherwise.
 * 
 * @author Tobias Kaefer
 * 
 */
public class ContentHandlerHybridRdfXmlAny23 extends
		AllrounderPretendingContentHandler {

	// public ContentHandlerHybridRdfXmlAny23(
	// Collection<String> any23extractorNames) {
	// this((String[]) any23extractorNames.toArray());
	// }
	//
	// public ContentHandlerHybridRdfXmlAny23(String... any23extractorNames) {
	// this(null, Treatment.INCLUDE, any23extractorNames);
	// }
	//
	// public ContentHandlerHybridRdfXmlAny23() {
	// this(null, Treatment.INCLUDE, (String) null);
	// }

	@Deprecated
	public ContentHandlerHybridRdfXmlAny23(TripleHandler headerTripleHandler,
			Treatment headerTreatment, String... any23extractorNames) {

		super(new ContentHandlers(new ContentHandlerRdfXml(),
				new AllrounderPretendingContentHandler(new ContentHandlerAny23(
						headerTripleHandler, headerTreatment,
						any23extractorNames))));

		// _rdfXmlContentHandler = new ContentHandlerRdfXml();
		// _any23ContentHandler = new ContentHandlerAny23(headerTripleHandler,
		// headerTreatment, any23extractorNames);
	}

	// public ContentHandlerHybridRdfXmlAny23(TripleHandler headerTripleHandler,
	// Treatment headerTreatment, Collection<String> any23extractorNames) {
	// _rdfXmlContentHandler = new ContentHandlerRdfXml();
	// _any23ContentHandler = new ContentHandlerAny23(headerTripleHandler,
	// headerTreatment, (String[]) any23extractorNames.toArray());
	// }

	// public ContentHandlerHybridRdfXmlAny23(TripleHandler headerTripleHandler,
	// Treatment headerTreatment) {
	// this(headerTripleHandler, headerTreatment, (String) null);
	// }

	// public boolean canHandle(String mime) {
	// // The any23 content handler returns always true so there is maybe no
	// // point in this evaluation, but I keep it if somebody changes the any23
	// // contenthandler.
	// if (_rdfXmlContentHandler.canHandle(mime))
	// return true;
	// else if (_any23ContentHandler.canHandle(mime))
	// return true;
	// else
	// return false;
	// }

	// public boolean handle(URI uri, String mime, InputStream source,
	// Callback callback) {
	// if (_rdfXmlContentHandler.canHandle(mime))
	// return _rdfXmlContentHandler.handle(uri, mime, source, callback);
	// else
	// return _any23ContentHandler.handle(uri, mime, source, callback);
	// }

}
