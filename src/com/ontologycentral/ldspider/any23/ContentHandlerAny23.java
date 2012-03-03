package com.ontologycentral.ldspider.any23;

import ie.deri.urq.lidaq.source.CallbackNQuadTripleHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deri.any23.Any23;
import org.deri.any23.extractor.html.TurtleHTMLExtractor;
import org.deri.any23.extractor.rdf.NQuadsExtractor;
import org.deri.any23.extractor.rdf.NTriplesExtractor;
import org.deri.any23.extractor.rdf.RDFXMLExtractor;
import org.deri.any23.extractor.rdf.TurtleExtractor;
import org.deri.any23.extractor.rdfa.RDFaExtractor;
import org.deri.any23.filter.IgnoreTitlesOfEmptyDocuments;
import org.deri.any23.source.ByteArrayDocumentSource;
import org.deri.any23.writer.TripleHandler;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.http.Headers;
import com.ontologycentral.ldspider.http.Headers.Treatment;

/**
 * A Content Handler that uses Any23 (not via server).
 * 
 * @author Tobias Kaefer
 * 
 */
public class ContentHandlerAny23 implements ContentHandler {

	private final Any23 runner;
	private final TripleHandler headerTripleHandler;
	private final Headers.Treatment headerTreatment;

	public static String[] getDefaultExtractorNames() {
		String[] extractorNames = { RDFaExtractor.NAME,
				RDFXMLExtractor.factory.getExtractorName(),
				TurtleExtractor.factory.getExtractorName(),
				NTriplesExtractor.factory.getExtractorName(),
				NQuadsExtractor.factory.getExtractorName(),
				TurtleHTMLExtractor.NAME };
		return extractorNames;
	}

	public ContentHandlerAny23() {
		// causes any23 to use all extractors
		this((String) null);
	}

	public ContentHandlerAny23(String... extractorNames) {
		this(null, Treatment.INCLUDE, extractorNames);
	}

	public ContentHandlerAny23(TripleHandler headerTripleHandler,
			Treatment headerTreatment, String... extractorNames) {
		this.headerTripleHandler = headerTripleHandler;
		runner = new Any23(extractorNames);
		this.headerTreatment = headerTreatment;
	}

	private final Logger _log = Logger.getLogger(this.getClass().getName());

	public boolean canHandle(String mime) {
		// mime type might be wrong, Any23 guesses it in the processing anyway.

		return true;

		// return ExtractorRegistry.getInstance().getExtractorGroup()
		// .filterByMIMEType(MIMEType.parse(mime)) != null;
	}

	public boolean handle(URI uri, String mime, InputStream source,
			Callback callback) {
		try {
			if (headerTreatment == Treatment.DROP)
			
			runner.extract(
					new ByteArrayDocumentSource(source, uri.toASCIIString(),
							mime), new IgnoreOrDumpAny23addedSindiceStuff(
							headerTripleHandler, false,
							new IgnoreAccidentalRDFaReally(
									new IgnoreTitlesOfEmptyDocuments(
											new CallbackNQuadTripleHandler(
													callback)))));
			else if (headerTreatment == Treatment.DUMP)
				runner.extract(
						new ByteArrayDocumentSource(source, uri.toASCIIString(),
								mime), new IgnoreOrDumpAny23addedSindiceStuff(
								headerTripleHandler, true,
								new IgnoreAccidentalRDFaReally(
										new IgnoreTitlesOfEmptyDocuments(
												new CallbackNQuadTripleHandler(
														callback)))));
			else // headerTreatment == Treatment.INCLUDE
				runner.extract(
						new ByteArrayDocumentSource(source,
								uri.toASCIIString(), mime),
						new IgnoreAccidentalRDFaReally(
								new IgnoreTitlesOfEmptyDocuments(
										new CallbackNQuadTripleHandler(callback))));

			return true;
		} catch (IOException e) {
			_log.log(Level.WARNING, "Could not read document " + uri, e);
			return false;
		} catch (Exception e) {
			_log.log(Level.WARNING, "Could not read document " + uri, e);
			return false;
		}
	}
}
