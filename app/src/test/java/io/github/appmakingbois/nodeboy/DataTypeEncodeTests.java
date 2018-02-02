package io.github.appmakingbois.nodeboy;


import org.junit.Test;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.PacketDecoder;
import io.github.appmakingbois.nodeboy.protocol.PacketEncoder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataTypeEncodeTests {
    @Test
    public void encodeUUIDTest(){
        System.out.print("Testing if UUID encoding works... ");
        UUID testUUID = UUID.randomUUID();
        assertTrue(PacketDecoder.decodeUUID(PacketEncoder.encodeUUID(testUUID)).compareTo(testUUID) == 0);
        System.out.println("Success");
    }

    @Test
    public void encodeIntTest(){
        System.out.print("Testing if VarInt encoding works... ");
        int testNum = 420;
        assertTrue(PacketDecoder.decodeVarInt(PacketEncoder.encodeVarInt(testNum)) == testNum);
        System.out.println("Success");
    }

    @Test
    public void encodeNegativeIntTest(){
        System.out.print("Testing if VarInt encoding works with negative numbers... ");
        int testNum = -1337;
        assertTrue(PacketDecoder.decodeVarInt(PacketEncoder.encodeVarInt(testNum)) == testNum);
        System.out.println("Success");
    }

    @Test
    public void encodeLongTest(){
        System.out.print("Testing if VarLong encoding works... ");
        long testNum = 1234567890;
        assertTrue(PacketDecoder.decodeVarLong(PacketEncoder.encodeVarLong(testNum)) == testNum);
        System.out.println("Success");
    }

    @Test
    public void encodeBooleanTest(){
        System.out.print("Testing if Boolean encoding works... ");
        boolean t = true;
        boolean f = false;
        assertTrue(PacketDecoder.decodeBoolean(PacketEncoder.encodeBoolean(t)));
        assertFalse(PacketDecoder.decodeBoolean(PacketEncoder.encodeBoolean(f)));
        System.out.println("Success");
    }

    @Test
    public void arrayConversionTest(){
        System.out.print("Testing if converting between byte arrays and ArrayList<Byte> works... ");
        byte[] bytes = new byte[3];
        bytes[0] = 0x10;
        bytes[1] = 0x42;
        bytes[2] = 0x69;
        assertArrayEquals(bytes, PacketDecoder.byteListToArray(PacketEncoder.byteArrayToList(bytes)));
        System.out.println("Success");
    }
}
