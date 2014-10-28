package org.deri.opencube.tarql;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.deri.opencube.tarql.ui.DelimiterSelectValueFactory;
import org.deri.opencube.tarql.ui.EncodingSelectValueFactory;
import org.deri.opencube.tarql.ui.EscapecharSelectValueFactory;
import org.deri.opencube.tarql.ui.HeaderrowSelectValueFactory;
import org.deri.opencube.tarql.ui.QuotecharSelectValueFactory;
import org.deri.tarql.CSVOptions;
import org.deri.tarql.TarqlParser;
import org.deri.tarql.TarqlQuery;
import org.deri.tarql.TarqlQueryExecution;
import org.deri.tarql.TarqlQueryExecutionFactory;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.model.ParameterConfigDoc.Type;
import com.fluidops.iwb.provider.AbstractFlexProvider;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;


public class TarqlProvider extends AbstractFlexProvider<TarqlProvider.Config> {
	private static final long serialVersionUID = 1L;

	public static class Config implements Serializable {

		private static final long serialVersionUID = 1L;

		private static final String SAMPLE_TARQL = "PREFIX qb:<http://purl.org/linked-data/cube#> \n" +
				"PREFIX ex: <http://example.com/> \n" +
				"CONSTRUCT { ?uri a qb:Observation; <http://example.com/refArea> ?dim1val; <http://example.com/dim2> ?dim2val; <http://example.com/obsValue> ?e . } WHERE { " +
				" BIND (URI(CONCAT('http://example.com/ns#', ?a)) AS ?uri)\n" +
				" BIND (URI(CONCAT('http://example.com/ns#', ?b)) AS ?dim1val)\n" +
				" BIND (URI(CONCAT('http://example.com/ns#', ?d)) AS ?dim2val)\n" +
				"}";

		@ParameterConfigDoc(desc = "URL of the input CSV file", type=Type.FILEEDITOR)
		public String csvFileLocation;

		@ParameterConfigDoc(
				desc = "Delimiting character of the CSV file",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = DelimiterSelectValueFactory.class)
		public String delimiter;

		@ParameterConfigDoc(
				desc = "CSV file encoding",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = EncodingSelectValueFactory.class)
		public String encoding;

		@ParameterConfigDoc(
				desc = "Character used to escape quotes in the CSV file",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = EscapecharSelectValueFactory.class)
		public String escapeChar;

		@ParameterConfigDoc(
				desc = "Quote character used in the CSV file",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = QuotecharSelectValueFactory.class)
		public String quoteChar;		

		@ParameterConfigDoc(
				desc = "CSV file's first row is a header with variable names",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = HeaderrowSelectValueFactory.class)
		public String headerRow;	

//		@ParameterConfigDoc(
//				desc = " Show CONSTRUCT template and first rows only (for query debugging)",
//				required = false,
//				defaultValue = "no",
//				type = Type.CHECKBOX)
//		public String test;
		
		@ParameterConfigDoc(desc = "Tarql Query", required = true, type=Type.SPARQLEDITOR, defaultContent=SAMPLE_TARQL)
		public String tarqlQuery;

	}

	@Override
	public void gather(final List<Statement> res) throws Exception {

		Config c = config;

		// TODO populate form values
		CSVOptions options = new CSVOptions();
		
		TarqlQuery tq = new TarqlParser(new StringReader(c.tarqlQuery), null)
				.getResult();

		TarqlQueryExecution ex = TarqlQueryExecutionFactory.create(tq, c.csvFileLocation, options);
		Iterator<Triple> triples = ex.execTriples();

		ValueFactory factory = new ValueFactoryImpl();
		while (triples.hasNext()) {
			Triple t = triples.next();
			Node n = t.getObject();
			Statement st;
			if (t.getSubject().isURI()) {
				if (n.isLiteral()) {
					// TODO Literals are treated as strings (no datatypes
					// retained)
					st = factory.createStatement(
							factory.createURI(t.getSubject().getURI()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createLiteral(n.toString()));
				} else if (n.isURI()) {
					st = factory.createStatement(
							factory.createURI(t.getSubject().getURI()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createURI(n.toString()));
				} else {
					st = factory.createStatement(
							factory.createURI(t.getSubject().getURI()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createBNode(n.toString()));
				}
			} else {
				// subject is a blank node
				if (n.isLiteral()) {
					// TODO Literals are treated as strings (no datatypes
					// retained)
					st = factory.createStatement(
							factory.createBNode(t.getSubject().toString()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createLiteral(n.toString()));
				} else if (n.isURI()) {
					st = factory.createStatement(
							factory.createBNode(t.getSubject().toString()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createURI(n.toString()));
				} else {
					st = factory.createStatement(
							factory.createBNode(t.getSubject().toString()),
							factory.createURI(t.getPredicate().getURI()),
							factory.createBNode(n.toString()));
				}
			}
			res.add(st);
		}

	}

	@Override
	public Class<? extends Config> getConfigClass() {
		return Config.class;
	}
}
