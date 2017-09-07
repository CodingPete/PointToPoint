package btmesh.pointtopoint;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import btmesh.pointtopoint.BTLE.BTLE;

public class MainActivity extends Activity {

    private ListView devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BroadcastReceiver broadcastReceiver = new BluetoothBroadcastReceiver();
        IntentFilter filter = new IntentFilter("btmesh.pointtopoint.BLUETOOTH_DEVICE_FOUND");
        this.registerReceiver(broadcastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("mesh:MainActivity", "TEST");
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        // Wenn Bluetooth Low Energy unterstützt wird ...
        if (check_btle_support()) {

            // BluetoothAdapter holen
            BluetoothAdapter btAdapter = init_bt_adapter();

            // User ggf. Auffordern Bluetooth einzuschalten
            check_bt_turned_on(btAdapter);

            // Wenn Bluetooth eingeschaltet ...
            if (btAdapter.isEnabled()) {
                new BTLE(getApplicationContext(), btAdapter, (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
            }
        }

        devicesList = (ListView) findViewById(R.id.devicesList);
        devicesList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        );

        devicesList.setAdapter(
                new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, BTLE.devicesGet())
        );

        // ListView aktualisieren
        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                devicesList.setAdapter(
                        new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, BTLE.devicesGet())
                );
                handler.postDelayed( this, 1 * 1000 );
            }
        }, 1 * 1000 );

    }

    private boolean check_btle_support() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private BluetoothAdapter init_bt_adapter() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter();
    }

    private void check_bt_turned_on(BluetoothAdapter adapter) {
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 42);
        }
    }
}
