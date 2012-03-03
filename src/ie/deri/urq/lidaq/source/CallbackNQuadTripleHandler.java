package ie.deri.urq.lidaq.source;

import org.deri.any23.extractor.ExtractionContext;
import org.deri.any23.writer.TripleHandler;
import org.deri.any23.writer.TripleHandlerException;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * @author Juergen Umbrich (firstname.lastname@deri.org)
 * @author Tobias Kaefer
 * @date Jul 19, 2011
 */
public class CallbackNQuadTripleHandler implements TripleHandler {

	private Callback cb;

	/**
	 * @param callback
	 */
	public CallbackNQuadTripleHandler(Callback callback) {
		cb= callback;
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#close()
	 */
	public void close() throws TripleHandlerException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#closeContext(org.deri.any23.extractor.ExtractionContext)
	 */
	public void closeContext(ExtractionContext arg0)
			throws TripleHandlerException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.deri.any23.writer.TripleHandler#endDocument(org.openrdf.model.URI)
	 */
	public void endDocument(URI arg0) throws TripleHandlerException {;}

	public void openContext(ExtractionContext arg0)
			throws TripleHandlerException {;}

	public void receiveNamespace(String arg0, String arg1,
			ExtractionContext arg2) throws TripleHandlerException {;}

	public void receiveTriple(Resource arg0, URI arg1, Value arg2, URI arg3,
			ExtractionContext arg4) throws TripleHandlerException {
		Node[] nx = {convert(arg0),convert(arg1),convert(arg2),convert(arg4.getDocumentURI())};
		cb.processStatement(nx);
	}

	private org.semanticweb.yars.nx.Node convert(Value arg0) {
		if(arg0 instanceof Resource)
			return new org.semanticweb.yars.nx.Resource(arg0.stringValue());
		else if(arg0 instanceof BNode)
			return new org.semanticweb.yars.nx.BNode(arg0.stringValue());
		else if(arg0 instanceof Literal)
			return new org.semanticweb.yars.nx.Literal(NxUtil.escapeForNx(arg0.stringValue()));
		else return null;
	}

	public void setContentLength(long arg0) {;}

	public void startDocument(URI arg0) throws TripleHandlerException {;}
}
