package com.example.hermitowlapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;

public class MainActivity extends ActionBarActivity  {
	private ProgressDialog progressDialog;
	
	private Timer timer;
	private float draw;
	private float drained,timeElapsed;
	private float Reasonerdrained;
	private String ontologyName, queryName;
	private float OntologyLoaderDrained;
	private long startCountingTime;
	private long stopCountingTime;


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
		progressDialog.setCanceledOnTouchOutside(false);
		
		// display dialog
		progressDialog.show(); 
		
		Intent myIntent = getIntent(); // gets the previously created intent
		ontologyName = myIntent.getStringExtra("ontologyName"); // will return "ontologyName"
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"
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
    		        	org.semanticweb.HermiT.Reasoner hermit = null;
    		        	File file = new File("storage/emulated/0/Download/" +ontologyName);
    		        	//IRI ontIRI = IRI.create(file);
    		        	OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
    		    		OWLOntology ont = null;
    					try {
    						
	    				    start();
	    					startCountingTime = System.currentTimeMillis();

  
    		    			ont = ontManager.loadOntologyFromOntologyDocument(IRI.create(file));
    		    			hermit = new Reasoner(ont);//factory.createReasoner(ont);
    		    			//hermit.precomputeInferences(InferenceType.CLASS_ASSERTIONS,InferenceType.OBJECT_PROPERTY_ASSERTIONS);
    		    			
    		    		} catch (OWLOntologyCreationException e) {
    		    			// TODO Auto-generated catch block
    		    			e.printStackTrace();
    		    		}
    		    		if(ont!=null){
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
    			    		        		 "SELECT * WHERE { "
    			    		        		+"Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#GraduateStudent>), PropertyValue(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#takesCourse>, <http://www.Department0.University0.edu/GraduateCourse0>)"
    			    		        		+ "}");
    			    		        Query query1 = Query.create(
    			    		        		 "SELECT * WHERE { "
    		    			    		        		+"Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#Student>), PropertyValue(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#takesCourse>, <http://www.Department0.University0.edu/GraduateCourse0>)"
    		    			    		        		+ "}");
    			    		        
    			    		        Query query2 = Query.create(
    			    		        		"SELECT * WHERE { " 		
       			   		    		        	 + "Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#Student>) "
       			   		    		        	 //+ "SubClassOf(?x,?y)"
       			   		    		        	 + "}");
    			    		        Query query3 = Query.create(
    			    		        		"SELECT * WHERE { " 		
       			   		    		        	// + "Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#Student>) "
       			   		    		        	 + "SubClassOf(?x,?y)"
       			   		    		        	 + "}");
    			    		        Query[] queries = null;
    			    		        if(queryName.equals("Query1")){
    			    		        	queries = new Query[]{query};
    			    		        }
    			    		        if(queryName.equals("Query2")){
    			    		        	queries = new Query[]{query1};
    			    		        }
    			    		        if(queryName.equals("Query3")){
    			    		        	queries = new Query[]{query2};
    			    		        }
    			    		        if(queryName.equals("Query4")){
    			    		        	queries = new Query[]{query3};
    			    		        }

