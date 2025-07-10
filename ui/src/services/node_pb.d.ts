import * as $protobuf from "protobufjs";
import Long = require("long");
/** Namespace de. */
export namespace de {

    /** Namespace flashyotter. */
    namespace flashyotter {

        /** Namespace blockchain_node. */
        namespace blockchain_node {

            /** Namespace grpc. */
            namespace grpc {

                /** Properties of an Empty. */
                interface IEmpty {
                }

                /** Represents an Empty. */
                class Empty implements IEmpty {

                    /**
                     * Constructs a new Empty.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IEmpty);

                    /**
                     * Creates a new Empty instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns Empty instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IEmpty): de.flashyotter.blockchain_node.grpc.Empty;

                    /**
                     * Encodes the specified Empty message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Empty.verify|verify} messages.
                     * @param message Empty message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IEmpty, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified Empty message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Empty.verify|verify} messages.
                     * @param message Empty message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IEmpty, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes an Empty message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns Empty
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.Empty;

                    /**
                     * Decodes an Empty message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns Empty
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.Empty;

                    /**
                     * Verifies an Empty message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates an Empty message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns Empty
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.Empty;

                    /**
                     * Creates a plain object from an Empty message. Also converts values to other types if specified.
                     * @param message Empty
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.Empty, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this Empty to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for Empty
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a TxInput. */
                interface ITxInput {

                    /** TxInput referencedOutputId */
                    referencedOutputId?: (string|null);

                    /** TxInput signature */
                    signature?: (Uint8Array|null);

                    /** TxInput sender */
                    sender?: (Uint8Array|null);
                }

                /** Represents a TxInput. */
                class TxInput implements ITxInput {

                    /**
                     * Constructs a new TxInput.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.ITxInput);

                    /** TxInput referencedOutputId. */
                    public referencedOutputId: string;

                    /** TxInput signature. */
                    public signature: Uint8Array;

                    /** TxInput sender. */
                    public sender: Uint8Array;

                    /**
                     * Creates a new TxInput instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns TxInput instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.ITxInput): de.flashyotter.blockchain_node.grpc.TxInput;

                    /**
                     * Encodes the specified TxInput message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxInput.verify|verify} messages.
                     * @param message TxInput message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.ITxInput, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified TxInput message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxInput.verify|verify} messages.
                     * @param message TxInput message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.ITxInput, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a TxInput message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns TxInput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.TxInput;

                    /**
                     * Decodes a TxInput message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns TxInput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.TxInput;

                    /**
                     * Verifies a TxInput message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a TxInput message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns TxInput
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.TxInput;

                    /**
                     * Creates a plain object from a TxInput message. Also converts values to other types if specified.
                     * @param message TxInput
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.TxInput, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this TxInput to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for TxInput
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a TxOutput. */
                interface ITxOutput {

                    /** TxOutput value */
                    value?: (number|null);

                    /** TxOutput recipientAddress */
                    recipientAddress?: (string|null);
                }

                /** Represents a TxOutput. */
                class TxOutput implements ITxOutput {

                    /**
                     * Constructs a new TxOutput.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.ITxOutput);

                    /** TxOutput value. */
                    public value: number;

                    /** TxOutput recipientAddress. */
                    public recipientAddress: string;

                    /**
                     * Creates a new TxOutput instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns TxOutput instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.ITxOutput): de.flashyotter.blockchain_node.grpc.TxOutput;

                    /**
                     * Encodes the specified TxOutput message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxOutput.verify|verify} messages.
                     * @param message TxOutput message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.ITxOutput, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified TxOutput message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxOutput.verify|verify} messages.
                     * @param message TxOutput message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.ITxOutput, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a TxOutput message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns TxOutput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.TxOutput;

                    /**
                     * Decodes a TxOutput message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns TxOutput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.TxOutput;

                    /**
                     * Verifies a TxOutput message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a TxOutput message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns TxOutput
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.TxOutput;

                    /**
                     * Creates a plain object from a TxOutput message. Also converts values to other types if specified.
                     * @param message TxOutput
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.TxOutput, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this TxOutput to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for TxOutput
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a Transaction. */
                interface ITransaction {

