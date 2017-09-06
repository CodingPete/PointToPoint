package btmesh.pointtopoint.BTLE.Interface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import btmesh.pointtopoint.BTLE.BTLE;
import btmesh.pointtopoint.R;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by peter on 21.04.2017.
 */
public class Advertiser {

    private BluetoothLeAdvertiser btAdvertiser;
    private AdvertiseSettings settings;
    private AdvertiseData data;
    private AdvertiseData scanResponse;

    public Advertiser(Context context, BluetoothAdapter btAdapter) {

        btAdvertiser = btAdapter.getBluetoothLeAdvertiser();

        ParcelUuid puuid = new ParcelUuid(UUID.fromString(context.getString(R.string.btmesh_uuid)));

        settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();
        data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(puuid)
                .addServiceData(puuid, "data".getBytes(Charset.forName("UTF-8")))
                .build();
        scanResponse = new AdvertiseData.Builder()
                .addManufacturerData(1, BTLE.getUUIDBytes())
                .build();
    }

    public void start() {
        btAdvertiser.startAdvertising(
                settings,
                data,
                scanResponse,
                callback
        );
    }

    public void stop() {
        btAdvertiser.stopAdvertising(
                callback
        );
    }


    private static AdvertiseCallback callback = new AdvertiseCallback() {

        private String TAG = "mesh:Advertiser";

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Success");
        }

        @Override
        public void onStartFailure(int errorCode) {
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    Log.d(TAG, "Failed : Already started");
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.d(TAG, "Failed : Data too large");
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.d(TAG, "Failed : Feature unsupported");
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.d(TAG, "Failed : Internal Error");
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.d(TAG, "Failed : Too many advertisers");
                    break;
            }
        }
    };

}
