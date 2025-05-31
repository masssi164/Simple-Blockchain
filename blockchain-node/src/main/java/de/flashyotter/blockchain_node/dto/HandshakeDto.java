package de.flashyotter.blockchain_node.dto;

/**
 * First message each peer sends after the TCP/WebSocket upgrade has
 * finished. Contains a unique node ID and the protocol version so that
 * incompatible nodes can immediately disconnect.
 *
 * A handshake is **idempotent**: if both ends send it at the same time,
 * each side simply ignores the duplicate.
 *
 * @param nodeId          arbitrary, human-friendly identifier
 * @param protocolVersion semantic protocol version (major.minor.patch)
 */
public record HandshakeDto(String nodeId, String protocolVersion)
        implements P2PMessageDto { }
