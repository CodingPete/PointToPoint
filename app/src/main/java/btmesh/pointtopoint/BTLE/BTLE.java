package btmesh.pointtopoint.BTLE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ArrayAdapter;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import btmesh.pointtopoint.BTLE.Interface.Advertiser;
import btmesh.pointtopoint.BTLE.Interface.Helpers.EnqueuedMessage;
import btmesh.pointtopoint.BTLE.Interface.Scanner;
import btmesh.pointtopoint.BTLE.Interface.Server;
import btmesh.pointtopoint.BTLE.Interface.Transmitter;

/**
 * Created by peter on 01.08.2017.
 */

public class BTLE {

    public static final String ACTION_FOUND_DEVICE = "btmesh.pointtopoint.BLUETOOTH_DEVICE_FOUND";
    public static final String EXTRA_DATA_STRING = "btmesh.pointtopoint.DATA";

    private static final UUID USER_ID = UUID.randomUUID();

    public static Map<String, BluetoothDevice> devices = Collections.synchronizedMap(
            new HashMap<String, BluetoothDevice>()
    );

    private final String TAG = "mesh:BTLE";

    private static final int WINDOW_SCANNER_IDLE = 0;
    private static final int WINDOW_SCANNER_SCAN = 1;

    private int WINDOW = 0;

    private Advertiser advertiser;
    private Scanner scanner;
    private Server server;
    private static Transmitter transmitter;

    // Scanner Toggle Handler
    private Handler roundRobinHandler = new Handler();

    public BTLE(Context applicationContext, BluetoothAdapter btAdapter, BluetoothManager btManager) {

        advertiser = new Advertiser(
                applicationContext,
                btAdapter
        );
        advertiser.start();

        scanner = new Scanner(
                applicationContext,
                btAdapter
        );

        server = new Server(
                applicationContext,
                btManager
        );

        transmitter = new Transmitter(
                applicationContext
        );

        // Scannerhandler anstoßen
        roundRobinHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        switch (WINDOW) {
                            case WINDOW_SCANNER_IDLE:
                                scanner.stop();
                                break;
                            case WINDOW_SCANNER_SCAN:
                                scanner.start();
                                break;
                        }
                        WINDOW = (WINDOW + 1) % 2;
                        roundRobinHandler.postDelayed(
                                this,
                                new Random().nextInt(1000 - 500) + 500
                        );
                    }
                },
                new Random().nextInt(1000 - 500) + 500
        );
    }

    public static void deviceAdd(String uuid, BluetoothDevice device) {

        // Wenn zu dieser UUID bereits ein Gerät gespeichert ist
        if (devices.containsKey(uuid)) {

            // Wenn sich die public mac geändert hat ...
            if (
                    !devices.get(uuid).getAddress().equals(
                            device.getAddress()
                    )
                    ) {
                // ... neues Device abspeichern
                devices.put(
                        uuid,
                        device
                );
            }
        } else devices.put(
                uuid,
                device
        );
    }

    public static void sendMessage(String uuid, String text) {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.put(BTLE.getUUIDBytes());
        bb.put(text.getBytes());
        bb.flip();
        transmitter.enqueue(
                new EnqueuedMessage()
                    .setUUID(uuid)
                    .setMessage(bb.array())
                    .setDevice(
                            devices.get(
                                    uuid
                            )
                    )
        );
    }

    public static List<String> devicesGet() {
        ArrayList<String> uuids = new ArrayList<>();
        for(Map.Entry<String, BluetoothDevice> result : devices.entrySet()) {
            uuids.add(result.getKey());
        }

        return uuids;
    }

    public static String getUUIDString() {
        return USER_ID.toString();
    }

    public static byte[] getUUIDBytes() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        bb.flip();
        return bb.array();
    }

    public static UUID parseUUIDfromBytes(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long msb = bb.getLong();
        long lsb = bb.getLong();
        return new UUID(
                msb,
                lsb
        );
    }
}
