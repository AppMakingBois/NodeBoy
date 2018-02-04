package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.PacketDecoder;
import io.github.appmakingbois.nodeboy.protocol.PacketEncoder;


public class DataPayloadPacket extends Packet {
    private byte[] data;

    public DataPayloadPacket(boolean rebroadcasted, UUID clientID, byte[] data) {
        super(rebroadcasted, clientID);
        this.data = data;
    }

    public DataPayloadPacket(UUID clientID, byte[] data) {
        this(false, clientID, data);
    }

    public DataPayloadPacket(byte[] encodedData){
        super(encodedData);
        PacketDecoder decoder = new PacketDecoder(encodedData);
        decoder.stripHeader();
        int length = decoder.getNumberOfBytesRemaining();
        this.data = new byte[length];
        for(int i =0; i<length; i++){
            this.data[i] = decoder.getByte();
        }
    }

    @Override
    public byte getPacketID() {
        return ID.DATA_PAYLOAD;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] serialize() {
        PacketEncoder encoder = new PacketEncoder(super.serialize());
        encoder.putBytes(data);
        return encoder.finalPacket();
    }
}
