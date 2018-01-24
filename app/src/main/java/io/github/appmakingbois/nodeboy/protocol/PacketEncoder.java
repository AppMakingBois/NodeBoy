package io.github.appmakingbois.nodeboy.protocol;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class PacketEncoder {
    private ArrayList<Byte> contents;
    private int length;

    public PacketEncoder(){
        length = 0;
        contents = new ArrayList<>();
    }

    public PacketEncoder(byte[] existingBytes){
        ArrayList<Byte> packetLength = new ArrayList<>();
        ArrayList<Byte> tempContents = EncodeHelper.byteArrayToList(existingBytes);
        for(int i = 0; i<5; i++)
        {
            packetLength.add(tempContents.get(i));
            tempContents.remove(i);
        }
        int len = EncodeHelper.decodeVarInt(EncodeHelper.byteListToArray(packetLength));
        if(len != tempContents.size())
        {
            this.length = tempContents.size();
        }
        else{
            this.length = len;
        }
        this.putBytes(tempContents);
    }

    public void putUUID(UUID value){
        ArrayList<Byte> encodedValue = EncodeHelper.byteArrayToList(EncodeHelper.encodeUUID(value));
    }

    public void putInt(int value){
        ArrayList<Byte> encodedValue = EncodeHelper.byteArrayToList(EncodeHelper.encodeVarInt(value));
        this.putBytes(encodedValue);
    }

    public void putLong(long value){
        ArrayList<Byte> encodedValue = EncodeHelper.byteArrayToList(EncodeHelper.encodeVarLong(value));
        this.putBytes(encodedValue);
    }

    public void putBoolean(boolean value){
        length++;
        contents.add(EncodeHelper.encodeBoolean(value));
    }

    public void putByte(byte value){
        length++;
        contents.add(value);
    }

    public void putBytes(byte[] value){
        this.putBytes(EncodeHelper.byteArrayToList(value));
    }

    public void putBytes(ArrayList<Byte> value){
        length += value.size();
        contents.addAll(value);
    }

    public byte[] finalPacket(){
        ArrayList<Byte> output = contents;
        int len = contents.size();
        byte[] lengthEncoded = EncodeHelper.encodeVarInt(len);
        output.addAll(0, EncodeHelper.byteArrayToList(lengthEncoded));
        return EncodeHelper.byteListToArray(output);
    }

    public static class EncodeHelper {
        public static byte[] encodeUUID(UUID uuid){
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            return bb.array();
        }

        public static UUID decodeUUID(byte[] bytes){
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long high = bb.getLong();
            long low = bb.getLong();
            return new UUID(high, low);
        }

        public static byte encodeBoolean(boolean input){
            return (byte)(input ? 0x01 : 0x00);
        }

        public static boolean decodeBoolean(byte input){
            return input != 0x00;
        }

        public static byte[] encodeVarInt(int input){
            ArrayList<Byte> outputTemp = new ArrayList<>();
            int i = 0;
            do {
                byte temp = (byte)(input & 0b01111111);
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

        public static int decodeVarInt(byte[] input){
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

        public static byte[] encodeVarLong(long input){
            ArrayList<Byte> outputTemp = new ArrayList<>();
            int i = 0;
            do {
                byte temp = (byte)(input & 0b01111111);
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

        public static long decodeVarLong(byte[] input){
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

        public static byte[] byteListToArray(ArrayList<Byte> input){
            byte[] output = new byte[input.size()];
            for(int i = 0; i<input.size(); i++){
                output[i] = input.get(i);
            }
            return output;
        }

        public static ArrayList<Byte> byteArrayToList(byte[] input){
            ArrayList<Byte> output = new ArrayList<>();
            for(byte b: input)
            {
                output.add(b);
            }
            return output;
        }
    }
}
