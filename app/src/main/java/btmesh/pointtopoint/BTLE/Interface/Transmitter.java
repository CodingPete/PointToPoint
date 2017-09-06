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

import btmesh.pointtopoint.R;

import java.util.UUID;

import btmesh.pointtopoint.BTLE.BTLE;

/**
 * Created by peter on 24.04.2017.
 */
public class Transmitter {

    private Context context;
    private byte[] message;
    private final String TAG = "mesh:Transmitter";
    private BluetoothDevice device;

    public Transmitter(Context context) {
        this.context = context;
    }

    public void send_message(BluetoothDevice device, byte[] message) {
        Log.d(TAG, "send_message");
        this.message = message;
        device.connectGatt(
                context,
                true,
                callback
        );
    }

    private BluetoothGattCallback callback = new BluetoothGattCallback() {

        private int MAX_MTU = 0;
        private int packet_number = 0;
        private int message_max = 0;

        private void send_fragment(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            byte[] buffer;
            int bytes_left = message.length - packet_number * MAX_MTU;
            if (bytes_left >= MAX_MTU) {
                buffer = new byte[MAX_MTU];
            } else {
                buffer = new byte[bytes_left];
            }
            System.arraycopy(message, packet_number * MAX_MTU, buffer, 0, buffer.length);

            // Nachricht absetzen.
            if (!characteristic.setValue(buffer)) {
                Log.d(TAG, "setValue fail");
            } else Log.d(TAG, "setCharacteristic success : " + new String(buffer));

            if (!gatt.writeCharacteristic(characteristic)) { //Sending
                Log.d(TAG, "writeCharacteristic fail");
            } else {
                Log.d(TAG, "writeCharacteristc success : " + new String(buffer));
                Log.d(TAG, "send package number " + packet_number);
                packet_number++;
            }
        }

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
                    break;
                default:
                    Log.d(TAG, "Unknown Connection State");
                    gatt.close();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BluetoothGattService service = gatt.getService(UUID.fromString(context.getString(R.string.btmesh_uuid)));

            //Ist eine Naxhricht zu senden?
            if (message_max > 0) {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(context.getString(R.string.btmesh_rcv_characteristic)));
                // Die Characteristic des BT-Mesh Services holen, über die wir Daten schreiben
                send_fragment(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead");

            // UUID aus der Characteristic des Remotegerätes holen
            String uuid = new String(characteristic.getValue());

            // UUID und BluetoothDevice in Liste speichern
            BTLE.deviceAdd(
                    uuid,
                    device
            );

            gatt.disconnect();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite");
            if (packet_number == message_max) gatt.disconnect();
            else send_fragment(gatt, characteristic);
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
