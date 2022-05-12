package com.example.bookcleartool;

import android.app.Application;
import android.content.Context;

import com.example.bookcleartool.database.MyDatabase;

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyDatabase.getInstance(this);


    }
}
