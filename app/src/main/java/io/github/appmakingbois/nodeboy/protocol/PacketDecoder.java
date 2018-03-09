package io.github.appmakingbois.nodeboy.protocol;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class PacketDecoder {
    private ArrayList<Byte> contents;
    private int length;

    public PacketDecoder(byte[] input) {
        this.contents = byteArrayToList(input);
        int len = getInt();
        if (len != contents.size()) {
            throw new RuntimeException("Packet length is invalid! Your data could be incomplete or corrupt.");
        }
        this.length = len;
    }

    private byte readByte() {
        byte val = contents.get(0);
        contents.remove(0);
        return val;
    }

    /**
     * Attempts to strip the standard packet fields from the beginning (packet ID, packet UUID, rebroadcast status, and client UUID.)
     * This is useful if you are extending {@link Packet} and have already called the superclass constructor, which takes care of decoding this data. This allows you to quickly jump to the rest of the data.
     * <p>
     * Throws any exceptions encountered during trying to read this data.
     */
    public void stripHeader() {
        this.getInt();
        this.getUUID();
        this.getBoolean();
        this.getUUID();
    }

    public int getInt() {
        int len = testVarInt(byteListToArray(contents));
        if (len == -1) {
            throw new RuntimeException("VarInt could not be read! You could be trying to read a VarLong instead.");
        }
        if (len == -2) {
            throw new RuntimeException("Not enough bytes left. What you are trying to read is likely not a VarInt, or the data is corrupt.");
        }
        byte[] input = new byte[len];
        for (int i = 0; i < len; i++) {
            input[i] = readByte();
        }
        return decodeVarInt(input);
    }

    public long getLong() {
        int len = testVarLong(byteListToArray(contents));
        if (len == -1) {
            throw new RuntimeException("VarLong could not be read! What you are trying to read is likely not a VarLong, or the data is corrupt.");
        }
        if (len == -2) {
            throw new RuntimeException("Not enough bytes left. What you are trying to read is likely not a VarInt, or the data is corrupt.");
        }
        byte[] input = new byte[len];
        for (int i = 0; i < len; i++) {
            input[i] = readByte();
        }
        return decodeVarLong(input);
    }

    public UUID getUUID() {
        if (contents.size() < 16) {
            throw new RuntimeException("Not enough bytes left. What you are trying to read is likely not a UUID, or the data is corrupt.");
        }
        byte[] data = new byte[16];
        for (int i = 0; i < 16; i++) {
            data[i] = readByte();
        }
        return decodeUUID(data);
    }

    public boolean getBoolean() {
        if (contents.size() < 1) {
            throw new RuntimeException("No bytes left! If you expected more data, your data could be corrupt.");
        }
        byte b = readByte();
        return decodeBoolean(b);
    }

    public byte getByte() {
        if (contents.size() < 1) {
            throw new RuntimeException("No bytes left! If you expected more data, your data could be corrupt.");
        }
        return readByte();
    }

    public byte[] getBytes(int number) {
        if (number < 1) {
            throw new IllegalArgumentException("Cannot read a number of bytes less than 1!");
        }
        if (contents.size() < number) {
            throw new RuntimeException("Not enough bytes to read " + number + " bytes! If you expected more data, your data could be corrupt.");
        }
        byte[] output = new byte[number];
        for (int i = 0; i < number; i++) {
            output[i] = readByte();
        }
        return output;
    }

    public int getNumberOfBytesRemaining() {
        return contents.size();
    }

    public int getPacketLength() {
        return length;
    }

    public static UUID decodeUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    public static boolean decodeBoolean(byte input) {
        return input != 0x00;
    }

    public static int decodeVarInt(byte[] input) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = input[numRead];
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);
        return result;
    }

    static int testVarInt(byte[] input) {
        int numRead = 0;
        byte read;
        try {
            do {
                read = input[numRead];
                numRead++;
                if (numRead > 5) {
                    return -1;
                }
            } while ((read & 0b10000000) != 0);
            return numRead;
        } catch (ArrayIndexOutOfBoundsException e) {
            return -2;
        }
    }

    public static long decodeVarLong(byte[] input) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = input[numRead];
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    static int testVarLong(byte[] input) {
        int numRead = 0;
        byte read;
        try {
            do {
                read = input[numRead];
                numRead++;
                if (numRead > 10) {
                    return -1;
                }
            } while ((read & 0b10000000) != 0);
            return numRead;
        } catch (ArrayIndexOutOfBoundsException e) {
            return -2;
        }
    }

    public static byte[] byteListToArray(ArrayList<Byte> input) {
        byte[] output = new byte[input.size()];
        for (int i = 0; i < input.size(); i++) {
            output[i] = input.get(i);
        }
        return output;
    }

    public static ArrayList<Byte> byteArrayToList(byte[] input) {
        ArrayList<Byte> output = new ArrayList<>();
        for (byte b : input) {
            output.add(b);
        }
        return output;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String byteToHex(byte b){
        return bytesToHex(new byte[]{b});
    }
}
