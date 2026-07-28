package net.altosheeve.tracking.client.Networking2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Typing {

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

    public static byte[] encodeLong(long value) {

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();

    }

    public static byte[] encodeInt(int value) {

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();

    }

    public static byte[] encodeString(String value) {

        byte[] usernameLength = new byte[]{(byte) value.length()};
        byte[] usernameBytes = value.getBytes(StandardCharsets.UTF_8);

        return combineBuffers(usernameLength, usernameBytes);

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

        //i cant be fucked honestly

        return ByteBuffer.wrap(new byte[] { first, second, third, fourth }).getInt();
    }

    public static long decodeLong(Iterator<Byte> buffer) {

        byte[] longBytes = new byte[8];

        for (int i = 0; i < 8 && buffer.hasNext(); i++) longBytes[i] = buffer.next();

        ByteBuffer b = ByteBuffer.allocate(Long.BYTES);
        b.put(longBytes);
        b.flip();//need flip
        return b.getLong();

    }

}
