package io.github.appmakingbois.nodeboy.protocol;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

public class PacketDecoder {
    public static UUID decodeUUID(byte[] bytes){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low);
    }

    public static boolean decodeBoolean(byte input){
        return input != 0x00;
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
