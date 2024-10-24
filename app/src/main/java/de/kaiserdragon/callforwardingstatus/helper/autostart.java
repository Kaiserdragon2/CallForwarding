package de.kaiserdragon.callforwardingstatus.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.kaiserdragon.callforwardingstatus.PhoneStateService;

public class autostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        Intent intent = new Intent(context, PhoneStateService.class);
        context.startForegroundService(intent);
        Log.i("Autostart", "started");
    }
}
