package btmesh.pointtopoint.BTLE.Interface.Helpers;

import android.content.Context;
import android.content.Intent;

/**
 * Created by julia on 07.09.2017.
 */

public class IntentBroadcast {
    private Context context;
    public IntentBroadcast(Context applicationContext){
        this.context = applicationContext;
    }

    public void sendBroadcast(String uuid){
        Intent intent = new Intent("b");
        intent.setAction("btmesh.pointtopoint.BLUETOOTH_DEVICE_FOUND");
        intent.putExtra("data", uuid);
        context.sendBroadcast(intent);
    }
}