			    		   	 		boolean NOTmeasured = true;
			    		   	 		float PrewReasonerDrained = 0;
    			    		   	 	for(int i= 0; i<queries.length; i++){
    			    		   	 		try{
	    			    		   	 		Query queryString = queries[i];
	    			    		   	 		String temp = queryString.toString();
		    			    		   	 		
	    			    		   	 		//records how much loader drained of a battery
	
	    			    		   			if(NOTmeasured){
	    			    		   				//records how much loader drained of a battery
	    			    		   				OntologyLoaderDrained = drained;
	    			    		   				write("ontLoader", OntologyLoaderDrained +"");
	    			    		   				NOTmeasured = false;
	    			    		   			}
	    			    		   			stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	    			    					float timeElapsed2 = stopCountingTime;
	    			    					timeElapsed = timeElapsed2/1000;
	    			    					write("LoaderTime", "" +timeElapsed );
	    			    		    		startCountingTime= System.currentTimeMillis();
	    			    		   			
		    			    		        QueryResult result = queryEng.execute(queryString);
		    						    	System.out.println( result);
		    						    	
		    						    	//records how much reasoner drained.
		    								Reasonerdrained = drained - OntologyLoaderDrained- PrewReasonerDrained;
		    								
		    								//keeps record of previous reasoner
		    								PrewReasonerDrained = PrewReasonerDrained + Reasonerdrained;
		    								
		    					    		System.out.println("There was " + OntologyLoaderDrained + "mAh" + " drained by ontology loader");
		    					    		System.out.println("There was " + Reasonerdrained + "mAh" + " drained by reasoner");
		    					    		System.out.println("Running : " + ontologyName);
		    					    		write("log", "________________________________________\n"+"Query: "+ queryName +  "\n"+"HermiT Reasoner " +Reasonerdrained+"mAh"+"\n"
		    					    		+ "HermiT ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "HermiT Total: " +drained+"mAh" +"\n"
		    					    		+"HermiT Running : " + ontologyName+"\n________________________");
		    					    		write("justdata", ""+Reasonerdrained );
		    					    		write("Results", ""+result );

	    			    	    		} catch (OutOfMemoryError E) {
	    			    					System.err.println(E);
	    			    					quiteAnApp();
	    			    	    		}
    			    		   	 	}
    			    				stopCountingTime = System.currentTimeMillis()-startCountingTime;	
    			    				float timeElapsed2 = stopCountingTime;
    			    				float timeElapsed = timeElapsed2/1000;	    			    				//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
    			    	    		write("ReasonerTime", "" +timeElapsed);    			    				//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );

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
            stop();
            finishWithResult();
            finish();
            System.exit(0);        }
	}
	

	public  float bat(){		
		
				BatteryManager mBatteryManager =
						(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
						Long energy =
						mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
				float currentdraw = energy;
				draw = currentdraw;		
			
				return draw;
	}


	public void start() {
	    if(timer != null) {
	        return;
	    }
	    timer = new Timer();	   
	    timer.schedule(new TimerTask() {
	        public void run() {	            
	        	float curret =bat(); 
	        	drained =drained +(curret/3300);
	        	runOnUiThread(new Runnable() {

	        	    @Override
	        	    public void run() {
	        	    	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	    				float timeElapsed2 = stopCountingTime;
	    				timeElapsed = timeElapsed2/1000;		
		        		((TextView)findViewById(R.id.textView)).setText("Capacity Drained = " + drained + "mAh \n"+
	    				"Time Elapsed: "+timeElapsed+"s");
		        		//This if ABORTS the reasoning task because it took too long,
		        		if(timeElapsed>900||drained>45){
		        			quiteAnApp();

		        		}
	        	    }
	        	 });
	        	
	       }
	   }, 0, 1000);
	}
	public void stop() {
		timer.cancel();
		timer = null;
	}




//File writter
	public void write(String fname, String fcontent){
      String filename= "storage/emulated/0/Download/"+fname+".txt";
      String temp = read(fname);
      BufferedWriter writer = null;
      try {
          //create a temporary file
          File logFile = new File(filename);

          // This will output the full path where the file will be written to...
          System.out.println(logFile.getCanonicalPath());

          writer = new BufferedWriter(new FileWriter(logFile));
          
          writer.write(temp + fcontent );
      } catch (Exception e) {
          e.printStackTrace();
      } finally {
          try {
              // Close the writer regardless of what happens...
              writer.close();
          } catch (Exception e) {
          }
      }
	}
	
	//File reader
	   public String read(String fname){
	     BufferedReader br = null;
	     String response = null;
	      try {
	        StringBuffer output = new StringBuffer();
	        String fpath = "storage/emulated/0/Download/"+fname+".txt";
	        br = new BufferedReader(new FileReader(fpath));
	        String line = "";
	        while ((line = br.readLine()) != null) {
	          output.append(line +"\n");
	        }
	        response = output.toString();
	      } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	      }
	      return response;
	   }
	   
	   private void finishWithResult(){
		   
	      Bundle conData = new Bundle();
	      conData.putInt("results", 1);
	      Intent intent = new Intent();
	      intent.putExtras(conData);
	      setResult(RESULT_OK, intent);
	   }
	   
	   public void quiteAnApp(){
		   
		   Reasonerdrained = drained-OntologyLoaderDrained;
			write("log", "ABORTED due to Out Of Memory/Time \n"+"________________________________________\n"+"Query: "+ queryName + "\n"+"HermiT Reasoner " +Reasonerdrained+"mAh"+"\n"
		    		+ "HermiT ont loader " + OntologyLoaderDrained +"mAh"+"\n" + "HermiT Total: " +drained+"mAh"+ "\n"
		    		+"HermiT Running : " + ontologyName+"\n Time Elapsed: "+timeElapsed+"s"+"\n________________________");
		    		write("justdata", ""+Reasonerdrained );
		    		write("Results", "Results Aborted " );
		    		stopCountingTime = System.currentTimeMillis()-startCountingTime;	
					float timeElapsed2 = stopCountingTime;
					timeElapsed = timeElapsed2/1000;			//System.out.println("Time elapsed when runnig simulation :" +(stopCountingTime/1000) + "s" );
					write("ReasonerTime", "" +timeElapsed );
		            progressDialog.dismiss();
		    		stop();
		            finishWithResult();
		            finish();	
		            System.exit(0);
	   }

}
