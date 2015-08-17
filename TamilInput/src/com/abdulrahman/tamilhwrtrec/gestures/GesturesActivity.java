package com.abdulrahman.tamilhwrtrec.gestures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GesturesActivity extends Activity implements
		OnGesturePerformedListener {
	
	private GestureOverlayView overlay;
	public static GestureLibrary mLibrary1;
	private int mStartBgColor = Color.GRAY;
	private Button finBut;
	private RelativeLayout mLayout;
	private TextView tv;
	private Bitmap trace=null;
	private Gesture gesture;
	private static HashMap<String,String> alphabetPairs;
	File directory;
	Typeface tf;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		directory=new File(this.getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
		if(!filesExist()){
			createFiles();
		}
		alphabetPairs=new HashMap<String, String>();
		initiateKeyValuePairs();
		overlay=(GestureOverlayView)findViewById(R.id.gestures_overlay);
		overlay.setDrawingCacheEnabled(true);
		overlay.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
		finBut=(Button)findViewById(R.id.button1);
		final OnGesturePerformedListener t=this;
		finBut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tv.setText("");
				
			}
		});
		tv=(TextView)findViewById(R.id.textView1);
		tv.setText("");
		tf = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");  
		tf = Typeface.create(tf, Typeface.BOLD);  
		tv.setTypeface(tf);
		mLayout = (RelativeLayout) findViewById(R.id.main);
		mLayout.setBackgroundColor(mStartBgColor);
		mLibrary1=AllographStoreManager.getInstance().setLibrary(GestureLibraries.fromFile(new File(directory.getPath(),"gesture")));
		if (!mLibrary1.load()) {
			finish();
		}
		
		if(mLibrary1.isReadOnly()){
			Toast.makeText(getApplicationContext(), "Read only", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(getApplicationContext(), "Read & write", Toast.LENGTH_LONG).show();
		}
		overlay.addOnGesturePerformedListener(this);
	}

	private void initiateKeyValuePairs() {
		// TODO Auto-generated method stub
		AssetManager assets=getApplicationContext().getAssets();
		try {
			 
			BufferedReader keyReader = new BufferedReader(new InputStreamReader(assets.open("key.txt")));
			BufferedReader valueReader = new BufferedReader(new InputStreamReader(assets.open("value.txt")));
			String str;
			while(!(str=keyReader.readLine()).trim().equals("***")){
				String str2=valueReader.readLine().trim();
				alphabetPairs.put(str, str2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void createFiles() {
		// TODO Auto-generated method stub
		
		File successFile=new File(directory.getPath(),"success.txt");
		File failureFile=new File(directory.getPath(),"failure.txt");
		File sucScree=new File(directory,"sucScreen");
		File screenshot=new File(directory, "screen");
		File failFile=new File(directory.getPath(),"failurCrct.txt");
		File gestureFile=new File(directory.getPath(),"gesture");
		
				PrintWriter out=null;
		try {
			directory.mkdir();
			sucScree.mkdir();
			successFile.createNewFile();
			failureFile.createNewFile();
			failFile.createNewFile();
			gestureFile.createNewFile();
			screenshot.mkdir();
			out=new PrintWriter(successFile);
			out.println(0);
			out.close();
			out=new PrintWriter(failureFile);
			out.println(0);
			out.close();
			out=new PrintWriter(failFile);
			out.println(" ");
			out.close();
			try {
	            InputStream is = getAssets().open("gestures");
	             
	            OutputStream os = new FileOutputStream(gestureFile);
	             
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            //read from is to buffer
	            while((bytesRead = is.read(buffer)) !=-1){
	                os.write(buffer, 0, bytesRead);
	            }
	            is.close();
	            //flush OutputStream to write any buffered data to file
	            os.flush();
	            os.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private boolean filesExist() {
		// TODO Auto-generated method stub
		File tempFile=new File(this.getExternalFilesDir(Environment.MEDIA_UNKNOWN), "TamilRec");
		if(tempFile.exists()){
			return true;
		}else{
			return false;
		}
		
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		trace=Bitmap.createBitmap(overlay.getDrawingCache());
		overlay.destroyDrawingCache();
		this.gesture=gesture;
		ArrayList<Prediction> predictions = mLibrary1.recognize(gesture);
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > .5) {
				showDialog(alphabetPairs.get(prediction.name));
				//tv.setText(tv.getText().toString()+alphabetPairs.get(prediction.name));
				} 
			}else {
				Toast.makeText(this, "No prediction", Toast.LENGTH_SHORT)
						.show();
			}
	}

	@SuppressWarnings("deprecation")
	private void showDialog(String string) {
		// TODO Auto-generated method stub
		final String strFin=string;
		AlertDialog recCorrection=new AlertDialog.Builder(this).create();
		recCorrection.setTitle("Confirmation");
		recCorrection.setMessage("The Character you entered is:\n"+string);
		recCorrection.setButton(DialogInterface.BUTTON_POSITIVE,"Confirm", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				tv.setText(tv.getText().toString()+strFin);
				File directory=new File(getApplicationContext().getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
				File successFile=new File(directory.getPath(),"success.txt");
				File sucScree=new File(directory,"sucScreen");
				Scanner scanner;
				try {
					scanner = new Scanner(successFile);
					int noSuccess=scanner.nextInt();
					PrintWriter out=new PrintWriter(successFile);
					out.println(noSuccess+1);
					out.close();
					//tv.setText("");
					/*File Screen=new File(path)
					FileOutputStream fos1=new FileOutputStream(traceFile);
					*///Bitmap gestureBm=Bitmap.createBitmap(overlay.getDrawingCache(true));
					Toast.makeText(getApplicationContext(), "Thanks for the response.",Toast.LENGTH_SHORT).show();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
		});
		recCorrection.setButton2("Wrong", new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				File directory=new File(getApplicationContext().getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
				File failFile=new File(directory.getPath(),"failure.txt");
				File screenshot=new File(directory, "screen");
				Scanner scanner;
				try {
					scanner = new Scanner(failFile);
					int noFail=scanner.nextInt();
					PrintWriter out=new PrintWriter(failFile);
					out.println(noFail+1);
					out.close();
					File gestureFile=new File(screenshot,"Screen"+noFail+".png");
					File traceFile=new File(screenshot,"Trace"+noFail+".png");
					gestureFile.createNewFile();
					traceFile.createNewFile();
					FileOutputStream fos=new FileOutputStream(gestureFile);
					FileOutputStream fos1=new FileOutputStream(traceFile);
					Bitmap gestureBm=Bitmap.createBitmap(overlay.getDrawingCache(true));
					gestureBm.compress(Bitmap.CompressFormat.PNG, 100, fos);
					trace.compress(Bitmap.CompressFormat.PNG, 100, fos1);
					overlay.destroyDrawingCache();
					//tv.setText("");
					
					fos.close();
					feedback();
					Toast.makeText(getApplicationContext(), "Thanks for the response, It has been recorded",Toast.LENGTH_SHORT).show();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
			}
		}); 
		recCorrection.show();
		
		
	}
	void feedback(){
	AlertDialog.Builder dialog=new AlertDialog.Builder(this);
	LayoutInflater infl=this.getLayoutInflater();
	//dialog.setView(infl.inflate(R.layout.feedback_layout,null));
	//final Spinner alpSp=(Spinner)this.findViewById(R.id.spinner1);
	Resources res=getResources();
	final String [] keyArray=res.getStringArray(R.array.keys);
	final String [] alpArray=res.getStringArray(R.array.alphabets);
	
	dialog.setSingleChoiceItems(R.array.alphabets, 0,new OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			File directory=new File(getApplicationContext().getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
			File failFile=new File(directory.getPath(),"failurCrct.txt");
			PrintWriter out = null;
			try {
				out=new PrintWriter(new FileWriter(failFile,true));
				out.println(alpArray[which]);
				out.close();
				dialog.dismiss();
				tv.setText(tv.getText().toString()+alpArray[which]);
				String strTemp=keyArray[which];
				mLibrary1.addGesture(strTemp,gesture);
				boolean temp= mLibrary1.save();
				if(temp){
					Toast.makeText(getApplicationContext(), "Saved",Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), " Not Saved..........",Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}
	});
	dialog.setTitle("Correct Me Pls");
	dialog.create().show();
		
	}
}