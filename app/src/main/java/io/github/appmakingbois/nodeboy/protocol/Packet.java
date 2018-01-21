package io.github.appmakingbois.nodeboy.protocol;

import java.util.UUID;

public abstract class Packet
{
    /**
     * The current version of the protocol; sent in Announce packets (see {@link io.github.appmakingbois.nodeboy.protocol.packets.AnnouncePacket}).
     */
    public static int PROTOCOL_VERSION = 2;
    /**
     * Whether or not the packet has been forwarded from another client
     */
    protected boolean rebroadcasted;
    /**
     * The UUID of this specific packet. You can use this to ignore a packet you have already received.
     */
    protected UUID packetUUID;

    /**
     * @return The packet ID of this packet
     * @see ID
     */
    public abstract byte getPacketID();

    /**
     * @return The UUID of this specific packet
     * @see Packet#packetUUID
     */
    public UUID getPacketUUID(){
        return this.packetUUID;
    }

    public Packet(boolean rebroadcasted){
        this.rebroadcasted = rebroadcasted;
        this.packetUUID = UUID.randomUUID();
    }
    public Packet(){
        this(false);
    }

    /**
     * Marks this packet as rebroadcasted.
     * @see Packet#isRebroadcasted()
     */
    public void setRebroadcasted(){
        this.rebroadcasted = true;
    }

    /**
     * Returns rebroadcasted status; i.e. whether or not the packet has been forwarded from another client
     * @return {@code true} if the packet is rebroadcasted and {@code false} if not
     */
    public boolean isRebroadcasted(){
        return this.rebroadcasted;
    }

    /**
     * Contains definitions for packet IDs
     */
    public static class ID
    {
        //packet ID definitions below
        /**
         * The ID of the Announce packet
         */
        public static byte ANNOUNCE = 0x01;
        /**
         * The ID of the Request Client Info packet
         */
        public static byte REQUEST_CLIENT_INFO = 0x02;
        /**
         * The ID of the Keep-Alive packet
         */
        public static byte KEEP_ALIVE = 0x03;
        /**
         * The ID of the Keep-Alive packet
         */
        public static byte CONNECTION_LIST = 0x04;
        /**
         * The ID of the Keep-Alive packet
         */
        public static byte REQUEST_CONNECTION_LIST = 0x05;
        /**
         * The ID of the Keep-Alive packet
         */
        public static byte DATA_PAYLOAD = 0x06;
        /**
         * The ID of the Keep-Alive packet
         */
        public static byte DISCONNECT = 0x7F;
    }
}