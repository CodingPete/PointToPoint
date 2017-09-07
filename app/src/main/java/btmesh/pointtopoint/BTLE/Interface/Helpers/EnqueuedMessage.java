package btmesh.pointtopoint.BTLE.Interface.Helpers;

import android.bluetooth.BluetoothDevice;

/**
 * Created by peter on 07.09.2017.
 */

public class EnqueuedMessage {
    private String UUID;
    private BluetoothDevice device;
    private byte[] message;

    public EnqueuedMessage setUUID(String uuid) {
        this.UUID = uuid;
        return this;
    }

    public String getUUID() {
        return this.UUID;
    }

    public EnqueuedMessage setDevice(BluetoothDevice device) {
        this.device = device;
        return this;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public EnqueuedMessage setMessage(byte[] message) {
        this.message = message;
        return this;
    }

    public byte[] getMessage() {
        return this.message;
    }
}
