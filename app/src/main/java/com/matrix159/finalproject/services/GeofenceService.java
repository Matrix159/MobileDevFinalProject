package com.matrix159.finalproject.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GeofenceService extends Service {
    public GeofenceService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
