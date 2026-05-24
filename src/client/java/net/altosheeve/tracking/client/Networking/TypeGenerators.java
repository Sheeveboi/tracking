package net.altosheeve.tracking.client.Networking;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class TypeGenerators {

    public static byte[] combineBuffers(byte[]... buffers) {

        int totalLength = 0;
        for (byte[] buffer : buffers) totalLength += buffer.length;
        byte[] out = new byte[totalLength];

        int incrementalLength = 0;
        for (byte[] buffer : buffers) {
            System.arraycopy(buffer, 0, out, incrementalLength, buffer.length);
            incrementalLength += buffer.length;
        }

        return out;
    }

    public static byte[] encodeFloat(float value) {
        int intBits = Float.floatToIntBits(value);
        return new byte[] { (byte) (intBits >> 24), (byte) (intBits >> 16), (byte) (intBits >> 8), (byte) (intBits) };
    }

    public static byte[] encodeInt(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }

    public static byte[] encodePlayer(float x, float y, float z, UUID UUID, String username) {

        byte[] UUIDBytes = UUID.toString().getBytes();

        byte[] xBytes = encodeFloat(x);
        byte[] yBytes = encodeFloat(y);
        byte[] zBytes = encodeFloat(z);

        byte[] usernameLength = new byte[]{(byte) username.length()};
        byte[] usernameBytes = username.getBytes();

        return combineBuffers(UUIDBytes, usernameLength, usernameBytes, xBytes, yBytes, zBytes);

    }

    public static String decodeUUID(Iterator<Byte> buffer) {

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 36 && buffer.hasNext(); i++) out.append((char) buffer.next().byteValue());

        return out.toString();
    }

    public static String decodeString(Iterator<Byte> buffer) {
        int length = buffer.next();

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < length && buffer.hasNext(); i++) out.append((char) buffer.next().byteValue());

        return out.toString();
    }

    public static float decodeFloat(Iterator<Byte> buffer) {
        if (!buffer.hasNext()) return 0;
        byte first  = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte second = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte third  = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte fourth = buffer.next();

        return ByteBuffer.wrap(new byte[] { first, second, third, fourth }).getFloat();
    }

    public static int decodeInt(Iterator<Byte> buffer) {
        if (!buffer.hasNext()) return 0;
        byte first  = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte second = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte third  = buffer.next();
        if (!buffer.hasNext()) return 0;
        byte fourth = buffer.next();
        if (!buffer.hasNext()) return 0;

        //i cant be fucked honestly

        return ByteBuffer.wrap(new byte[] { first, second, third, fourth }).getInt();
    }

}