                    /** Transaction inputs */
                    inputs?: (de.flashyotter.blockchain_node.grpc.ITxInput[]|null);

                    /** Transaction outputs */
                    outputs?: (de.flashyotter.blockchain_node.grpc.ITxOutput[]|null);

                    /** Transaction maxFee */
                    maxFee?: (number|null);

                    /** Transaction tip */
                    tip?: (number|null);
                }

                /** Represents a Transaction. */
                class Transaction implements ITransaction {

                    /**
                     * Constructs a new Transaction.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.ITransaction);

                    /** Transaction inputs. */
                    public inputs: de.flashyotter.blockchain_node.grpc.ITxInput[];

                    /** Transaction outputs. */
                    public outputs: de.flashyotter.blockchain_node.grpc.ITxOutput[];

                    /** Transaction maxFee. */
                    public maxFee: number;

                    /** Transaction tip. */
                    public tip: number;

                    /**
                     * Creates a new Transaction instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns Transaction instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.ITransaction): de.flashyotter.blockchain_node.grpc.Transaction;

                    /**
                     * Encodes the specified Transaction message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Transaction.verify|verify} messages.
                     * @param message Transaction message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.ITransaction, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified Transaction message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Transaction.verify|verify} messages.
                     * @param message Transaction message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.ITransaction, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a Transaction message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns Transaction
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.Transaction;

                    /**
                     * Decodes a Transaction message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns Transaction
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.Transaction;

                    /**
                     * Verifies a Transaction message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a Transaction message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns Transaction
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.Transaction;

                    /**
                     * Creates a plain object from a Transaction message. Also converts values to other types if specified.
                     * @param message Transaction
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.Transaction, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this Transaction to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for Transaction
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a Block. */
                interface IBlock {

                    /** Block height */
                    height?: (number|null);

                    /** Block previousHashHex */
                    previousHashHex?: (string|null);

                    /** Block timeMillis */
                    timeMillis?: (number|Long|null);

                    /** Block compactBits */
                    compactBits?: (number|null);

                    /** Block nonce */
                    nonce?: (number|null);

                    /** Block merkleRootHex */
                    merkleRootHex?: (string|null);

                    /** Block txList */
                    txList?: (de.flashyotter.blockchain_node.grpc.ITransaction[]|null);
                }

                /** Represents a Block. */
                class Block implements IBlock {

                    /**
                     * Constructs a new Block.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IBlock);

                    /** Block height. */
                    public height: number;

                    /** Block previousHashHex. */
                    public previousHashHex: string;

                    /** Block timeMillis. */
                    public timeMillis: (number|Long);

                    /** Block compactBits. */
                    public compactBits: number;

                    /** Block nonce. */
                    public nonce: number;

                    /** Block merkleRootHex. */
                    public merkleRootHex: string;

                    /** Block txList. */
                    public txList: de.flashyotter.blockchain_node.grpc.ITransaction[];

                    /**
                     * Creates a new Block instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns Block instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IBlock): de.flashyotter.blockchain_node.grpc.Block;

                    /**
                     * Encodes the specified Block message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Block.verify|verify} messages.
                     * @param message Block message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IBlock, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified Block message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Block.verify|verify} messages.
                     * @param message Block message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IBlock, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a Block message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns Block
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.Block;

                    /**
                     * Decodes a Block message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns Block
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.Block;

                    /**
                     * Verifies a Block message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a Block message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns Block
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.Block;

                    /**
                     * Creates a plain object from a Block message. Also converts values to other types if specified.
                     * @param message Block
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.Block, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this Block to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for Block
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a BlockList. */
                interface IBlockList {

