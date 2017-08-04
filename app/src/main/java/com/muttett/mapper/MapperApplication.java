package com.muttett.mapper;

import android.app.Application;

import com.firebase.client.Firebase;

public class MapperApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Firebase.setAndroidContext(this);
  }
}
