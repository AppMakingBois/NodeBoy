package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;


public class DataPayloadPacket extends Packet {
  private byte[] data;
  public DataPayloadPacket(boolean rebroadcasted, UUID clientID, byte[] data) {
    super(rebroadcasted, clientID);
    this.data = data;
  }

  public DataPayloadPacket(UUID clientID, byte[] data){
    this(false,clientID, data);
  }

  @Override
  public byte getPacketID() {
    return ID.DATA_PAYLOAD;
  }

  public byte[] getData() {
    return data;
  }
}
