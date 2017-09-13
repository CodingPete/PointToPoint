package btmesh.pointtopoint.BTLE.Interface.Protocol;

import android.bluetooth.BluetoothDevice;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

import btmesh.pointtopoint.BTLE.BTLE;
import btmesh.pointtopoint.BTLE.Interface.Helpers.EnqueuedMessage;
import btmesh.pointtopoint.BTLE.Interface.Transmitter;

/**
 * Created by peter on 11.09.2017.
 */

public class PingPongProtocol implements BTLEProtocol {
    @Override
    public void receive(BluetoothDevice device, byte[] message) {

        // Absender holen
        UUID sender = BTLE.parseUUIDfromBytes(Arrays.copyOfRange(message, 0,16));

        // Die Übertragene Payload holen
        String value = new String(Arrays.copyOfRange(message, 16, 20));

        // War es ein Ping? Dann antworten wir mit Pong
        if(value.equals("Ping")) {
            transmit(
                    sender,
                    "Pong".getBytes()
            );
        }
    }

    @Override
    public void consume(BluetoothDevice device, byte[] message) {

    }

    @Override
    public void transmit(UUID receiver, byte[] message) {
        ByteBuffer bb = ByteBuffer.allocate(20);
        bb.put(BTLE.getUUIDBytes(
                BTLE.USER_ID
        ));
        bb.put(message);
        bb.flip();
        BTLE.transmitter.enqueue(
                new EnqueuedMessage()
                        .setUUID(receiver)
                        .setMessage(bb.array())
                        .setDevice(
                                BTLE.devices.get(
                                        receiver
                                )
                        )
        );
    }
}
