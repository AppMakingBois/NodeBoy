package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.PacketDecoder;
import io.github.appmakingbois.nodeboy.protocol.PacketEncoder;

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

    public RequestConnectionListPacket(UUID clientID, UUID recipientID){
        this(false,clientID,recipientID);
    }

    public RequestConnectionListPacket(byte[] encodedData){
        super(encodedData);
        PacketDecoder decoder = new PacketDecoder(encodedData);
        decoder.stripHeader();
        this.recipientID = decoder.getUUID();
    }

    public UUID getRecipientID() {
        return recipientID;
    }

    public byte[] serialize(){
        PacketEncoder encoder = new PacketEncoder(super.serialize());
        encoder.putUUID(recipientID);
        return encoder.finalPacket();
    }
}
