package com.example.xyzreader.remote;

/**
 * Created by darshan on 27/3/17.
 */

public class UpdateEvent {
    private boolean isSuccessful;

    public UpdateEvent(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}
