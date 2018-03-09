package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.PacketDecoder;
import io.github.appmakingbois.nodeboy.protocol.PacketEncoder;

public class AnnouncePacket extends Packet
{
    private int protocolVersion;
    private String hardwareAddress;

    private AnnouncePacket(boolean rebroadcasted, UUID clientID, int protocolVersion, String hardwareAddress){
        super(rebroadcasted, clientID);
        this.protocolVersion = protocolVersion;
        this.hardwareAddress = hardwareAddress;
    }

    private AnnouncePacket(UUID clientID, int protocolVersion, String hardwareAddress){
        this(false,clientID,protocolVersion,hardwareAddress);
    }

    public AnnouncePacket(boolean rebroadcasted, UUID clientID, String hardwareAddress){
        this(rebroadcasted,clientID,Packet.PROTOCOL_VERSION,hardwareAddress);
    }

    public AnnouncePacket(UUID clientID, String hardwareAddress){
        this(clientID,Packet.PROTOCOL_VERSION, hardwareAddress);
    }

    public AnnouncePacket(byte[] encodedData){
        super(encodedData);
        PacketDecoder decoder = new PacketDecoder(encodedData);
        decoder.stripHeader();
        this.protocolVersion = decoder.getInt();
        byte[] bytes = decoder.getBytes(6);
        StringBuilder address = new StringBuilder();
        for(int i = 0; i<6;i++){
            address.append(PacketDecoder.byteToHex(bytes[i]));
            if(i<5){
                address.append(":");
            }
        }
        this.hardwareAddress = address.toString().toLowerCase();
    }

    @Override
    public byte getPacketID()
    {
        return Packet.ID.ANNOUNCE;
    }

    public int getProtocolVersion(){
        return this.protocolVersion;
    }

    public byte[] serialize()
    {
        byte[] existing = super.serialize();
        PacketEncoder encoder = new PacketEncoder(existing);
        encoder.putInt(protocolVersion);
        String[] segments = getHardwareAddress().split(":");
        for(String s : segments){
            encoder.putByte((byte) (Integer.parseInt(s,16) & 0xff));
        }
        return encoder.finalPacket();
    }

    public String getHardwareAddress() {
        return hardwareAddress;
    }
}