                    /** BlockList blocks */
                    blocks?: (de.flashyotter.blockchain_node.grpc.IBlock[]|null);
                }

                /** Represents a BlockList. */
                class BlockList implements IBlockList {

                    /**
                     * Constructs a new BlockList.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IBlockList);

                    /** BlockList blocks. */
                    public blocks: de.flashyotter.blockchain_node.grpc.IBlock[];

                    /**
                     * Creates a new BlockList instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns BlockList instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IBlockList): de.flashyotter.blockchain_node.grpc.BlockList;

                    /**
                     * Encodes the specified BlockList message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.BlockList.verify|verify} messages.
                     * @param message BlockList message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IBlockList, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified BlockList message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.BlockList.verify|verify} messages.
                     * @param message BlockList message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IBlockList, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a BlockList message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns BlockList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.BlockList;

                    /**
                     * Decodes a BlockList message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns BlockList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.BlockList;

                    /**
                     * Verifies a BlockList message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a BlockList message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns BlockList
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.BlockList;

                    /**
                     * Creates a plain object from a BlockList message. Also converts values to other types if specified.
                     * @param message BlockList
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.BlockList, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this BlockList to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for BlockList
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a PageRequest. */
                interface IPageRequest {

                    /** PageRequest page */
                    page?: (number|null);

                    /** PageRequest size */
                    size?: (number|null);
                }

                /** Represents a PageRequest. */
                class PageRequest implements IPageRequest {

                    /**
                     * Constructs a new PageRequest.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IPageRequest);

                    /** PageRequest page. */
                    public page: number;

                    /** PageRequest size. */
                    public size: number;

                    /**
                     * Creates a new PageRequest instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns PageRequest instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IPageRequest): de.flashyotter.blockchain_node.grpc.PageRequest;

                    /**
                     * Encodes the specified PageRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.PageRequest.verify|verify} messages.
                     * @param message PageRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IPageRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified PageRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.PageRequest.verify|verify} messages.
                     * @param message PageRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IPageRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a PageRequest message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns PageRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.PageRequest;

                    /**
                     * Decodes a PageRequest message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns PageRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.PageRequest;

                    /**
                     * Verifies a PageRequest message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a PageRequest message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns PageRequest
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.PageRequest;

                    /**
                     * Creates a plain object from a PageRequest message. Also converts values to other types if specified.
                     * @param message PageRequest
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.PageRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this PageRequest to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for PageRequest
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a HistoryRequest. */
                interface IHistoryRequest {

                    /** HistoryRequest address */
                    address?: (string|null);

                    /** HistoryRequest limit */
                    limit?: (number|null);
                }

                /** Represents a HistoryRequest. */
                class HistoryRequest implements IHistoryRequest {

                    /**
                     * Constructs a new HistoryRequest.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IHistoryRequest);

                    /** HistoryRequest address. */
                    public address: string;

                    /** HistoryRequest limit. */
                    public limit: number;

                    /**
                     * Creates a new HistoryRequest instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns HistoryRequest instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IHistoryRequest): de.flashyotter.blockchain_node.grpc.HistoryRequest;

                    /**
                     * Encodes the specified HistoryRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.HistoryRequest.verify|verify} messages.
                     * @param message HistoryRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IHistoryRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified HistoryRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.HistoryRequest.verify|verify} messages.
                     * @param message HistoryRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IHistoryRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a HistoryRequest message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns HistoryRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.HistoryRequest;

                    /**
                     * Decodes a HistoryRequest message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns HistoryRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.HistoryRequest;

                    /**
                     * Verifies a HistoryRequest message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a HistoryRequest message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns HistoryRequest
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.HistoryRequest;

                    /**
                     * Creates a plain object from a HistoryRequest message. Also converts values to other types if specified.
                     * @param message HistoryRequest
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.HistoryRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this HistoryRequest to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for HistoryRequest
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a WalletInfo. */
                interface IWalletInfo {

