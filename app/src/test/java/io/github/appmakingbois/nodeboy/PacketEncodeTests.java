package io.github.appmakingbois.nodeboy;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.packets.AnnouncePacket;
import io.github.appmakingbois.nodeboy.protocol.packets.ConnectionListPacket;
import io.github.appmakingbois.nodeboy.protocol.packets.DataPayloadPacket;
import io.github.appmakingbois.nodeboy.protocol.packets.DisconnectPacket;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PacketEncodeTests {
    @Test
    public void testAnnouncePacket() {
        System.out.print("Testing if encoding/decoding an AnnouncePacket works... ");
        UUID clientID = UUID.randomUUID();
        AnnouncePacket p = new AnnouncePacket(clientID);
        byte[] encoded = p.serialize();
        AnnouncePacket p2 = new AnnouncePacket(encoded);

        assertEquals(p.getPacketID(), Packet.ID.ANNOUNCE);
        assertEquals(p2.getPacketID(), Packet.ID.ANNOUNCE);
        assertTrue(p.getClientID().compareTo(p2.getClientID()) == 0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID()) == 0);
        assertEquals(p.isRebroadcasted(), p2.isRebroadcasted());
        assertEquals(p.getProtocolVersion(), p2.getProtocolVersion());
        System.out.println("Success");
    }

    @Test
    public void testConnectionListPacket() {
        System.out.print("Testing if encoding/decoding a ConnectionListPacket works... ");
        UUID clientID = UUID.randomUUID();
        ArrayList<UUID> peerIDs = new ArrayList<>();
        peerIDs.add(UUID.randomUUID());
        peerIDs.add(UUID.randomUUID());
        peerIDs.add(UUID.randomUUID());
        ConnectionListPacket p = new ConnectionListPacket(clientID,peerIDs);

        byte[] encoded = p.serialize();
        ConnectionListPacket p2 = new ConnectionListPacket(encoded);

        assertEquals(p.getPacketID(), Packet.ID.CONNECTION_LIST);
        assertEquals(p2.getPacketID(), Packet.ID.CONNECTION_LIST);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertEquals(p.isRebroadcasted(), p2.isRebroadcasted());
        int i =0;
        for(UUID current : p.getPeerIDs()){
            assertFalse(p2.getPeerIDs().get(i) == null);
            assertTrue(current.compareTo(p2.getPeerIDs().get(i))== 0);
            i++;
        }
        System.out.println("Success");
    }

    @Test
    public void testDataPayloadPacket() {
        System.out.print("Testing if encoding/decoding a DataPayloadPacket works... ");
        UUID clientID = UUID.randomUUID();
        Random r = new Random();
        byte[] data = new byte[18];
        r.nextBytes(data);
        boolean rebroadcasted = r.nextBoolean();
        DataPayloadPacket p = new DataPayloadPacket(rebroadcasted,clientID,data);

        byte[] encoded = p.serialize();
        DataPayloadPacket p2 = new DataPayloadPacket(encoded);
        assertEquals(p.getPacketID(), Packet.ID.DATA_PAYLOAD);
        assertEquals(p2.getPacketID(), Packet.ID.DATA_PAYLOAD);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertEquals(p.isRebroadcasted(), p2.isRebroadcasted());
        assertArrayEquals(data,p.getData());
        assertArrayEquals(data,p2.getData());

        System.out.println("Success");
    }

    @Test
    public void testDisconnectPacket() {
        System.out.print("Testing if encoding/decoding a DisconnectPacket works... ");
        UUID clientID = UUID.randomUUID();
        Random r = new Random();
        boolean rebroadcasted = r.nextBoolean();
        DisconnectPacket p = new DisconnectPacket(rebroadcasted,clientID);

        byte[] encoded = p.serialize();
        DisconnectPacket p2 = new DisconnectPacket(encoded);
        assertEquals(p.getPacketID(), Packet.ID.DISCONNECT);
        assertEquals(p2.getPacketID(), Packet.ID.DISCONNECT);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertEquals(p.isRebroadcasted(),p2.isRebroadcasted());
        System.out.println("Success");
    }
}
