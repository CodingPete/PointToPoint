package btmesh.pointtopoint.BTLE.Interface.Helpers;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by peter on 07.09.2017.
 */

public class EnqueuedMessage {
    private UUID uuid;
    private BluetoothDevice device;
    private byte[] message;

    public EnqueuedMessage setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public UUID getUUID() {
        return this.uuid;
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
