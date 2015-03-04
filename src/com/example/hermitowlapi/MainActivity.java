package com.example.hermitowlapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity  {
	private ProgressDialog progressDialog;
	
	private Timer timer;
	private float draw;
	private float drained;
	private float Reasonerdrained;
	private BroadcastReceiver batteryInfoReceiver;
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
    		        	File file = new File("storage/emulated/0/Download/a.owl");
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
    			    		        		//+"Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#GraduateStudent>)"
    			    		        		//+ "Class(?X)"		
    			    		        		+"Type(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#GraduateStudent>), PropertyValue(?X, <http://swat.cse.lehigh.edu/onto/univ-bench.owl#takesCourse>, <http://www.Department0.University0.edu/GraduateCourse0>)"
    			    		        		//+"Type(?X, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#GraduateStudent>),PropertyValue(?X,<http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#takesCourse>,<http://www.Department0.University0.edu/GraduateCourse0>)"
    			    		        		//+ " Type(?X, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person>), PropertyValue(?X, ?Y, <http://www.Department0.University0.edu>), SubPropertyOf(?Y, <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#memberOf>)"
    			    		        		+ "}");
    			    		        //"Type(?X, http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person), PropertyValue(?X, ?Y, http://www.Department0.University0.edu), SubPropertyOf(?Y, http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#memberOf)"
    			    		        
    				    			start();
    			    		        QueryResult result = queryEng.execute(query);
    						    	System.out.println( result);
    						    	Reasonerdrained = drained;

    								System.out.println("There was " + Reasonerdrained + "mAh" + " drained");

    				    			


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
            //stop();
			//System.exit(0);
        }
}
	

public  float bat(){		
    registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    batteryInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {			
			int  plugged= intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
			String  technology= intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
			int  temperature= intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
			int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);				
			
			BatteryManager mBatteryManager =
					(BatteryManager)getSystemService(Context.BATTERY_SERVICE);
					Long energy =
					mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);					
			float currentdraw = energy;
			draw = currentdraw;		
			((TextView)findViewById(R.id.textView)).setText("     HERMIT REASONER "+"\n"+"Plugged: "+plugged+"\n"+
					"Technology: "+technology+"\n"+
					"Temperature: "+temperature+"\n"+
					"Voltage: "+voltage+"\n"+
					"Current mA = " + energy + "mA"+ "\n"+
					"Hermit reasoner Drained = " + Reasonerdrained + "mA"+ "\n"+
					"Currentlly Drained = " + drained + "mAh"+ "\n");

		}
	};
	return draw;
}


public void start() {
    if(timer != null) {
        return;
    }
    timer = new Timer();	   
    timer.schedule(new TimerTask() {
        public void run() {	            
           // draw = draw + (bat());
        	float curret =bat(); 
        	drained =drained +(curret/7200);
            		//System.out.println("Current mA = " + curret + "mA"+ "\n"+
					//"Capacity Drained = " + drained + "mAh"+ "\n");
					
    		//batteryInfo=(TextView)findViewById(R.id.textView);

       }
   }, 0, 500 );
}
public void stop() {
    timer.cancel();
    timer = null;
}
	
}
