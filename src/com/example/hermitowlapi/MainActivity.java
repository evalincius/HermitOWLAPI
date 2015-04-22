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
/**
 * Class MainActivity extends the Activity class and its purpose
 * is to create the HermiT reasoner which is capable to perform the tasks
 * it has been assigned. It also is capable of Measuring the power it drained
 * as well as record the results into different files.
 * 
 * @author  Edgaras Valincius
 * @version 1.0
 * @since   2015-02-03 
 */
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

	/**
	 * onCreate is used to initialize activity. 
	 * This method launches the AsyncTask class that allows
	 * to use background operations.Method also gets the extras from the intent.
	 */ 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Intent myIntent = getIntent(); // gets the previously created intent
		
		
		datasetFileName = myIntent.getStringExtra("ontologyFile"); // will return the name of ontology file
		queryName = myIntent.getStringExtra("queryName"); // will return "queryName"
		ontologyName = myIntent.getStringExtra("ontologyName"); //returns the name of ontology size
		// Checks if the app was launched from the PowerBenchMark app.
		// If not, is closed. 
		if(datasetFileName==null){
			System.out.println("CLOSED. Dataset Empty"); 
			// Thread is used to hold the activity, before closing it, so
			// the Toast have enough time to show its message.
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
	/**
	 * Creates Asynchronous tasks on one same UI thread. 
	 * In this case progress wheel and the calculations are performed 
	 * asynchronously.
	 */
	private class MyAsyncTaskClass extends AsyncTask<Void, Void, Void> {	
		/**
		 * Method performs a computation on a background thread.
		 */
        @Override
        protected Void doInBackground(Void... params) {
	        try {        	
	        	org.semanticweb.HermiT.Reasoner hermit = null;
	        	File file = new File("storage/emulated/0/Download/" +datasetFileName);
	    		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
	    		OWLOntology ont = null;
	    		try {    
	    			//Starts timer that calculates the mAh drained
				    start();
		    		//calls method to start inspecting voltage.
		    		getVoltage();
					startCountingTime = System.currentTimeMillis();
	    			ont = ontManager.loadOntologyFromOntologyDocument(IRI.create(file));
	    			hermit = new Reasoner(ont);//factory.createReasoner(ont);	    			
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
   		    		        	 + "}");
	    		        Query query3 = Query.create(
	    		        		"SELECT * WHERE { " 		
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
	    				float timeElapsed = timeElapsed2/1000;
	    	    		write("ReasonerTime", "" +timeElapsed);
		    		    } catch (QueryParserException ex) {	
		    		    } catch (QueryEngineException ex) {
		    		        ex.getMessage();
		    		    }		    		
	    		}else{
	    			// do Nothing
	    		}
	    		
	    	} catch (Exception e) {
	            e.printStackTrace();
	    	}
	        return null;       	
        }
        /**
    	 * Runs on the UI thread after doInBackground.
    	 */
        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            stop();
            finishWithResult(1);
            finish();
            System.exit(0);        
        }
	}
	
	/**
	 * Battery method bat() reads the battery 
	 * information and return the current flow of the battery.	             
	 * @return float draw that is current in mA flowing from the 
	 * battery at the moment.	 
	 */
	public  float bat(){				
		BatteryManager mBatteryManager =(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
		Long energy =mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
		float currentdraw = energy;
		draw = currentdraw;		
		return draw;
	}

	/**
	* Starts timer that registers current flow in mA of battery
	* and transforms it to mAh.
	*/	
	public void start() {
	    if(timer != null) {
	        return;
	    }
	    timer = new Timer();	   
	    timer.schedule(new TimerTask() {
	    	/**
	    	* Timer calls method run every second. 
	    	* Within run, power consumption is calculated.
	    	* Method invokes
	    	*/	
	        public void run() {	            
	        	final float curret =bat();
	        	
	        	/**
		    	* 3300s instead 3600s because after calculations there 
		    	* were some error rate determined and divided from 3300 covers the loss of data that
		    	* was missed to be recorded. Calculated by measuring amount of current drained per 1% and finding 
		    	* the constant that derives 31mah.
		    	*/	
	        	
	        	drained =drained +(curret/3300);
	        	
	        	/**
		    	* Watts drained were calculated by following formula W=I*V
		    	* (watt= current * voltage). Since voltage was measured in miliVolts, the equation had to
		    	* be divided from 1000 to get the SI units. In case below, it was also multiplied by time,
		    	* so was converted back to Watts instead of watt/hours.
		    	*/
	        	
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
	
	/**
	*Stops the previously launched Timer.
	*/
	public void stop() {
		if(timer!=null){
		timer.cancel();
		}
		timer = null;
	}




	/**
	* File Writer method writes the desired content into the file.
	* If there is no such a file, generates it.
	* @param  fname is a name for a file.
	* @param  fcontent is a content for a file. 
	*/
	public void write(String fname, String fcontent){
      String filename= "storage/emulated/0/Download/"+fname+".txt";
      String temp = read(fname);
      BufferedWriter writer = null;
      try {
          File logFile = new File(filename);
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
	
	/**
	* File Reader method reads the content from the file.
	* @param  fname is a name for a file.
	* @return  response is a content that is read from the file.
	*/
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
	/**
	 * Sends results to intent activity (PowerBenchMark app that was called from) to 
	 * send the information that it finished its task.
	 */
	private void finishWithResult(int a){
		   
	      Bundle conData = new Bundle();
	      conData.putInt("results", a);
	      Intent intent = new Intent();
	      intent.putExtras(conData);
	      setResult(RESULT_OK, intent);
	}
	/**
	 * Closes the app.
	 * Is called when reasoner encounters an error and is manually called for closing.
	 * Records the power consumption it drained.
	 */
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
	/**
	  * Method records voltage. To do that it has to register the BroadcastReceiver,
	  * and every time the state of voltage changes, it records the resigns the mvoltage variable
	  * with the latest voltage measured in miliVolts.
	  */ 
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
