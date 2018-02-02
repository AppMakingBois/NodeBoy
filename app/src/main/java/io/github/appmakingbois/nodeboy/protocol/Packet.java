package io.github.appmakingbois.nodeboy.protocol;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Packet {
    /**
     * The current version of the protocol; sent in Announce packets (see {@link io.github.appmakingbois.nodeboy.protocol.packets.AnnouncePacket}).
     */
    public static int PROTOCOL_VERSION = 3;
    /**
     * Whether or not the packet has been forwarded from another client
     */
    protected boolean rebroadcasted;
    /**
     * The UUID of this specific packet. You can use this to ignore a packet you have already received.
     */
    protected UUID packetUUID;
    /**
     * The ID of the client who sent this packet.
     */
    protected UUID clientID;

    /**
     * @return The packet ID of this packet
     * @see ID
     */
    public abstract byte getPacketID();

    /**
     * @return The UUID of this specific packet
     * @see Packet#packetUUID
     */
    public UUID getPacketUUID() {
        return this.packetUUID;
    }

    /**
     * @return The ID of the client who sent this packet
     * @see Packet#clientID
     */
    public UUID getClientID() {
        return this.clientID;
    }

    public Packet(boolean rebroadcasted, UUID clientID) {
        this.rebroadcasted = rebroadcasted;
        this.packetUUID = UUID.randomUUID();
        if (clientID == null) {
            throw new IllegalArgumentException("clientID cannot be null!");
        }
        this.clientID = clientID;
    }
    public Packet(UUID clientID){
        this(false,clientID);
    }
    public Packet(byte[] encodedData){
        PacketDecoder decoder = new PacketDecoder(encodedData);
        int packetID = decoder.getInt();
        if(packetID!=getPacketID()){
            throw new IllegalArgumentException("Invalid packet type! (Expected packet ID "+getPacketID()+", got "+packetID+")");
        }
        UUID packetUUID = decoder.getUUID();
        boolean rebroadcasted = decoder.getBoolean();
        UUID clientID = decoder.getUUID();

        this.packetUUID = packetUUID;
        this.rebroadcasted = rebroadcasted;
        this.clientID = clientID;
    }

    /**
     * Marks this packet as rebroadcasted.
     *
     * @see Packet#isRebroadcasted()
     */
    public void setRebroadcasted() {
        this.rebroadcasted = true;
    }

    /**
     * Returns rebroadcasted status; i.e. whether or not the packet has been forwarded from another client
     *
     * @return {@code true} if the packet is rebroadcasted and {@code false} if not
     */
    public boolean isRebroadcasted() {
        return this.rebroadcasted;
    }

    public byte[] serialize() {
        PacketEncoder encoder = new PacketEncoder();
        encoder.putInt(this.getPacketID());
        encoder.putUUID(this.getPacketUUID());
        encoder.putBoolean(this.isRebroadcasted());
        encoder.putUUID(this.getClientID());
        return encoder.finalPacket();
    }

    /**
     * Contains definitions for packet IDs
     */
    public static class ID {
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