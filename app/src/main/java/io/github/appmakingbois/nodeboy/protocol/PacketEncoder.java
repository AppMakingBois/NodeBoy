package io.github.appmakingbois.nodeboy.protocol;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class PacketEncoder {
    private ArrayList<Byte> contents;
    private int length;

    public PacketEncoder() {
        length = 0;
        contents = new ArrayList<>();
    }

    public PacketEncoder(byte[] existingBytes) {
        contents = new ArrayList<>();
        ArrayList<Byte> packetLength = new ArrayList<>();
        ArrayList<Byte> tempContents = byteArrayToList(existingBytes);
        int packetLengthBytes = PacketDecoder.testVarInt(byteListToArray(tempContents));
        for (int i = 0; i < packetLengthBytes; i++) {
            packetLength.add(tempContents.get(0));
            tempContents.remove(0);
        }
        int len = PacketDecoder.decodeVarInt(byteListToArray(packetLength));
        if (len != tempContents.size()) {
            this.length = tempContents.size();
        }
        else {
            this.length = len;
        }
        this.putBytes(tempContents);
    }

    public static byte[] encodeUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static byte encodeBoolean(boolean input) {
        return (byte) (input ? 0x01 : 0x00);
    }

    public static byte[] encodeVarInt(int input) {
        ArrayList<Byte> outputTemp = new ArrayList<>();
        int i = 0;
        do {
            byte temp = (byte) (input & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            input >>>= 7;
            if (input != 0) {
                temp |= 0b10000000;
            }
            outputTemp.add(temp);
            i++;
        } while (input != 0);
        return byteListToArray(outputTemp);
    }

    public static byte[] encodeVarLong(long input) {
        ArrayList<Byte> outputTemp = new ArrayList<>();
        int i = 0;
        do {
            byte temp = (byte) (input & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            input >>>= 7;
            if (input != 0) {
                temp |= 0b10000000;
            }
            outputTemp.add(temp);
            i++;
        } while (input != 0);
        return byteListToArray(outputTemp);
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

    public void putUUID(UUID value) {
        ArrayList<Byte> encodedValue = byteArrayToList(encodeUUID(value));
        this.putBytes(encodedValue);
    }

    public void putInt(int value) {
        ArrayList<Byte> encodedValue = byteArrayToList(encodeVarInt(value));
        this.putBytes(encodedValue);
    }

    public void putLong(long value) {
        ArrayList<Byte> encodedValue = byteArrayToList(encodeVarLong(value));
        this.putBytes(encodedValue);
    }

    public void putBoolean(boolean value) {
        length++;
        contents.add(encodeBoolean(value));
    }

    public void putByte(byte value) {
        length++;
        contents.add(value);
    }

    public void putBytes(byte[] value) {
        ArrayList<Byte> list = byteArrayToList(value);
        this.putBytes(list);
    }

    public void putBytes(ArrayList<Byte> value) {
        length += value.size();
        contents.addAll(value);
    }

    public byte[] finalPacket() {
        ArrayList<Byte> output = contents;
        int len = contents.size();
        byte[] lengthEncoded = encodeVarInt(len);
        output.addAll(0, byteArrayToList(lengthEncoded));
        return byteListToArray(output);
    }

}
