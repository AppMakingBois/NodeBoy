package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;


public class KeepAlivePacket extends Packet {
    public KeepAlivePacket(boolean rebroadcasted, UUID clientID) {
        super(rebroadcasted, clientID);
    }

    public KeepAlivePacket(UUID clientID) {
        super(clientID);
    }

    /**
     * @return The packet ID of this packet
     * @see ID
     */
    @Override
    public byte getPacketID() {
        return ID.KEEP_ALIVE;
    }
}
