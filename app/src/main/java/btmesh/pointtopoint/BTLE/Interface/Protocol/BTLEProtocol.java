package btmesh.pointtopoint.BTLE.Interface.Protocol;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

/**
 * Created by julia on 11.09.2017.
 */

public interface BTLEProtocol {
    void receive(BluetoothDevice device, byte[] message);
    void consume(BluetoothDevice device, byte[] message);
    void transmit(UUID receiver, byte[] message);
}
