package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.ArrayList;
import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;

public class ConnectionListPacket extends Packet {
    private ArrayList<UUID> peerIDs;
    public ConnectionListPacket(boolean rebroadcasted, UUID clientID, ArrayList<UUID> peerIDs) {
        super(rebroadcasted, clientID);
        if(peerIDs == null){
            throw new IllegalArgumentException("peerIDs cannot be null!");
        }
        this.peerIDs = peerIDs;
    }

    public ConnectionListPacket(UUID clientID, ArrayList<UUID> peerIDs) {
        this(false,clientID,peerIDs);
    }

    /**
     * @return The packet ID of this packet
     * @see ID
     */
    @Override
    public byte getPacketID() {
        return ID.CONNECTION_LIST;
    }

    public ArrayList<UUID> getPeerIDs() {
        return peerIDs;
    }
}
