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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import btmesh.pointtopoint.BTLE.BTLE;
import btmesh.pointtopoint.BTLE.Interface.Protocol.PingPongProtocol;

public class MainActivity extends Activity {

    private ListView devicesList;
    private ArrayAdapter listAdapter;
    private TextView uuidHeader;
    private Button sendButton;
    private EditText editText;
    private String sendToUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uuidHeader = (TextView)findViewById(R.id.uuidHeader);
        sendButton = (Button)findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTLE.sendMessage(UUID.fromString(sendToUUID), editText.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });
        editText = (EditText)findViewById(R.id.sendToText);

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
                BTLE.getInstance(
                    getApplicationContext(),
                    btAdapter,
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),
                    new PingPongProtocol()
                );
            }
        }


        devicesList = (ListView) findViewById(R.id.devicesList);
        devicesList.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        setSendContent(((RowItem)parent.getItemAtPosition(position)).getUuid());
                    }
                }
        );

        listAdapter = new CustomList(getApplicationContext(), R.layout.list_item, BTLE.devicesGet());
        devicesList.setAdapter(listAdapter);

        // ListView aktualisieren
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(intent.getAction()){
                    case BTLE.ACTION_FOUND_DEVICE:
                        listAdapter.clear();
                        listAdapter.addAll(BTLE.devicesGet());
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter(BTLE.ACTION_FOUND_DEVICE);
        this.registerReceiver(br, filter);
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

    private void setSendContent(String uuid){
        sendToUUID = uuid;
        uuidHeader.setText(uuid);
    }
}