                    /** WalletInfo address */
                    address?: (string|null);

                    /** WalletInfo balance */
                    balance?: (number|null);
                }

                /** Represents a WalletInfo. */
                class WalletInfo implements IWalletInfo {

                    /**
                     * Constructs a new WalletInfo.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.IWalletInfo);

                    /** WalletInfo address. */
                    public address: string;

                    /** WalletInfo balance. */
                    public balance: number;

                    /**
                     * Creates a new WalletInfo instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns WalletInfo instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.IWalletInfo): de.flashyotter.blockchain_node.grpc.WalletInfo;

                    /**
                     * Encodes the specified WalletInfo message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.WalletInfo.verify|verify} messages.
                     * @param message WalletInfo message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.IWalletInfo, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified WalletInfo message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.WalletInfo.verify|verify} messages.
                     * @param message WalletInfo message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.IWalletInfo, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a WalletInfo message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns WalletInfo
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.WalletInfo;

                    /**
                     * Decodes a WalletInfo message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns WalletInfo
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.WalletInfo;

                    /**
                     * Verifies a WalletInfo message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a WalletInfo message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns WalletInfo
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.WalletInfo;

                    /**
                     * Creates a plain object from a WalletInfo message. Also converts values to other types if specified.
                     * @param message WalletInfo
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.WalletInfo, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this WalletInfo to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for WalletInfo
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a SendRequest. */
                interface ISendRequest {

                    /** SendRequest recipient */
                    recipient?: (string|null);

                    /** SendRequest amount */
                    amount?: (number|null);
                }

                /** Represents a SendRequest. */
                class SendRequest implements ISendRequest {

                    /**
                     * Constructs a new SendRequest.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.ISendRequest);

                    /** SendRequest recipient. */
                    public recipient: string;

                    /** SendRequest amount. */
                    public amount: number;

                    /**
                     * Creates a new SendRequest instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns SendRequest instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.ISendRequest): de.flashyotter.blockchain_node.grpc.SendRequest;

                    /**
                     * Encodes the specified SendRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.SendRequest.verify|verify} messages.
                     * @param message SendRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.ISendRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified SendRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.SendRequest.verify|verify} messages.
                     * @param message SendRequest message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.ISendRequest, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a SendRequest message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns SendRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.SendRequest;

                    /**
                     * Decodes a SendRequest message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns SendRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.SendRequest;

                    /**
                     * Verifies a SendRequest message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a SendRequest message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns SendRequest
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.SendRequest;

                    /**
                     * Creates a plain object from a SendRequest message. Also converts values to other types if specified.
                     * @param message SendRequest
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.SendRequest, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this SendRequest to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for SendRequest
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Properties of a TxList. */
                interface ITxList {

                    /** TxList txs */
                    txs?: (de.flashyotter.blockchain_node.grpc.ITransaction[]|null);
                }

                /** Represents a TxList. */
                class TxList implements ITxList {

                    /**
                     * Constructs a new TxList.
                     * @param [properties] Properties to set
                     */
                    constructor(properties?: de.flashyotter.blockchain_node.grpc.ITxList);

                    /** TxList txs. */
                    public txs: de.flashyotter.blockchain_node.grpc.ITransaction[];

                    /**
                     * Creates a new TxList instance using the specified properties.
                     * @param [properties] Properties to set
                     * @returns TxList instance
                     */
                    public static create(properties?: de.flashyotter.blockchain_node.grpc.ITxList): de.flashyotter.blockchain_node.grpc.TxList;

                    /**
                     * Encodes the specified TxList message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxList.verify|verify} messages.
                     * @param message TxList message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encode(message: de.flashyotter.blockchain_node.grpc.ITxList, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Encodes the specified TxList message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxList.verify|verify} messages.
                     * @param message TxList message or plain object to encode
                     * @param [writer] Writer to encode to
                     * @returns Writer
                     */
                    public static encodeDelimited(message: de.flashyotter.blockchain_node.grpc.ITxList, writer?: $protobuf.Writer): $protobuf.Writer;

