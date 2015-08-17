package com.abdulrahman.tamilhwrtrec.gestures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureStroke;
import android.gesture.GestureUtils;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TamilAlphabetInputEditor extends InputMethodService implements OnGesturePerformedListener{

	private GestureOverlayView overlay;
	private static GestureLibrary mLibrary1;
	private int mStartBgColor = Color.GRAY;
	private Button eraceBut;
	private Button finBut;
	private Button spaceBut;
	private RelativeLayout mLayout;
	private TextView tv;
	private Bitmap trace=null;
	private Gesture gesture;
	private static HashMap<String,String> alphabetPairs;
	Typeface tf;
	private InputMethodService currentObject;
	File directory;
	
	@Override
	public View onCreateInputView() {
		// TODO Auto-generated method stub
		FrameLayout tracerView = (FrameLayout) getLayoutInflater().inflate(R.layout.input_layout, null);
		directory=new File(this.getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
		currentObject=this;
		if(!filesExist()){
			createFiles();
		}
		DisplayMetrics dm = getResources().getDisplayMetrics(); 
		int densityDpi = dm.densityDpi;
		RelativeLayout rel=(RelativeLayout)tracerView.findViewById(R.id.main);
		rel.setMinimumHeight(densityDpi/2);
		alphabetPairs=new HashMap<String, String>();
		initiateKeyValuePairs();
		overlay=(GestureOverlayView)tracerView.findViewById(R.id.gestures_overlay);
		overlay.setDrawingCacheEnabled(true);
		overlay.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);
		
		finBut=(Button)tracerView.findViewById(R.id.done);
		finBut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getCurrentInputConnection().finishComposingText();

			}
		});

		
		eraceBut=(Button)tracerView.findViewById(R.id.back);
		eraceBut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getCurrentInputConnection().deleteSurroundingText(1, 0);


			}
		});

		spaceBut=(Button)tracerView.findViewById(R.id.space);
		spaceBut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getCurrentInputConnection().commitText(" ", 1);


			}
		});

		
		tv=(TextView)tracerView.findViewById(R.id.textView1);
		tv.setText("");
		tf = Typeface.createFromAsset(getAssets(), "fonts/font1.ttf");  
		tf = Typeface.create(tf, Typeface.BOLD);  
		tv.setTypeface(tf);
		
		mLayout = (RelativeLayout) tracerView.findViewById(R.id.main);
		mLayout.setBackgroundColor(mStartBgColor);
		mLibrary1 =AllographStoreManager.getInstance().setLibrary(GestureLibraries.fromFile(new File(directory.getPath(),"gesture")));
		
		if (!mLibrary1.load()) {
			Toast.makeText(getApplicationContext(), "An Error Occured try to relaunch", Toast.LENGTH_LONG).show();
		}
		overlay.addOnGesturePerformedListener(this);


		return tracerView;
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
		File directory=new File(this.getExternalFilesDir(Environment.MEDIA_UNKNOWN),"TamilRec");
		File successFile=new File(directory.getPath(),"success.txt");
		File failureFile=new File(directory.getPath(),"failure.txt");
		File sucScree=new File(directory,"sucScreen");
		File screenshot=new File(directory, "screen");
		File failFile=new File(directory.getPath(),"failurCrct.txt");
		PrintWriter out=null;
		try {
			directory.mkdir();
			sucScree.mkdir();
			successFile.createNewFile();
			failureFile.createNewFile();
			failFile.createNewFile();
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
				//showDialog(alphabetPairs.get(prediction.name));
				//tv.setText(tv.getText().toString()+alphabetPairs.get(prediction.name));
				currentObject.getCurrentInputConnection().commitText(alphabetPairs.get(prediction.name), 1);
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
					mLibrary1.save();
					
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
