package com.abdulrahman.tamilhwrtrec.gestures;

import android.gesture.GestureLibrary;

public class AllographStoreManager {

	private static GestureLibrary mLibrary;
	private static AllographStoreManager instance=new AllographStoreManager();
	private AllographStoreManager(){
		
	}
	public static  AllographStoreManager getInstance(){
		return instance;
	}
	public GestureLibrary getLibrary(){
		return mLibrary; 
	}
	public GestureLibrary setLibrary(GestureLibrary fromRawResource) {
		mLibrary=fromRawResource;
		return mLibrary;
	}
}
