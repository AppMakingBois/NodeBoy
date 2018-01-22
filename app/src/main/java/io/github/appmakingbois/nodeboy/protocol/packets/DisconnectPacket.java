package io.github.appmakingbois.nodeboy.protocol.packets;

import java.util.UUID;

import io.github.appmakingbois.nodeboy.protocol.Packet;


public class DisconnectPacket extends Packet {
  public DisconnectPacket(boolean rebroadcasted, UUID clientID) {
    super(rebroadcasted, clientID);
  }

  public DisconnectPacket(UUID clientID) {
    super(clientID);
  }

  @Override
  public byte getPacketID() {
    return ID.DISCONNECT;
  }
}
