package org.openmrs.EmbeddedSolrTest;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Scanner;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.search.SolrIndexSearcher;

public class App {
	private static SolrServer server;

	public static void main(String[] args) {
		init();
		clear();
		setLastIndexTime(2770);
		System.out.println(getLastIndexTime(20));
		System.out.println("Add to index.");
		Scanner reader = new Scanner(System.in);
		String ans;
		while (!((ans = reader.next()).equals("stop"))) {
			if (ans.equals("q")) {
				query();
			} else {
				try {
					int personId = Integer.parseInt(ans);
					Date lastIndexTime = getLastIndexTime(personId);
					if (lastIndexTime == null) {
						fullImport(personId);
						setLastIndexTime(personId);
					}
					else {
						deltaImport(personId, lastIndexTime);
					}
					
					System.out.println("Done.");
				} catch (Exception e) {
					System.out.println("Exception.");
				}
			}
		}
		System.out.println("Remove from index.");
		while (!((ans = reader.next()).equals("stop"))) {
			if (ans.equals("q")) {
				query();
			} else {
				remove(Integer.parseInt(ans));
				System.out.println("Done.");
			}
		}
		System.out.println("End.");

		/*
		 * try { clear(); } catch (SolrServerException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } catch (IOException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// index();
		// dataImport();
		query();
		// queryCount();
	}

	private static void deltaImport(int personId, Date lastIndexTime) {
		// TODO Auto-generated method stub
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/dataimport");
		params.set("command", "delta-import");
		params.set("clean", false);

		params.set("personId", personId);
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		params.set("lastIndexTime", formatter.format(lastIndexTime));

		QueryResponse response = null;
		try {
			response = server.query(params);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("response = " + response);
	}

	private static void init() {
		System.setProperty(
				"solr.solr.home",
				"E:\\eclipse\\workspace\\openmrs\\chartsearch\\EmbeddedSolrTest\\src\\main\\resources");
		CoreContainer.Initializer initializer = new CoreContainer.Initializer();
		CoreContainer coreContainer;

		try {
			coreContainer = initializer.initialize();
			// server = new EmbeddedSolrServer(coreContainer, "");
			server = new HttpSolrServer("http://localhost:8983/solr");

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void addToIndex(int personId) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/dataimport");
		params.set("command", "full-import");
		params.set("clean", false);
		params.set("personId", personId);

		QueryResponse response = null;
		try {
			response = server.query(params);
			// query();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void removeFromIndex(String uuid) {
		try {
			server.deleteById(uuid, 10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Date getLastIndexTime(int personId) {
		SolrQuery query = new SolrQuery();
		String queryString = String.format("uuid:%d", personId);
		query.setFilterQueries(queryString);
		// query.setParam("patient_id", ((Integer)patientId).toString());
		// query.setParam("last_index_time", "[* TO *]");
		try {
			QueryResponse response = server.query(query);
			;
			if (response.getResults().isEmpty())
				return null;
			// DateFormat formatter = new
			// SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			return (Date) response.getResults().get(0)
					.getFieldValue("last_index_time");
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void setLastIndexTime(int personId) {
		SolrInputDocument document = new SolrInputDocument();
		Date lastIndexTimeUnformatted = Calendar.getInstance().getTime();
		/*
		 * DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		 * String lastIndexTime = formatter.format(lastIndexTimeUnformatted);
		 */
		document.addField("last_index_time", lastIndexTimeUnformatted);
		document.addField("person_id", personId);
		document.addField("uuid", personId);
		try {
			server.add(document);
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void remove(int obs_id) {
		try {
			server.deleteById(((Integer) obs_id).toString());
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void clear() {
		try {
			server.deleteByQuery("*:*");
			server.commit();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private static void fullImport(int personId) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/dataimport");
		params.set("command", "full-import");
		params.set("clean", false);
		params.set("personId", personId);

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
		query.setQuery("*:*");
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
		/*
		 * SolrQuery query = new SolrQuery(); query.setQuery("rows=0"); try {
		 * QueryResponse response = server.query(query); SolrDocumentList list =
		 * response.getResults(); for (SolrDocument solrDocument : list) {
		 * System.out.println(solrDocument); } } catch (SolrServerException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}
}
