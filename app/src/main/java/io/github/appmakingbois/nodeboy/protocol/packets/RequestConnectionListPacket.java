package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;

public class RequestConnectionListPacket extends Packet
{
    private UUID recipientID;
    /**
     * @return The packet ID of this packet
     * @see ID
     */
    @Override
    public byte getPacketID()
    {
        return ID.REQUEST_CONNECTION_LIST;
    }

    public RequestConnectionListPacket(boolean rebroadcasted, UUID clientID, UUID recipientID){
        super(rebroadcasted, clientID);
        this.recipientID = recipientID;
    }

    private RequestConnectionListPacket(UUID clientID, UUID recipientID){
        this(false,clientID,recipientID);
    }

    public UUID getRecipientID() {
        return recipientID;
    }
}
