package org.deri.opencube.tarql;

import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.deri.opencube.tarql.ui.EncodingSelectValueFactory;
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

		@ParameterConfigDoc(desc = "URL of the input CSV file", type=Type.FILEEDITOR)
		public String csvFileLocation;

		
	    //  -d   --delimiter       Delimiting character of the CSV file

		//  -e   --encoding        Override CSV file encoding (e.g., utf-8 or latin-1)
		
		//   -p   --escapechar      Character used to escape quotes in the CSV file
		
		// --quotechar            Quote character used in the CSV file
		
		// 	      -H   --no-header-row   CSV file has no header row; use variable names ?a, ?b, ...
	    //  --header-row           CSV file's first row is a header with variable names (default)
		
		//  -t   --tabs            Specifies that the input is tab-separagted (TSV), overriding -d
		
	    //  --test                 Show CONSTRUCT template and first rows only (for query debugging)

		@ParameterConfigDoc(
				desc = "Defines the character set name of the files being read, such as UTF-16,UTF-8 or US-ASCII",
				required = false,
				type = Type.DROPDOWN,
				selectValuesFactory = EncodingSelectValueFactory.class)
		public String encoding;
		
		private static final String SAMPLE_TARQL = "PREFIX qb:<http://purl.org/linked-data/cube#> \n" +
				"PREFIX ex: <http://example.com/> \n" +
				"CONSTRUCT { ?uri a qb:Observation; <http://example.com/refArea> ?dim1val; <http://example.com/dim2> ?dim2val; <http://example.com/obsValue> ?e . } WHERE { " +
				" BIND (URI(CONCAT('http://example.com/ns#', ?a)) AS ?uri)\n" +
				" BIND (URI(CONCAT('http://example.com/ns#', ?b)) AS ?dim1val)\n" +
				" BIND (URI(CONCAT('http://example.com/ns#', ?d)) AS ?dim2val)\n" +
				"}";

		@ParameterConfigDoc(desc = "Tarql Query", required = true, type=Type.SPARQLEDITOR, defaultContent=SAMPLE_TARQL)
		public String tarqlQuery;		

	}

	@Override
	public void gather(final List<Statement> res) throws Exception {

		Config c = config;

		TarqlQuery tq = new TarqlParser(new StringReader(c.tarqlQuery), null)
				.getResult();

		InputStream in = new URL(c.csvFileLocation).openStream();
		CSVOptions options = new CSVOptions();
		
		//InputStream in = this.getClass().getResourceAsStream(c.csvFileLocation);
		TarqlQueryExecution ex = TarqlQueryExecutionFactory.create(tq, options);
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
		in.close();
	}

	@Override
	public Class<? extends Config> getConfigClass() {
		return Config.class;
	}
}
