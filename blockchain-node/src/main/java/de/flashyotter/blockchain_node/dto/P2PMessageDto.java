package de.flashyotter.blockchain_node.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/** Root type for every peer-to-peer message. */
// blockchain-node/src/main/java/de/flashyotter/blockchain_node/dto/P2PMessageDto.java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(NewTxDto.class),
        @JsonSubTypes.Type(NewBlockDto.class),
        @JsonSubTypes.Type(GetBlocksDto.class),
        @JsonSubTypes.Type(BlocksDto.class),
        @JsonSubTypes.Type(PeerListDto.class),
        @JsonSubTypes.Type(HandshakeDto.class)   
})
public sealed interface P2PMessageDto
        permits NewTxDto, NewBlockDto, GetBlocksDto,
                BlocksDto, PeerListDto, HandshakeDto { }   // ➋  neu
