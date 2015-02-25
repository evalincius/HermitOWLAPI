package com.example.hermitowlapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import android.support.v7.app.ActionBarActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity  {
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// instantiate new progress dialog
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Loading data"); 
		// display dialog
		progressDialog.show(); 
		 
		 
		// start async task
		new MyAsyncTaskClass().execute();   
		
	}
	
	public void setText(String a){
		TextView batteryInfo=(TextView)findViewById(R.id.textView);
		 batteryInfo.setText("Results: " + a.toString());
		
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
	
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {
		 
        @Override
        protected Void doInBackground(Void... params) {

        	
    		        try {
        		        OWLReasoner hermit = null;

    		        	File file = new File("storage/emulated/0/Download/lubm.owl");
    		        	//IRI ontIRI = IRI.create(file);
    		        	OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
    		    		OWLOntology ont = null;
    					try {
    		    			ont = ontManager.loadOntologyFromOntologyDocument(IRI.create(file));
    		    			
    		    			StructuralReasonerFactory factory = new StructuralReasonerFactory();
    		    			hermit = factory.createReasoner(ont);
    		    			hermit.precomputeInferences(InferenceType.CLASS_ASSERTIONS,InferenceType.OBJECT_PROPERTY_ASSERTIONS);
    		    			
    		    		} catch (OWLOntologyCreationException e) {
    		    			// TODO Auto-generated catch block
    		    			e.printStackTrace();
    		    		}
    		    		if(ont!=null){
    		    			System.out.println("---------->  WORKS <-------------");
    		    			try {
    		    		//OWLReasoner r = new StructuralReasonerFactory().createReasoner(ont);
    		    		 //hermit = new Reasoner.ReasonerFactory().createReasoner(ont);
    		    			} catch (OWLReasonerRuntimeException e) {
    			    			// TODO Auto-generated catch block
    			    			e.printStackTrace();
    			    		}
    		    		
    		    			 try {
    			    		        QueryEngine queryEng = QueryEngine.create(ontManager, hermit);
    			    		        Query query = Query.create(
    			    		        		 "SELECT ?X WHERE { "
    			    		        		//+ "Class(?X)"		
    			    		        		 + "Type(?X, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#GraduateStudent>), PropertyValue(?X, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#takesCourse>, <http://www.Department0.University0.edu/GraduateCourse0>)"
    			    		        		//+ " Type(?X, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person>), PropertyValue(?X, ?Y, <http://www.Department0.University0.edu>), SubPropertyOf(?Y, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#memberOf>)"
    			    		        		+ "}");
    			    		        //"Type(?X, http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person), PropertyValue(?X, ?Y, http://www.Department0.University0.edu), SubPropertyOf(?Y, http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#memberOf)"
    			    		        
    				    			System.out.println("---------->  Executing Query <-------------");

    			    		        QueryResult result = queryEng.execute(query);
    				    			System.out.println("---------->  WORKS <-------------");
    						    	System.out.println( result);
    						    	//setText(result.toString());

    				    			System.out.println("---------->  WORKS <-------------");
    				    			System.exit(0);
    				    			


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
    		        
    		       
    		    
    		
    		
    	
        	
        	return null;
        }
 
        @Override
        protected void onPostExecute(Void result) {
            // put here everything that needs to be done after your async task finishes
            progressDialog.dismiss();
        }
}
	
}