                    /**
                     * Decodes a TxList message from the specified reader or buffer.
                     * @param reader Reader or buffer to decode from
                     * @param [length] Message length if known beforehand
                     * @returns TxList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decode(reader: ($protobuf.Reader|Uint8Array), length?: number): de.flashyotter.blockchain_node.grpc.TxList;

                    /**
                     * Decodes a TxList message from the specified reader or buffer, length delimited.
                     * @param reader Reader or buffer to decode from
                     * @returns TxList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    public static decodeDelimited(reader: ($protobuf.Reader|Uint8Array)): de.flashyotter.blockchain_node.grpc.TxList;

                    /**
                     * Verifies a TxList message.
                     * @param message Plain object to verify
                     * @returns `null` if valid, otherwise the reason why it is not
                     */
                    public static verify(message: { [k: string]: any }): (string|null);

                    /**
                     * Creates a TxList message from a plain object. Also converts values to their respective internal types.
                     * @param object Plain object
                     * @returns TxList
                     */
                    public static fromObject(object: { [k: string]: any }): de.flashyotter.blockchain_node.grpc.TxList;

                    /**
                     * Creates a plain object from a TxList message. Also converts values to other types if specified.
                     * @param message TxList
                     * @param [options] Conversion options
                     * @returns Plain object
                     */
                    public static toObject(message: de.flashyotter.blockchain_node.grpc.TxList, options?: $protobuf.IConversionOptions): { [k: string]: any };

                    /**
                     * Converts this TxList to JSON.
                     * @returns JSON object
                     */
                    public toJSON(): { [k: string]: any };

                    /**
                     * Gets the default type url for TxList
                     * @param [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns The default type url
                     */
                    public static getTypeUrl(typeUrlPrefix?: string): string;
                }

                /** Represents a Mining */
                class Mining extends $protobuf.rpc.Service {

                    /**
                     * Constructs a new Mining service.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     */
                    constructor(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean);

                    /**
                     * Creates new Mining service using the specified rpc implementation.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     * @returns RPC service. Useful where requests and/or responses are streamed.
                     */
                    public static create(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean): Mining;

                    /**
                     * Calls Mine.
                     * @param request Empty message or plain object
                     * @param callback Node-style callback called with the error, if any, and Block
                     */
                    public mine(request: de.flashyotter.blockchain_node.grpc.IEmpty, callback: de.flashyotter.blockchain_node.grpc.Mining.MineCallback): void;

                    /**
                     * Calls Mine.
                     * @param request Empty message or plain object
                     * @returns Promise
                     */
                    public mine(request: de.flashyotter.blockchain_node.grpc.IEmpty): Promise<de.flashyotter.blockchain_node.grpc.Block>;
                }

                namespace Mining {

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Mining#mine}.
                     * @param error Error, if any
                     * @param [response] Block
                     */
                    type MineCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.Block) => void;
                }

                /** Represents a Wallet */
                class Wallet extends $protobuf.rpc.Service {

                    /**
                     * Constructs a new Wallet service.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     */
                    constructor(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean);

                    /**
                     * Creates new Wallet service using the specified rpc implementation.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     * @returns RPC service. Useful where requests and/or responses are streamed.
                     */
                    public static create(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean): Wallet;

                    /**
                     * Calls Send.
                     * @param request SendRequest message or plain object
                     * @param callback Node-style callback called with the error, if any, and Transaction
                     */
                    public send(request: de.flashyotter.blockchain_node.grpc.ISendRequest, callback: de.flashyotter.blockchain_node.grpc.Wallet.SendCallback): void;

                    /**
                     * Calls Send.
                     * @param request SendRequest message or plain object
                     * @returns Promise
                     */
                    public send(request: de.flashyotter.blockchain_node.grpc.ISendRequest): Promise<de.flashyotter.blockchain_node.grpc.Transaction>;

