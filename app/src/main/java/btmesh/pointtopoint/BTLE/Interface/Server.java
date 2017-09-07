package btmesh.pointtopoint.BTLE.Interface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import btmesh.pointtopoint.BTLE.BTLE;
import btmesh.pointtopoint.BTLE.Interface.Helpers.IntentBroadcast;
import btmesh.pointtopoint.BTLE.Interface.Service.RcvService;
import btmesh.pointtopoint.R;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by peter on 21.04.2017.
 */
public class Server {

    private BluetoothGattServer server;
    private Context context;
    private IntentBroadcast intentBroadcast;

    public Server(Context context, BluetoothManager btManager) {
        this.context = context;
        this.intentBroadcast = new IntentBroadcast(context);
        server = btManager.openGattServer(context, callback);
        server.addService(
          new RcvService(
                  UUID.fromString(context.getString(R.string.btmesh_uuid)),
                  BluetoothGattService.SERVICE_TYPE_PRIMARY,
                  context
          )
        );
    }

    private BluetoothGattServerCallback callback = new BluetoothGattServerCallback() {
        private String TAG = "mesh:Server";

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);

            server.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

            if(offset == 0) {
                server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                String senderUUID = Arrays.copyOfRange(value, 0,15).toString();
                String message = Arrays.copyOfRange(value, 16, 19).toString();
                Log.d(TAG, message+"\n"+ senderUUID);

            } else {
                server.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, value);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            Log.d(TAG, "EXECUTE WRITE : " + device.getName());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            Log.d(TAG, "onMtuChanged()" + mtu);
        }
    };
}
