package io.github.appmakingbois.nodeboy;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;
import io.github.appmakingbois.nodeboy.protocol.packets.AnnouncePacket;

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
}
