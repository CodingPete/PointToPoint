package btmesh.pointtopoint.BTLE.Interface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.SparseArray;

import btmesh.pointtopoint.BTLE.BTLE;
import btmesh.pointtopoint.BTLE.Interface.Helpers.IntentBroadcast;
import btmesh.pointtopoint.R;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by peter on 16.04.2017.
 */
public class Scanner {

    private static String TAG = "mesh:Scanner";
    private Context context;
    private IntentBroadcast intentBroadcast;
    private BluetoothAdapter btAdapter;

    private static BluetoothLeScanner btScanner;
    private static ScanSettings settings;
    private static ArrayList<ScanFilter> filters;

    public Scanner(Context context, BluetoothAdapter btAdapter) {
        this.context = context;
        this.btAdapter = btAdapter;
        this.intentBroadcast = new IntentBroadcast(context);
        btScanner = btAdapter.getBluetoothLeScanner();

        filters = new ArrayList<>();
        filters.add(
                new ScanFilter.Builder()
                        .setServiceUuid(
                                new ParcelUuid(UUID.fromString(context.getString(R.string.btmesh_uuid)))
                        )
                        .build()
        );
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    public void start() {
        if(btAdapter.isEnabled())
            btScanner.startScan(filters, settings, scanCallback);
    }

    public void stop() {
        btScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try {
                // Die UUID des Gegenübers ist in der ScanResponse enthalten
                String uuid = BTLE.parseUUIDfromBytes(
                        result.getScanRecord().getManufacturerSpecificData(1)
                ).toString();
                BTLE.deviceAdd(
                        uuid,
                        result.getDevice()
                );
                intentBroadcast.sendBroadcast(uuid);

            } catch (NullPointerException e) {
                // Scanresult verwerfen
            }
        }
    };


}
