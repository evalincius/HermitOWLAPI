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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
	private String datasetFileName, queryName, ontologyName;
	private float OntologyLoaderDrained;
	private long startCountingTime;
	private long stopCountingTime;
	
	private BroadcastReceiver batteryInfoReceiver;
	private int mvoltage;
	private float watts;
	private float ReasonerdrainedWatts;
	private float OntologyLoaderDrainedWatts;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Intent myIntent = getIntent(); // gets the previously created intent
		
		
		datasetFileName = myIntent.getStringExtra("ontologyFile"); // will return the name of ontology file
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"
		ontologyName = myIntent.getStringExtra("ontologyName"); //returns the name of ontology size
		if(datasetFileName==null){
			System.out.println("CLOSED. Dataset Empty");           
    		Thread thread = new Thread(){
                @Override
               public void run() {
                    try {
                       Thread.sleep(3500); // As I am using LENGTH_LONG in Toast
                        	finish();	
                        	System.exit(0);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
                }  
              };
              
              Toast.makeText(getApplicationContext(), "Launch From The PowerBenchMark app", Toast.LENGTH_LONG).show();
              thread.start();

    	}else{
		// instantiate new progress dialog
		progressDialog = new ProgressDialog(this); 
		// spinner (wheel) style dialog
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); 
		// better yet - use a string resource getString(R.string.your_message)
		progressDialog.setMessage("Please Wait"); 
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new OnCancelListener() {

	        @Override
	        public void onCancel(DialogInterface dialog) {
	        	onBackPressed();
	        }});
		
		// display dialog
		progressDialog.show(); 
		
		// start async task
    	new MyAsyncTaskClass().execute();   
		}
		
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
    		        	File file = new File("storage/emulated/0/Download/" +datasetFileName);
    		        	//IRI ontIRI = IRI.create(file);
    		        	OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
    		    		OWLOntology ont = null;
    					try {
    						
	    				    start();
	    		    		getVoltage();
	    					startCountingTime = System.currentTimeMillis();

  
    		    			ont = ontManager.loadOntologyFromOntologyDocument(IRI.create(file));
    		    			hermit = new Reasoner(ont);//factory.createReasoner(ont);
    		    			//hermit.precomputeInferences(InferenceType.CLASS_ASSERTIONS,InferenceType.OBJECT_PROPERTY_ASSERTIONS);
    		    			
    		    		} catch (OWLOntologyCreationException e) {
    		    			e.printStackTrace();
    		    		}
    		    		if(ont!=null){
    		    			
    		    		
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
    			    		        if(queryName.equals("Instance Retrieval")){
    			    		        	queries = new Query[]{query};
    			    		        }
    			    		        if(queryName.equals("Inference & Instance Retrieval")){
    			    		        	queries = new Query[]{query1};
    			    		        }
    			    		        if(queryName.equals("Inference")){
    			    		        	queries = new Query[]{query2};
    			    		        }
    			    		        if(queryName.equals("Classification")){
    			    		        	queries = new Query[]{query3};
    			    		        }

			    		   	 		boolean NOTmeasured = true;
			    		   	 		float PrewReasonerDrained = 0;
			    		   	 		float PrewReasonerDrainedWatts = 0;
    			    		   	 	for(int i= 0; i<queries.length; i++){
    			    		   	 		try{
	    			    		   	 		Query queryString = queries[i];
		    			    		   	 		
	    			    		   	 		//records how much loader drained of a battery
	
	    			    		   			if(NOTmeasured){
	    			    		   				//records how much loader drained of a battery
	    			    		   				OntologyLoaderDrained = drained;
	    			    		   				OntologyLoaderDrainedWatts = watts;	
	    			    		   				write("ontLoader",""+ OntologyLoaderDrained);
	    			    		   				write("PowerLoader",""+ OntologyLoaderDrainedWatts);
	    			    		   				NOTmeasured = false;
	    			    		   			}
	    			    		   			stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	    			    					float timeElapsed2 = stopCountingTime;
	    			    					timeElapsed = timeElapsed2/1000;
	    			    					write("LoaderTime", "" +timeElapsed );
	    			    		    		startCountingTime= System.currentTimeMillis();
	    			    		   			
		    			    		        QueryResult result = queryEng.execute(queryString);
		    						    	System.out.println( result);
		    						    	
		    						    	//records how much mAh reasoner drained.
		    								Reasonerdrained = drained - OntologyLoaderDrained- PrewReasonerDrained;
		    								//records how much watts reasoner drained
		    								ReasonerdrainedWatts = watts - OntologyLoaderDrainedWatts- PrewReasonerDrainedWatts;

		    								//keeps record of previous reasoner
		    								PrewReasonerDrained = PrewReasonerDrained + Reasonerdrained;
		    								PrewReasonerDrainedWatts = PrewReasonerDrainedWatts + ReasonerdrainedWatts;
		    								
		    					    		//System.out.println("There was " + OntologyLoaderDrained + "mAh" + " drained by ontology loader");
		    					    		//System.out.println("There was " + Reasonerdrained + "mAh" + " drained by reasoner");
		    					    		//System.out.println("Running : " + ontologyName);
		    					    		
		    								
		    								
		    								
		    								write("log", "________________________________________\n"
		    							    		+"HERMIT REASONER:\n"
		    							    		+"Reasoning task: "+ queryName + "  \n"
		    							    		+ "Ontology size : " + ontologyName+ "\n"
		    							    		+"Reasoning task drained: " +Reasonerdrained+"mAh"+"\n"
		    							    		+ "Ontology loader drained: " + OntologyLoaderDrained +"mAh"+"\n" 
		    							    		+ "HermiT drained total: " +drained+"mAh"+ "\n"
		    							    		+ "Time elapsed: "+timeElapsed+"s\n"
		    							    		+ "Power consumed: "+watts+"W"
		    							    		+"\n________________________");
		    					    		write("justdata", ""+Reasonerdrained );
		    					    		write("PowerReasoner", ""+ ReasonerdrainedWatts);
		    					    		write("Results", ""+result );

	    			    	    		} catch (OutOfMemoryError E) {
	    			    					System.err.println(E);
	    			    					quiteAnApp(1);
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
            finishWithResult(1);
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
	        	final float curret =bat();
	        	drained =drained +(curret/3300);//3300s instead 3600s because after calculations there 
	        	//were some error rate determined and diviation from 3300 covers the loss of data that
	        	//was missed to be recorded. Calculated by measuring amount of current drained per 1% and finding 
	        	//the constant that derives 31mah
	        	watts = (float) ((drained*mvoltage/1000)*3.6);
	        	runOnUiThread(new Runnable() {

	        	    @Override
	        	    public void run() {
	        	    	stopCountingTime = System.currentTimeMillis()-startCountingTime;	
	    				float timeElapsed = (float) (stopCountingTime/1000.0);	
	    				((TextView)findViewById(R.id.textView)).setText("HERMIT REASONER:\n"
								+"Reasoning task: "+ queryName + " \n"
								+ "Ontology size : " + ontologyName+ "\n"
								+"Capacity drained = " + drained + "mAh \n"
								+"Time elapsed : " +timeElapsed + "s"
								+ "\nPower consumed: "+watts+"W");
		        		//This if ABORTS the reasoning task because it took too long,
		        		if(timeElapsed>300||drained>45){
		        			quiteAnApp(1);
		        		}
	        	    }
	        	 });
	        	
	       }
	   }, 0, 1000);
	}
	public void stop() {
		if(timer!=null){
		timer.cancel();
		}
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
	        br.close();
	        response = output.toString();
	      } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	      }
	      return response;
	   }
	   
	   private void finishWithResult(int a){
		   
	      Bundle conData = new Bundle();
	      conData.putInt("results", a);
	      Intent intent = new Intent();
	      intent.putExtras(conData);
	      setResult(RESULT_OK, intent);
	   }
	   
	   public void quiteAnApp(int a){
		   
		   Reasonerdrained = drained-OntologyLoaderDrained;
		   ReasonerdrainedWatts = watts-OntologyLoaderDrainedWatts;
		   stopCountingTime = System.currentTimeMillis()-startCountingTime;	
		   float timeElapsed = (float) (stopCountingTime/1000.0);	
		   			write("log", "________ABORTED____________\n"
		    		+"HERMIT REASONER:\n"
		    		+"Reasoning task: "+ queryName + "\n"
		    		+ "Ontology size : " + ontologyName+ "\n"
		    		+"Reasoning task drained: " +Reasonerdrained+"mAh"+"\n"
		    		+ "Ontology loader drained: " + OntologyLoaderDrained +"mAh"+"\n" 
		    		+ "HermiT drained total: " +drained+"mAh"+ "\n"
		    		+ "Time elapsed: "+timeElapsed+"s\n"
		    		+ "Power consumed: "+watts+"W"
		    		+"\n________________________");
		   
		   			write("justdata", ""+Reasonerdrained );
		    		write("PowerReasoner", ""+ ReasonerdrainedWatts);
		    		write("Results", "Results Aborted " );
					write("ReasonerTime", "" +timeElapsed );
		            progressDialog.dismiss();
		    		stop();
		            finishWithResult(a);
		            finish();	
		            System.exit(0);
	   }
	   
	   public void getVoltage(){
	       batteryInfoReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {			
					mvoltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);				
				}
			};
			registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	   /**
	    * Method created the pop up dialog asking if user
	    * wants really quite and application.
	    */
	   @Override
	   public void onBackPressed() {
	   	 
	   		final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.customexit);			
				dialog.setTitle("HermiT");
				// set the custom dialog components - text, image and button
				TextView text = (TextView) dialog.findViewById(R.id.text);
				text.setText("Are you sure you want");
				TextView text2 = (TextView) dialog.findViewById(R.id.text2);
				text2.setText("to CANCEL reasoning?");
				ImageView image = (ImageView) dialog.findViewById(R.id.image);
				image.setImageResource(R.drawable.cancel); 
				Button dialogButton = (Button) dialog.findViewById(R.id.btnok);
				Button dialogButton2 = (Button) dialog.findViewById(R.id.btncancel);
				// if button is clicked, close the custom dialog
				dialogButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						quiteAnApp(-1);
	             		dialog.dismiss();             		
					}
				});
				
				dialogButton2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						progressDialog.show(); 
						dialog.dismiss();
						
					}
				});
				
				dialog.setOnCancelListener(new OnCancelListener() {

			        @Override
			        public void onCancel(DialogInterface dialog) {
			    		progressDialog.show(); 
			        }});
	 
				dialog.show();
	   }

}
