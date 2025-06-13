package de.flashyotter.blockchain_node.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.flashyotter.blockchain_node.discovery.PingDto;
import de.flashyotter.blockchain_node.discovery.PongDto;
import de.flashyotter.blockchain_node.discovery.FindNodeDto;
import de.flashyotter.blockchain_node.discovery.NodesDto;

/** Root type for every peer-to-peer message. */
// blockchain-node/src/main/java/de/flashyotter/blockchain_node/dto/P2PMessageDto.java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(NewTxDto.class),
        @JsonSubTypes.Type(NewBlockDto.class),
        @JsonSubTypes.Type(GetBlocksDto.class),
        @JsonSubTypes.Type(BlocksDto.class),
        @JsonSubTypes.Type(PeerListDto.class),
        @JsonSubTypes.Type(HandshakeDto.class),
        @JsonSubTypes.Type(PingDto.class),
        @JsonSubTypes.Type(PongDto.class),
        @JsonSubTypes.Type(FindNodeDto.class),
        @JsonSubTypes.Type(NodesDto.class)
})
public interface P2PMessageDto { }
