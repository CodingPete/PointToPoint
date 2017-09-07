package btmesh.pointtopoint.BTLE.Interface.Helpers;

import android.content.Context;
import android.content.Intent;

import btmesh.pointtopoint.BTLE.BTLE;

/**
 * Created by julia on 07.09.2017.
 */

public class IntentBroadcast {
    private Context context;
    public IntentBroadcast(Context applicationContext){
        this.context = applicationContext;
    }

    public void sendBroadcast(String uuid){
        Intent intent = new Intent(BTLE.ACTION_FOUND_DEVICE);
        intent.putExtra(BTLE.EXTRA_DATA_STRING, uuid);
        context.sendBroadcast(intent);
    }
}
