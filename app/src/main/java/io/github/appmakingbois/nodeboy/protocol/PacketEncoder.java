package io.github.appmakingbois.nodeboy.protocol;


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

}
