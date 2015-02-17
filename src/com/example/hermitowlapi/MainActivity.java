package com.example.hermitowlapi;

import java.io.File;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;








import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Thread thread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	IRI ontIRI = IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl");
		    		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		    		OWLOntology ont = null;
		    		try {
		    			ont = ontManager.loadOntologyFromOntologyDocument(ontIRI);
		    		} catch (OWLOntologyCreationException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
		    		if(ont!=null){
		    			System.out.println("---------->  WORKS <-------------");
		    		OWLReasoner r = new StructuralReasonerFactory().createReasoner(ont);
		    		OWLReasoner hermit = new Reasoner.ReasonerFactory().createReasoner(ont);
		    		
		    		 try {
		    		        QueryEngine queryEng = QueryEngine.create(ontManager, hermit);
		    		        Query query = Query.create("SELECT ?c WHERE { Class(?c) }");
		    		        QueryResult result = queryEng.execute(query);
		    		        System.out.println(result.toString());

		    		    } catch (QueryParserException ex) {
		    		        //return ex.getMessage();

		    		    } catch (QueryEngineException ex) {
		    		        ex.getMessage();
		    		    }
		    		
		    		
		    		
		    		}else{
		    			
		    			System.out.println("---------->  DOESN'T WORK <-------------");
		    		}	
		    		} catch (Exception e) {
		            e.printStackTrace();
		        }
		        
		       
		    }
		});

		thread.start(); 
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
