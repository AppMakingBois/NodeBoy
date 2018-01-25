package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.PacketEncoder;

public class AnnouncePacket extends Packet
{
    private int protocolVersion;

    private AnnouncePacket(boolean rebroadcasted, UUID clientID, int protocolVersion){
        super(rebroadcasted, clientID);
        this.protocolVersion = protocolVersion;
    }

    private AnnouncePacket(UUID clientID, int protocolVersion){
        this(false,clientID,protocolVersion);
    }

    public AnnouncePacket(boolean rebroadcasted, UUID clientID){
        this(rebroadcasted,clientID,Packet.PROTOCOL_VERSION);
    }

    public AnnouncePacket(UUID clientID){
        this(clientID,Packet.PROTOCOL_VERSION);
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
        PacketEncoder encoder = new PacketEncoder(super.serialize());
        encoder.putInt(protocolVersion);
        return encoder.finalPacket();
    }

}