                    /**
                     * Calls Info.
                     * @param request Empty message or plain object
                     * @param callback Node-style callback called with the error, if any, and WalletInfo
                     */
                    public info(request: de.flashyotter.blockchain_node.grpc.IEmpty, callback: de.flashyotter.blockchain_node.grpc.Wallet.InfoCallback): void;

                    /**
                     * Calls Info.
                     * @param request Empty message or plain object
                     * @returns Promise
                     */
                    public info(request: de.flashyotter.blockchain_node.grpc.IEmpty): Promise<de.flashyotter.blockchain_node.grpc.WalletInfo>;

                    /**
                     * Calls History.
                     * @param request HistoryRequest message or plain object
                     * @param callback Node-style callback called with the error, if any, and TxList
                     */
                    public history(request: de.flashyotter.blockchain_node.grpc.IHistoryRequest, callback: de.flashyotter.blockchain_node.grpc.Wallet.HistoryCallback): void;

                    /**
                     * Calls History.
                     * @param request HistoryRequest message or plain object
                     * @returns Promise
                     */
                    public history(request: de.flashyotter.blockchain_node.grpc.IHistoryRequest): Promise<de.flashyotter.blockchain_node.grpc.TxList>;
                }

                namespace Wallet {

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#send}.
                     * @param error Error, if any
                     * @param [response] Transaction
                     */
                    type SendCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.Transaction) => void;

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#info}.
                     * @param error Error, if any
                     * @param [response] WalletInfo
                     */
                    type InfoCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.WalletInfo) => void;

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#history}.
                     * @param error Error, if any
                     * @param [response] TxList
                     */
                    type HistoryCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.TxList) => void;
                }

                /** Represents a Chain */
                class Chain extends $protobuf.rpc.Service {

                    /**
                     * Constructs a new Chain service.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     */
                    constructor(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean);

                    /**
                     * Creates new Chain service using the specified rpc implementation.
                     * @param rpcImpl RPC implementation
                     * @param [requestDelimited=false] Whether requests are length-delimited
                     * @param [responseDelimited=false] Whether responses are length-delimited
                     * @returns RPC service. Useful where requests and/or responses are streamed.
                     */
                    public static create(rpcImpl: $protobuf.RPCImpl, requestDelimited?: boolean, responseDelimited?: boolean): Chain;

                    /**
                     * Calls Latest.
                     * @param request Empty message or plain object
                     * @param callback Node-style callback called with the error, if any, and Block
                     */
                    public latest(request: de.flashyotter.blockchain_node.grpc.IEmpty, callback: de.flashyotter.blockchain_node.grpc.Chain.LatestCallback): void;

                    /**
                     * Calls Latest.
                     * @param request Empty message or plain object
                     * @returns Promise
                     */
                    public latest(request: de.flashyotter.blockchain_node.grpc.IEmpty): Promise<de.flashyotter.blockchain_node.grpc.Block>;

                    /**
                     * Calls Page.
                     * @param request PageRequest message or plain object
                     * @param callback Node-style callback called with the error, if any, and BlockList
                     */
                    public page(request: de.flashyotter.blockchain_node.grpc.IPageRequest, callback: de.flashyotter.blockchain_node.grpc.Chain.PageCallback): void;

                    /**
                     * Calls Page.
                     * @param request PageRequest message or plain object
                     * @returns Promise
                     */
                    public page(request: de.flashyotter.blockchain_node.grpc.IPageRequest): Promise<de.flashyotter.blockchain_node.grpc.BlockList>;
                }

                namespace Chain {

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Chain#latest}.
                     * @param error Error, if any
                     * @param [response] Block
                     */
                    type LatestCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.Block) => void;

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Chain#page}.
                     * @param error Error, if any
                     * @param [response] BlockList
                     */
                    type PageCallback = (error: (Error|null), response?: de.flashyotter.blockchain_node.grpc.BlockList) => void;
                }
            }
        }
    }
}
