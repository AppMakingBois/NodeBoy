package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;

public class AnnouncePacket extends Packet
{
    private int protocolVersion;
    private UUID clientID;

    private AnnouncePacket(int protocolVersion, UUID clientID){
        super();
        this.protocolVersion = protocolVersion;
        this.clientID = clientID;
    }

    public AnnouncePacket(UUID clientID){
        this(Packet.PROTOCOL_VERSION,clientID);
    }

    @Override
    public byte getPacketID()
    {
        return Packet.ID.ANNOUNCE;
    }

    public int getProtocolVersion(){
        return this.protocolVersion;
    }

    public UUID getClientID(){
        return this.clientID;
    }
}
