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
import io.github.appmakingbois.nodeboy.protocol.packets.KeepAlivePacket;
import io.github.appmakingbois.nodeboy.protocol.packets.RequestClientInfoPacket;
import io.github.appmakingbois.nodeboy.protocol.packets.RequestConnectionListPacket;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PacketEncodeTests {
    @Test
    public void testAnnouncePacket() {
        System.out.print("Testing if encoding/decoding an AnnouncePacket works... ");
        UUID clientID = UUID.randomUUID();
        String macAddress = "a0:4b:5c:8f:9b:2e";
        AnnouncePacket p = new AnnouncePacket(clientID,macAddress);
        byte[] encoded = p.serialize();
        AnnouncePacket p2 = new AnnouncePacket(encoded);

        assertEquals(p.getPacketID(), Packet.ID.ANNOUNCE);
        assertEquals(p2.getPacketID(), Packet.ID.ANNOUNCE);
        assertTrue(p.getClientID().compareTo(p2.getClientID()) == 0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID()) == 0);
        assertEquals(p.getHardwareAddress(),p2.getHardwareAddress());
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

    @Test
    public void testKeepAlivePacket() {
        System.out.print("Testing if encoding/decoding a KeepAlivePacket works... ");
        UUID clientID = UUID.randomUUID();
        Random r = new Random();
        boolean rebroadcasted = r.nextBoolean();
        KeepAlivePacket p = new KeepAlivePacket(rebroadcasted,clientID);

        byte[] encoded = p.serialize();
        KeepAlivePacket p2 = new KeepAlivePacket(encoded);
        assertEquals(p.getPacketID(), Packet.ID.KEEP_ALIVE);
        assertEquals(p2.getPacketID(), Packet.ID.KEEP_ALIVE);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertEquals(p.isRebroadcasted(),p2.isRebroadcasted());
        System.out.println("Success");
    }

    @Test
    public void testRequestClientInfoPacket() {
        System.out.print("Testing if encoding/decoding a RequestClientInfoPacket works... ");
        UUID clientID = UUID.randomUUID();
        UUID recipientID = UUID.randomUUID();
        Random r = new Random();
        boolean rebroadcasted = r.nextBoolean();
        RequestClientInfoPacket p = new RequestClientInfoPacket(rebroadcasted,clientID,recipientID);

        byte[] encoded = p.serialize();
        RequestClientInfoPacket p2 = new RequestClientInfoPacket(encoded);
        assertEquals(p.getPacketID(), Packet.ID.REQUEST_CLIENT_INFO);
        assertEquals(p2.getPacketID(), Packet.ID.REQUEST_CLIENT_INFO);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertTrue(p.getRecipientID().compareTo(p2.getRecipientID())==0);
        assertEquals(p.isRebroadcasted(),p2.isRebroadcasted());
        System.out.println("Success");
    }

    @Test
    public void testRequestConnectionListPacket() {
        System.out.print("Testing if encoding/decoding a RequestConnectionListPacket works... ");
        UUID clientID = UUID.randomUUID();
        UUID recipientID = UUID.randomUUID();
        Random r = new Random();
        boolean rebroadcasted = r.nextBoolean();
        RequestConnectionListPacket p = new RequestConnectionListPacket(rebroadcasted,clientID,recipientID);

        byte[] encoded = p.serialize();
        RequestConnectionListPacket p2 = new RequestConnectionListPacket(encoded);
        assertEquals(p.getPacketID(), Packet.ID.REQUEST_CONNECTION_LIST);
        assertEquals(p2.getPacketID(), Packet.ID.REQUEST_CONNECTION_LIST);
        assertTrue(p.getClientID().compareTo(p2.getClientID())==0);
        assertTrue(p.getPacketUUID().compareTo(p2.getPacketUUID())==0);
        assertTrue(p.getRecipientID().compareTo(p2.getRecipientID())==0);
        assertEquals(p.isRebroadcasted(),p2.isRebroadcasted());
        System.out.println("Success");
    }
}
