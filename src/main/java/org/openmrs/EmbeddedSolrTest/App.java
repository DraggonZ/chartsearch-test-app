package org.openmrs.EmbeddedSolrTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.search.SolrIndexSearcher;

/**
 * Hello world!
 * 
 */
public class App {
	private static SolrServer server;

	public static void main(String[] args) {
		init();
		try {
			clear();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// index();
		dataImport();
		query();
		//queryCount();
	}

	private static void init() {
		System.setProperty(
				"solr.solr.home",
				"E:\\eclipse\\workspace\\openmrs\\chartsearch\\EmbeddedSolrTest\\src\\main\\resources");
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer;

		try {
			coreContainer = initializer.initialize();
			server = new EmbeddedSolrServer(coreContainer, "");

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private static void clear() throws SolrServerException, IOException {
		 server.deleteByQuery("*:*");
	}

	private static void index() {
		SolrInputDocument document = new SolrInputDocument();
		document.addField("person_id", 1);
		document.addField("gender", "M");
		document.addField("birthdate", new Date(1991 + 1900, 3, 25));
		SolrInputDocument document2 = new SolrInputDocument();
		document2.addField("person_id", 2);
		document2.addField("gender", "F");
		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		docs.add(document);
		docs.add(document2);
		try {
			server.add(docs);
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void dataImport() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/dataimport");
		params.set("command", "full-import");

		QueryResponse response = null;
		try {
			response = server.query(params);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("response = " + response);
	}

	private static void query() {
		SolrQuery query = new SolrQuery();
		query.setQuery("address1:Wishard");
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList list = response.getResults();
			for (SolrDocument solrDocument : list) {
				System.out.println(solrDocument);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void queryCount() {
		/*SolrQuery query = new SolrQuery();
		query.setQuery("rows=0");
		try {
			QueryResponse response = server.query(query);
			SolrDocumentList list = response.getResults();
			for (SolrDocument solrDocument : list) {
				System.out.println(solrDocument);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
