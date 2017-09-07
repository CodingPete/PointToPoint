package btmesh.pointtopoint.BTLE.Interface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import btmesh.pointtopoint.BTLE.Interface.Helpers.EnqueuedMessage;
import btmesh.pointtopoint.R;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

import btmesh.pointtopoint.BTLE.BTLE;

/**
 * Created by peter on 24.04.2017.
 */
public class Transmitter {

    private Context context;
    private final String TAG = "mesh:Transmitter";
    private final Semaphore sendPermit = new Semaphore(1);

    // Die Warteschlange der zu sendenden Nachrichten
    private static Queue<EnqueuedMessage> messageQueue = new LinkedBlockingQueue<>();

    public Transmitter(Context context) {
        this.context = context;
    }

    public void enqueue(EnqueuedMessage message) {

        if(messageQueue.isEmpty()) {
            // ... unser Paket anhängen
            messageQueue.add(message);
            next();
        } else messageQueue.add(message);

    }

    private void next() {
        try {
            sendPermit.acquire();
            if(!messageQueue.isEmpty()) {

                // Wenn die Nachricht im Rahmen der MTU liegt
                if(messageQueue.peek().getMessage().length == 20) {
                    // ... Versendung einleiten
                    messageQueue.peek()
                            .getDevice()
                            .connectGatt(
                                    context,
                                    true,
                                    callback
                            );
                } else {
                    // ... ansonsten Paket entfernen
                    messageQueue.remove();
                    // ... und zum nächsten übergehen
                    next();
                }

            }
            sendPermit.release();
        } catch (InterruptedException e) {
            // Tunix
        }
    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        private int MAX_MTU = 0;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTING:
                    Log.d(TAG, "Connecting");
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d(TAG, "Connected");
                    Log.d(TAG, "Request MTU: " + Boolean.toString(gatt.requestMtu(20)));
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.d(TAG, "Disconnecting");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d(TAG, "Disconnected");
                    gatt.close();
                    next();
                    break;
                default:
                    Log.d(TAG, "Unknown Connection State");
                    gatt.close();
                    next();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            // Den Service holen, in der unsere Characteristic steht
            BluetoothGattService service = gatt.getService(UUID.fromString(context.getString(R.string.btmesh_uuid)));

            // Die Characteristic des BT-Mesh Services holen, über die wir Daten schreiben
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(context.getString(R.string.btmesh_rcv_characteristic)));

            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            characteristic.setValue(
                    messageQueue.poll().getMessage()
            );
            gatt.writeCharacteristic(characteristic);


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead");
            gatt.disconnect();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite");

            // Characteristic wurde geschrieben, wir dürfen die Verbindung beenden
            gatt.disconnect();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged");

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead");

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite");

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAG, "Reliable Write Completed");
            gatt.close();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            MAX_MTU = mtu;
            Log.d(TAG, "MAX MTU: " + MAX_MTU);
            gatt.discoverServices();
        }
    };

}
