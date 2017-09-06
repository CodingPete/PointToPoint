package btmesh.pointtopoint;

import android.content.Context;
import android.os.ParcelUuid;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("btmesh.pointtopoint", appContext.getPackageName());
    }

    @Test
    public void UUID_converting() throws Exception {
        UUID first_uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(first_uuid.getMostSignificantBits());
        bb.putLong(first_uuid.getLeastSignificantBits());
        bb.flip();
        byte[] first_bytes = bb.array();

        bb.clear();
        bb = ByteBuffer.wrap(first_bytes);
        UUID second_uuid = new UUID(
                bb.getLong(),
                bb.getLong()
        );

        assertTrue(
                second_uuid.toString().equals(first_uuid.toString())
        );
    }
}
