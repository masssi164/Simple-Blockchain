package de.flashyotter.blockchain_node.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/** Base interface; Jackson adds {"type": "..."} discriminator. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NewTxDto.class,      name = "NEW_TX"),
        @JsonSubTypes.Type(value = NewBlockDto.class,   name = "NEW_BLOCK"),
        @JsonSubTypes.Type(value = PeerListDto.class,   name = "PEER_LIST"),
        @JsonSubTypes.Type(value = GetBlocksDto.class,  name = "GET_BLOCKS"),
        @JsonSubTypes.Type(value = BlocksDto.class,     name = "BLOCKS")
})
public sealed interface P2PMessageDto
        permits NewTxDto, NewBlockDto, PeerListDto, GetBlocksDto, BlocksDto {}
