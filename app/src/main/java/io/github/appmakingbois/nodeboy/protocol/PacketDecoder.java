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

    public boolean getBoolean(){
        if (contents.size() < 1){
            throw new RuntimeException("No bytes left! If you expected more data, your data could be corrupt.");
        }
        byte b = readByte();
        return decodeBoolean(b);
    }

    public byte getByte(){
        if (contents.size() < 1){
            throw new RuntimeException("No bytes left! If you expected more data, your data could be corrupt.");
        }
        return readByte();
    }

    public byte[] getBytes(int number){
        if(number<1){
            throw new IllegalArgumentException("Cannot read a number of bytes less than 1!");
        }
        if(contents.size() < number){
            throw new RuntimeException("Not enough bytes to read "+number+" bytes! If you expected more data, your data could be corrupt.");
        }
        byte[] output = new byte[number];
        for(int i = 0; i < number; i++){
            output[i] = readByte();
        }
        return output;
    }

    public int getNumberOfBytesRemaining(){
        return contents.size();
    }

    public int getPacketLength(){
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

    private int testVarInt(byte[] input) {
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

            return numRead + 1;
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

    private int testVarLong(byte[] input) {
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
            return numRead + 1;
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
}
