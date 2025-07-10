/*eslint-disable block-scoped-var, id-length, no-control-regex, no-magic-numbers, no-prototype-builtins, no-redeclare, no-shadow, no-var, sort-vars*/
import * as $protobuf from "protobufjs/minimal";

// Common aliases
const $Reader = $protobuf.Reader, $Writer = $protobuf.Writer, $util = $protobuf.util;

// Exported root namespace
const $root = $protobuf.roots["default"] || ($protobuf.roots["default"] = {});

export const de = $root.de = (() => {

    /**
     * Namespace de.
     * @exports de
     * @namespace
     */
    const de = {};

    de.flashyotter = (function() {

        /**
         * Namespace flashyotter.
         * @memberof de
         * @namespace
         */
        const flashyotter = {};

        flashyotter.blockchain_node = (function() {

            /**
             * Namespace blockchain_node.
             * @memberof de.flashyotter
             * @namespace
             */
            const blockchain_node = {};

            blockchain_node.grpc = (function() {

                /**
                 * Namespace grpc.
                 * @memberof de.flashyotter.blockchain_node
                 * @namespace
                 */
                const grpc = {};

                grpc.Empty = (function() {

                    /**
                     * Properties of an Empty.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IEmpty
                     */

                    /**
                     * Constructs a new Empty.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents an Empty.
                     * @implements IEmpty
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty=} [properties] Properties to set
                     */
                    function Empty(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * Creates a new Empty instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.Empty} Empty instance
                     */
                    Empty.create = function create(properties) {
                        return new Empty(properties);
                    };

                    /**
                     * Encodes the specified Empty message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Empty.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} message Empty message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Empty.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        return writer;
                    };

                    /**
                     * Encodes the specified Empty message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Empty.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} message Empty message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Empty.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes an Empty message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.Empty} Empty
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Empty.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.Empty();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes an Empty message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.Empty} Empty
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Empty.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies an Empty message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    Empty.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        return null;
                    };

                    /**
                     * Creates an Empty message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.Empty} Empty
                     */
                    Empty.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.Empty)
                            return object;
                        return new $root.de.flashyotter.blockchain_node.grpc.Empty();
                    };

                    /**
                     * Creates a plain object from an Empty message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.Empty} message Empty
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    Empty.toObject = function toObject() {
                        return {};
                    };

                    /**
                     * Converts this Empty to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    Empty.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for Empty
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.Empty
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    Empty.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.Empty";
                    };

                    return Empty;
                })();

                grpc.TxInput = (function() {

                    /**
                     * Properties of a TxInput.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface ITxInput
                     * @property {string|null} [referencedOutputId] TxInput referencedOutputId
                     * @property {Uint8Array|null} [signature] TxInput signature
                     * @property {Uint8Array|null} [sender] TxInput sender
                     */

                    /**
                     * Constructs a new TxInput.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a TxInput.
                     * @implements ITxInput
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.ITxInput=} [properties] Properties to set
                     */
                    function TxInput(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * TxInput referencedOutputId.
                     * @member {string} referencedOutputId
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @instance
                     */
                    TxInput.prototype.referencedOutputId = "";

                    /**
                     * TxInput signature.
                     * @member {Uint8Array} signature
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @instance
                     */
                    TxInput.prototype.signature = $util.newBuffer([]);

                    /**
                     * TxInput sender.
                     * @member {Uint8Array} sender
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @instance
                     */
                    TxInput.prototype.sender = $util.newBuffer([]);

                    /**
                     * Creates a new TxInput instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxInput=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.TxInput} TxInput instance
                     */
                    TxInput.create = function create(properties) {
                        return new TxInput(properties);
                    };

                    /**
                     * Encodes the specified TxInput message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxInput.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxInput} message TxInput message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxInput.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.referencedOutputId != null && Object.hasOwnProperty.call(message, "referencedOutputId"))
                            writer.uint32(/* id 1, wireType 2 =*/10).string(message.referencedOutputId);
                        if (message.signature != null && Object.hasOwnProperty.call(message, "signature"))
                            writer.uint32(/* id 2, wireType 2 =*/18).bytes(message.signature);
                        if (message.sender != null && Object.hasOwnProperty.call(message, "sender"))
                            writer.uint32(/* id 3, wireType 2 =*/26).bytes(message.sender);
                        return writer;
                    };

                    /**
                     * Encodes the specified TxInput message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxInput.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxInput} message TxInput message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxInput.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a TxInput message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.TxInput} TxInput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxInput.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.TxInput();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.referencedOutputId = reader.string();
                                    break;
                                }
                            case 2: {
                                    message.signature = reader.bytes();
                                    break;
                                }
                            case 3: {
                                    message.sender = reader.bytes();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a TxInput message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.TxInput} TxInput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxInput.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a TxInput message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    TxInput.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.referencedOutputId != null && message.hasOwnProperty("referencedOutputId"))
                            if (!$util.isString(message.referencedOutputId))
                                return "referencedOutputId: string expected";
                        if (message.signature != null && message.hasOwnProperty("signature"))
                            if (!(message.signature && typeof message.signature.length === "number" || $util.isString(message.signature)))
                                return "signature: buffer expected";
                        if (message.sender != null && message.hasOwnProperty("sender"))
                            if (!(message.sender && typeof message.sender.length === "number" || $util.isString(message.sender)))
                                return "sender: buffer expected";
                        return null;
                    };

                    /**
                     * Creates a TxInput message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.TxInput} TxInput
                     */
                    TxInput.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.TxInput)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.TxInput();
                        if (object.referencedOutputId != null)
                            message.referencedOutputId = String(object.referencedOutputId);
                        if (object.signature != null)
                            if (typeof object.signature === "string")
                                $util.base64.decode(object.signature, message.signature = $util.newBuffer($util.base64.length(object.signature)), 0);
                            else if (object.signature.length >= 0)
                                message.signature = object.signature;
                        if (object.sender != null)
                            if (typeof object.sender === "string")
                                $util.base64.decode(object.sender, message.sender = $util.newBuffer($util.base64.length(object.sender)), 0);
                            else if (object.sender.length >= 0)
                                message.sender = object.sender;
                        return message;
                    };

                    /**
                     * Creates a plain object from a TxInput message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.TxInput} message TxInput
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    TxInput.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.referencedOutputId = "";
                            if (options.bytes === String)
                                object.signature = "";
                            else {
                                object.signature = [];
                                if (options.bytes !== Array)
                                    object.signature = $util.newBuffer(object.signature);
                            }
                            if (options.bytes === String)
                                object.sender = "";
                            else {
                                object.sender = [];
                                if (options.bytes !== Array)
                                    object.sender = $util.newBuffer(object.sender);
                            }
                        }
                        if (message.referencedOutputId != null && message.hasOwnProperty("referencedOutputId"))
                            object.referencedOutputId = message.referencedOutputId;
                        if (message.signature != null && message.hasOwnProperty("signature"))
                            object.signature = options.bytes === String ? $util.base64.encode(message.signature, 0, message.signature.length) : options.bytes === Array ? Array.prototype.slice.call(message.signature) : message.signature;
                        if (message.sender != null && message.hasOwnProperty("sender"))
                            object.sender = options.bytes === String ? $util.base64.encode(message.sender, 0, message.sender.length) : options.bytes === Array ? Array.prototype.slice.call(message.sender) : message.sender;
                        return object;
                    };

                    /**
                     * Converts this TxInput to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    TxInput.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for TxInput
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.TxInput
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    TxInput.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.TxInput";
                    };

                    return TxInput;
                })();

                grpc.TxOutput = (function() {

                    /**
                     * Properties of a TxOutput.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface ITxOutput
                     * @property {number|null} [value] TxOutput value
                     * @property {string|null} [recipientAddress] TxOutput recipientAddress
                     */

                    /**
                     * Constructs a new TxOutput.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a TxOutput.
                     * @implements ITxOutput
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.ITxOutput=} [properties] Properties to set
                     */
                    function TxOutput(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * TxOutput value.
                     * @member {number} value
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @instance
                     */
                    TxOutput.prototype.value = 0;

                    /**
                     * TxOutput recipientAddress.
                     * @member {string} recipientAddress
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @instance
                     */
                    TxOutput.prototype.recipientAddress = "";

                    /**
                     * Creates a new TxOutput instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxOutput=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.TxOutput} TxOutput instance
                     */
                    TxOutput.create = function create(properties) {
                        return new TxOutput(properties);
                    };

                    /**
                     * Encodes the specified TxOutput message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxOutput.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxOutput} message TxOutput message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxOutput.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.value != null && Object.hasOwnProperty.call(message, "value"))
                            writer.uint32(/* id 1, wireType 1 =*/9).double(message.value);
                        if (message.recipientAddress != null && Object.hasOwnProperty.call(message, "recipientAddress"))
                            writer.uint32(/* id 2, wireType 2 =*/18).string(message.recipientAddress);
                        return writer;
                    };

                    /**
                     * Encodes the specified TxOutput message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxOutput.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxOutput} message TxOutput message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxOutput.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a TxOutput message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.TxOutput} TxOutput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxOutput.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.TxOutput();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.value = reader.double();
                                    break;
                                }
                            case 2: {
                                    message.recipientAddress = reader.string();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a TxOutput message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.TxOutput} TxOutput
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxOutput.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a TxOutput message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    TxOutput.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.value != null && message.hasOwnProperty("value"))
                            if (typeof message.value !== "number")
                                return "value: number expected";
                        if (message.recipientAddress != null && message.hasOwnProperty("recipientAddress"))
                            if (!$util.isString(message.recipientAddress))
                                return "recipientAddress: string expected";
                        return null;
                    };

                    /**
                     * Creates a TxOutput message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.TxOutput} TxOutput
                     */
                    TxOutput.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.TxOutput)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.TxOutput();
                        if (object.value != null)
                            message.value = Number(object.value);
                        if (object.recipientAddress != null)
                            message.recipientAddress = String(object.recipientAddress);
                        return message;
                    };

                    /**
                     * Creates a plain object from a TxOutput message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.TxOutput} message TxOutput
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    TxOutput.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.value = 0;
                            object.recipientAddress = "";
                        }
                        if (message.value != null && message.hasOwnProperty("value"))
                            object.value = options.json && !isFinite(message.value) ? String(message.value) : message.value;
                        if (message.recipientAddress != null && message.hasOwnProperty("recipientAddress"))
                            object.recipientAddress = message.recipientAddress;
                        return object;
                    };

                    /**
                     * Converts this TxOutput to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    TxOutput.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for TxOutput
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.TxOutput
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    TxOutput.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.TxOutput";
                    };

                    return TxOutput;
                })();

                grpc.Transaction = (function() {

                    /**
                     * Properties of a Transaction.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface ITransaction
                     * @property {Array.<de.flashyotter.blockchain_node.grpc.ITxInput>|null} [inputs] Transaction inputs
                     * @property {Array.<de.flashyotter.blockchain_node.grpc.ITxOutput>|null} [outputs] Transaction outputs
                     * @property {number|null} [maxFee] Transaction maxFee
                     * @property {number|null} [tip] Transaction tip
                     */

                    /**
                     * Constructs a new Transaction.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a Transaction.
                     * @implements ITransaction
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.ITransaction=} [properties] Properties to set
                     */
                    function Transaction(properties) {
                        this.inputs = [];
                        this.outputs = [];
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * Transaction inputs.
                     * @member {Array.<de.flashyotter.blockchain_node.grpc.ITxInput>} inputs
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @instance
                     */
                    Transaction.prototype.inputs = $util.emptyArray;

                    /**
                     * Transaction outputs.
                     * @member {Array.<de.flashyotter.blockchain_node.grpc.ITxOutput>} outputs
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @instance
                     */
                    Transaction.prototype.outputs = $util.emptyArray;

                    /**
                     * Transaction maxFee.
                     * @member {number} maxFee
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @instance
                     */
                    Transaction.prototype.maxFee = 0;

                    /**
                     * Transaction tip.
                     * @member {number} tip
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @instance
                     */
                    Transaction.prototype.tip = 0;

                    /**
                     * Creates a new Transaction instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITransaction=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.Transaction} Transaction instance
                     */
                    Transaction.create = function create(properties) {
                        return new Transaction(properties);
                    };

                    /**
                     * Encodes the specified Transaction message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Transaction.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITransaction} message Transaction message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Transaction.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.inputs != null && message.inputs.length)
                            for (let i = 0; i < message.inputs.length; ++i)
                                $root.de.flashyotter.blockchain_node.grpc.TxInput.encode(message.inputs[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
                        if (message.outputs != null && message.outputs.length)
                            for (let i = 0; i < message.outputs.length; ++i)
                                $root.de.flashyotter.blockchain_node.grpc.TxOutput.encode(message.outputs[i], writer.uint32(/* id 2, wireType 2 =*/18).fork()).ldelim();
                        if (message.maxFee != null && Object.hasOwnProperty.call(message, "maxFee"))
                            writer.uint32(/* id 3, wireType 1 =*/25).double(message.maxFee);
                        if (message.tip != null && Object.hasOwnProperty.call(message, "tip"))
                            writer.uint32(/* id 4, wireType 1 =*/33).double(message.tip);
                        return writer;
                    };

                    /**
                     * Encodes the specified Transaction message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Transaction.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITransaction} message Transaction message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Transaction.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a Transaction message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.Transaction} Transaction
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Transaction.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.Transaction();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    if (!(message.inputs && message.inputs.length))
                                        message.inputs = [];
                                    message.inputs.push($root.de.flashyotter.blockchain_node.grpc.TxInput.decode(reader, reader.uint32()));
                                    break;
                                }
                            case 2: {
                                    if (!(message.outputs && message.outputs.length))
                                        message.outputs = [];
                                    message.outputs.push($root.de.flashyotter.blockchain_node.grpc.TxOutput.decode(reader, reader.uint32()));
                                    break;
                                }
                            case 3: {
                                    message.maxFee = reader.double();
                                    break;
                                }
                            case 4: {
                                    message.tip = reader.double();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a Transaction message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.Transaction} Transaction
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Transaction.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a Transaction message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    Transaction.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.inputs != null && message.hasOwnProperty("inputs")) {
                            if (!Array.isArray(message.inputs))
                                return "inputs: array expected";
                            for (let i = 0; i < message.inputs.length; ++i) {
                                let error = $root.de.flashyotter.blockchain_node.grpc.TxInput.verify(message.inputs[i]);
                                if (error)
                                    return "inputs." + error;
                            }
                        }
                        if (message.outputs != null && message.hasOwnProperty("outputs")) {
                            if (!Array.isArray(message.outputs))
                                return "outputs: array expected";
                            for (let i = 0; i < message.outputs.length; ++i) {
                                let error = $root.de.flashyotter.blockchain_node.grpc.TxOutput.verify(message.outputs[i]);
                                if (error)
                                    return "outputs." + error;
                            }
                        }
                        if (message.maxFee != null && message.hasOwnProperty("maxFee"))
                            if (typeof message.maxFee !== "number")
                                return "maxFee: number expected";
                        if (message.tip != null && message.hasOwnProperty("tip"))
                            if (typeof message.tip !== "number")
                                return "tip: number expected";
                        return null;
                    };

                    /**
                     * Creates a Transaction message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.Transaction} Transaction
                     */
                    Transaction.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.Transaction)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.Transaction();
                        if (object.inputs) {
                            if (!Array.isArray(object.inputs))
                                throw TypeError(".de.flashyotter.blockchain_node.grpc.Transaction.inputs: array expected");
                            message.inputs = [];
                            for (let i = 0; i < object.inputs.length; ++i) {
                                if (typeof object.inputs[i] !== "object")
                                    throw TypeError(".de.flashyotter.blockchain_node.grpc.Transaction.inputs: object expected");
                                message.inputs[i] = $root.de.flashyotter.blockchain_node.grpc.TxInput.fromObject(object.inputs[i]);
                            }
                        }
                        if (object.outputs) {
                            if (!Array.isArray(object.outputs))
                                throw TypeError(".de.flashyotter.blockchain_node.grpc.Transaction.outputs: array expected");
                            message.outputs = [];
                            for (let i = 0; i < object.outputs.length; ++i) {
                                if (typeof object.outputs[i] !== "object")
                                    throw TypeError(".de.flashyotter.blockchain_node.grpc.Transaction.outputs: object expected");
                                message.outputs[i] = $root.de.flashyotter.blockchain_node.grpc.TxOutput.fromObject(object.outputs[i]);
                            }
                        }
                        if (object.maxFee != null)
                            message.maxFee = Number(object.maxFee);
                        if (object.tip != null)
                            message.tip = Number(object.tip);
                        return message;
                    };

                    /**
                     * Creates a plain object from a Transaction message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.Transaction} message Transaction
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    Transaction.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.arrays || options.defaults) {
                            object.inputs = [];
                            object.outputs = [];
                        }
                        if (options.defaults) {
                            object.maxFee = 0;
                            object.tip = 0;
                        }
                        if (message.inputs && message.inputs.length) {
                            object.inputs = [];
                            for (let j = 0; j < message.inputs.length; ++j)
                                object.inputs[j] = $root.de.flashyotter.blockchain_node.grpc.TxInput.toObject(message.inputs[j], options);
                        }
                        if (message.outputs && message.outputs.length) {
                            object.outputs = [];
                            for (let j = 0; j < message.outputs.length; ++j)
                                object.outputs[j] = $root.de.flashyotter.blockchain_node.grpc.TxOutput.toObject(message.outputs[j], options);
                        }
                        if (message.maxFee != null && message.hasOwnProperty("maxFee"))
                            object.maxFee = options.json && !isFinite(message.maxFee) ? String(message.maxFee) : message.maxFee;
                        if (message.tip != null && message.hasOwnProperty("tip"))
                            object.tip = options.json && !isFinite(message.tip) ? String(message.tip) : message.tip;
                        return object;
                    };

                    /**
                     * Converts this Transaction to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    Transaction.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for Transaction
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.Transaction
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    Transaction.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.Transaction";
                    };

                    return Transaction;
                })();

                grpc.Block = (function() {

                    /**
                     * Properties of a Block.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IBlock
                     * @property {number|null} [height] Block height
                     * @property {string|null} [previousHashHex] Block previousHashHex
                     * @property {number|Long|null} [timeMillis] Block timeMillis
                     * @property {number|null} [compactBits] Block compactBits
                     * @property {number|null} [nonce] Block nonce
                     * @property {string|null} [merkleRootHex] Block merkleRootHex
                     * @property {Array.<de.flashyotter.blockchain_node.grpc.ITransaction>|null} [txList] Block txList
                     */

                    /**
                     * Constructs a new Block.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a Block.
                     * @implements IBlock
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IBlock=} [properties] Properties to set
                     */
                    function Block(properties) {
                        this.txList = [];
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * Block height.
                     * @member {number} height
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.height = 0;

                    /**
                     * Block previousHashHex.
                     * @member {string} previousHashHex
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.previousHashHex = "";

                    /**
                     * Block timeMillis.
                     * @member {number|Long} timeMillis
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.timeMillis = $util.Long ? $util.Long.fromBits(0,0,false) : 0;

                    /**
                     * Block compactBits.
                     * @member {number} compactBits
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.compactBits = 0;

                    /**
                     * Block nonce.
                     * @member {number} nonce
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.nonce = 0;

                    /**
                     * Block merkleRootHex.
                     * @member {string} merkleRootHex
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.merkleRootHex = "";

                    /**
                     * Block txList.
                     * @member {Array.<de.flashyotter.blockchain_node.grpc.ITransaction>} txList
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     */
                    Block.prototype.txList = $util.emptyArray;

                    /**
                     * Creates a new Block instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlock=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.Block} Block instance
                     */
                    Block.create = function create(properties) {
                        return new Block(properties);
                    };

                    /**
                     * Encodes the specified Block message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Block.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlock} message Block message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Block.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.height != null && Object.hasOwnProperty.call(message, "height"))
                            writer.uint32(/* id 1, wireType 0 =*/8).int32(message.height);
                        if (message.previousHashHex != null && Object.hasOwnProperty.call(message, "previousHashHex"))
                            writer.uint32(/* id 2, wireType 2 =*/18).string(message.previousHashHex);
                        if (message.timeMillis != null && Object.hasOwnProperty.call(message, "timeMillis"))
                            writer.uint32(/* id 3, wireType 0 =*/24).int64(message.timeMillis);
                        if (message.compactBits != null && Object.hasOwnProperty.call(message, "compactBits"))
                            writer.uint32(/* id 4, wireType 0 =*/32).int32(message.compactBits);
                        if (message.nonce != null && Object.hasOwnProperty.call(message, "nonce"))
                            writer.uint32(/* id 5, wireType 0 =*/40).int32(message.nonce);
                        if (message.merkleRootHex != null && Object.hasOwnProperty.call(message, "merkleRootHex"))
                            writer.uint32(/* id 6, wireType 2 =*/50).string(message.merkleRootHex);
                        if (message.txList != null && message.txList.length)
                            for (let i = 0; i < message.txList.length; ++i)
                                $root.de.flashyotter.blockchain_node.grpc.Transaction.encode(message.txList[i], writer.uint32(/* id 7, wireType 2 =*/58).fork()).ldelim();
                        return writer;
                    };

                    /**
                     * Encodes the specified Block message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.Block.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlock} message Block message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    Block.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a Block message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.Block} Block
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Block.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.Block();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.height = reader.int32();
                                    break;
                                }
                            case 2: {
                                    message.previousHashHex = reader.string();
                                    break;
                                }
                            case 3: {
                                    message.timeMillis = reader.int64();
                                    break;
                                }
                            case 4: {
                                    message.compactBits = reader.int32();
                                    break;
                                }
                            case 5: {
                                    message.nonce = reader.int32();
                                    break;
                                }
                            case 6: {
                                    message.merkleRootHex = reader.string();
                                    break;
                                }
                            case 7: {
                                    if (!(message.txList && message.txList.length))
                                        message.txList = [];
                                    message.txList.push($root.de.flashyotter.blockchain_node.grpc.Transaction.decode(reader, reader.uint32()));
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a Block message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.Block} Block
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    Block.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a Block message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    Block.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.height != null && message.hasOwnProperty("height"))
                            if (!$util.isInteger(message.height))
                                return "height: integer expected";
                        if (message.previousHashHex != null && message.hasOwnProperty("previousHashHex"))
                            if (!$util.isString(message.previousHashHex))
                                return "previousHashHex: string expected";
                        if (message.timeMillis != null && message.hasOwnProperty("timeMillis"))
                            if (!$util.isInteger(message.timeMillis) && !(message.timeMillis && $util.isInteger(message.timeMillis.low) && $util.isInteger(message.timeMillis.high)))
                                return "timeMillis: integer|Long expected";
                        if (message.compactBits != null && message.hasOwnProperty("compactBits"))
                            if (!$util.isInteger(message.compactBits))
                                return "compactBits: integer expected";
                        if (message.nonce != null && message.hasOwnProperty("nonce"))
                            if (!$util.isInteger(message.nonce))
                                return "nonce: integer expected";
                        if (message.merkleRootHex != null && message.hasOwnProperty("merkleRootHex"))
                            if (!$util.isString(message.merkleRootHex))
                                return "merkleRootHex: string expected";
                        if (message.txList != null && message.hasOwnProperty("txList")) {
                            if (!Array.isArray(message.txList))
                                return "txList: array expected";
                            for (let i = 0; i < message.txList.length; ++i) {
                                let error = $root.de.flashyotter.blockchain_node.grpc.Transaction.verify(message.txList[i]);
                                if (error)
                                    return "txList." + error;
                            }
                        }
                        return null;
                    };

                    /**
                     * Creates a Block message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.Block} Block
                     */
                    Block.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.Block)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.Block();
                        if (object.height != null)
                            message.height = object.height | 0;
                        if (object.previousHashHex != null)
                            message.previousHashHex = String(object.previousHashHex);
                        if (object.timeMillis != null)
                            if ($util.Long)
                                (message.timeMillis = $util.Long.fromValue(object.timeMillis)).unsigned = false;
                            else if (typeof object.timeMillis === "string")
                                message.timeMillis = parseInt(object.timeMillis, 10);
                            else if (typeof object.timeMillis === "number")
                                message.timeMillis = object.timeMillis;
                            else if (typeof object.timeMillis === "object")
                                message.timeMillis = new $util.LongBits(object.timeMillis.low >>> 0, object.timeMillis.high >>> 0).toNumber();
                        if (object.compactBits != null)
                            message.compactBits = object.compactBits | 0;
                        if (object.nonce != null)
                            message.nonce = object.nonce | 0;
                        if (object.merkleRootHex != null)
                            message.merkleRootHex = String(object.merkleRootHex);
                        if (object.txList) {
                            if (!Array.isArray(object.txList))
                                throw TypeError(".de.flashyotter.blockchain_node.grpc.Block.txList: array expected");
                            message.txList = [];
                            for (let i = 0; i < object.txList.length; ++i) {
                                if (typeof object.txList[i] !== "object")
                                    throw TypeError(".de.flashyotter.blockchain_node.grpc.Block.txList: object expected");
                                message.txList[i] = $root.de.flashyotter.blockchain_node.grpc.Transaction.fromObject(object.txList[i]);
                            }
                        }
                        return message;
                    };

                    /**
                     * Creates a plain object from a Block message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.Block} message Block
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    Block.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.arrays || options.defaults)
                            object.txList = [];
                        if (options.defaults) {
                            object.height = 0;
                            object.previousHashHex = "";
                            if ($util.Long) {
                                let long = new $util.Long(0, 0, false);
                                object.timeMillis = options.longs === String ? long.toString() : options.longs === Number ? long.toNumber() : long;
                            } else
                                object.timeMillis = options.longs === String ? "0" : 0;
                            object.compactBits = 0;
                            object.nonce = 0;
                            object.merkleRootHex = "";
                        }
                        if (message.height != null && message.hasOwnProperty("height"))
                            object.height = message.height;
                        if (message.previousHashHex != null && message.hasOwnProperty("previousHashHex"))
                            object.previousHashHex = message.previousHashHex;
                        if (message.timeMillis != null && message.hasOwnProperty("timeMillis"))
                            if (typeof message.timeMillis === "number")
                                object.timeMillis = options.longs === String ? String(message.timeMillis) : message.timeMillis;
                            else
                                object.timeMillis = options.longs === String ? $util.Long.prototype.toString.call(message.timeMillis) : options.longs === Number ? new $util.LongBits(message.timeMillis.low >>> 0, message.timeMillis.high >>> 0).toNumber() : message.timeMillis;
                        if (message.compactBits != null && message.hasOwnProperty("compactBits"))
                            object.compactBits = message.compactBits;
                        if (message.nonce != null && message.hasOwnProperty("nonce"))
                            object.nonce = message.nonce;
                        if (message.merkleRootHex != null && message.hasOwnProperty("merkleRootHex"))
                            object.merkleRootHex = message.merkleRootHex;
                        if (message.txList && message.txList.length) {
                            object.txList = [];
                            for (let j = 0; j < message.txList.length; ++j)
                                object.txList[j] = $root.de.flashyotter.blockchain_node.grpc.Transaction.toObject(message.txList[j], options);
                        }
                        return object;
                    };

                    /**
                     * Converts this Block to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    Block.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for Block
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.Block
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    Block.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.Block";
                    };

                    return Block;
                })();

                grpc.BlockList = (function() {

                    /**
                     * Properties of a BlockList.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IBlockList
                     * @property {Array.<de.flashyotter.blockchain_node.grpc.IBlock>|null} [blocks] BlockList blocks
                     */

                    /**
                     * Constructs a new BlockList.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a BlockList.
                     * @implements IBlockList
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IBlockList=} [properties] Properties to set
                     */
                    function BlockList(properties) {
                        this.blocks = [];
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * BlockList blocks.
                     * @member {Array.<de.flashyotter.blockchain_node.grpc.IBlock>} blocks
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @instance
                     */
                    BlockList.prototype.blocks = $util.emptyArray;

                    /**
                     * Creates a new BlockList instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlockList=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.BlockList} BlockList instance
                     */
                    BlockList.create = function create(properties) {
                        return new BlockList(properties);
                    };

                    /**
                     * Encodes the specified BlockList message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.BlockList.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlockList} message BlockList message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    BlockList.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.blocks != null && message.blocks.length)
                            for (let i = 0; i < message.blocks.length; ++i)
                                $root.de.flashyotter.blockchain_node.grpc.Block.encode(message.blocks[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
                        return writer;
                    };

                    /**
                     * Encodes the specified BlockList message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.BlockList.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IBlockList} message BlockList message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    BlockList.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a BlockList message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.BlockList} BlockList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    BlockList.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.BlockList();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    if (!(message.blocks && message.blocks.length))
                                        message.blocks = [];
                                    message.blocks.push($root.de.flashyotter.blockchain_node.grpc.Block.decode(reader, reader.uint32()));
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a BlockList message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.BlockList} BlockList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    BlockList.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a BlockList message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    BlockList.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.blocks != null && message.hasOwnProperty("blocks")) {
                            if (!Array.isArray(message.blocks))
                                return "blocks: array expected";
                            for (let i = 0; i < message.blocks.length; ++i) {
                                let error = $root.de.flashyotter.blockchain_node.grpc.Block.verify(message.blocks[i]);
                                if (error)
                                    return "blocks." + error;
                            }
                        }
                        return null;
                    };

                    /**
                     * Creates a BlockList message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.BlockList} BlockList
                     */
                    BlockList.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.BlockList)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.BlockList();
                        if (object.blocks) {
                            if (!Array.isArray(object.blocks))
                                throw TypeError(".de.flashyotter.blockchain_node.grpc.BlockList.blocks: array expected");
                            message.blocks = [];
                            for (let i = 0; i < object.blocks.length; ++i) {
                                if (typeof object.blocks[i] !== "object")
                                    throw TypeError(".de.flashyotter.blockchain_node.grpc.BlockList.blocks: object expected");
                                message.blocks[i] = $root.de.flashyotter.blockchain_node.grpc.Block.fromObject(object.blocks[i]);
                            }
                        }
                        return message;
                    };

                    /**
                     * Creates a plain object from a BlockList message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.BlockList} message BlockList
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    BlockList.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.arrays || options.defaults)
                            object.blocks = [];
                        if (message.blocks && message.blocks.length) {
                            object.blocks = [];
                            for (let j = 0; j < message.blocks.length; ++j)
                                object.blocks[j] = $root.de.flashyotter.blockchain_node.grpc.Block.toObject(message.blocks[j], options);
                        }
                        return object;
                    };

                    /**
                     * Converts this BlockList to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    BlockList.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for BlockList
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.BlockList
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    BlockList.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.BlockList";
                    };

                    return BlockList;
                })();

                grpc.PageRequest = (function() {

                    /**
                     * Properties of a PageRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IPageRequest
                     * @property {number|null} [page] PageRequest page
                     * @property {number|null} [size] PageRequest size
                     */

                    /**
                     * Constructs a new PageRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a PageRequest.
                     * @implements IPageRequest
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest=} [properties] Properties to set
                     */
                    function PageRequest(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * PageRequest page.
                     * @member {number} page
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @instance
                     */
                    PageRequest.prototype.page = 0;

                    /**
                     * PageRequest size.
                     * @member {number} size
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @instance
                     */
                    PageRequest.prototype.size = 0;

                    /**
                     * Creates a new PageRequest instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.PageRequest} PageRequest instance
                     */
                    PageRequest.create = function create(properties) {
                        return new PageRequest(properties);
                    };

                    /**
                     * Encodes the specified PageRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.PageRequest.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest} message PageRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    PageRequest.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.page != null && Object.hasOwnProperty.call(message, "page"))
                            writer.uint32(/* id 1, wireType 0 =*/8).int32(message.page);
                        if (message.size != null && Object.hasOwnProperty.call(message, "size"))
                            writer.uint32(/* id 2, wireType 0 =*/16).int32(message.size);
                        return writer;
                    };

                    /**
                     * Encodes the specified PageRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.PageRequest.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest} message PageRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    PageRequest.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a PageRequest message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.PageRequest} PageRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    PageRequest.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.PageRequest();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.page = reader.int32();
                                    break;
                                }
                            case 2: {
                                    message.size = reader.int32();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a PageRequest message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.PageRequest} PageRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    PageRequest.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a PageRequest message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    PageRequest.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.page != null && message.hasOwnProperty("page"))
                            if (!$util.isInteger(message.page))
                                return "page: integer expected";
                        if (message.size != null && message.hasOwnProperty("size"))
                            if (!$util.isInteger(message.size))
                                return "size: integer expected";
                        return null;
                    };

                    /**
                     * Creates a PageRequest message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.PageRequest} PageRequest
                     */
                    PageRequest.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.PageRequest)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.PageRequest();
                        if (object.page != null)
                            message.page = object.page | 0;
                        if (object.size != null)
                            message.size = object.size | 0;
                        return message;
                    };

                    /**
                     * Creates a plain object from a PageRequest message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.PageRequest} message PageRequest
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    PageRequest.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.page = 0;
                            object.size = 0;
                        }
                        if (message.page != null && message.hasOwnProperty("page"))
                            object.page = message.page;
                        if (message.size != null && message.hasOwnProperty("size"))
                            object.size = message.size;
                        return object;
                    };

                    /**
                     * Converts this PageRequest to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    PageRequest.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for PageRequest
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.PageRequest
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    PageRequest.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.PageRequest";
                    };

                    return PageRequest;
                })();

                grpc.HistoryRequest = (function() {

                    /**
                     * Properties of a HistoryRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IHistoryRequest
                     * @property {string|null} [address] HistoryRequest address
                     * @property {number|null} [limit] HistoryRequest limit
                     */

                    /**
                     * Constructs a new HistoryRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a HistoryRequest.
                     * @implements IHistoryRequest
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest=} [properties] Properties to set
                     */
                    function HistoryRequest(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * HistoryRequest address.
                     * @member {string} address
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @instance
                     */
                    HistoryRequest.prototype.address = "";

                    /**
                     * HistoryRequest limit.
                     * @member {number} limit
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @instance
                     */
                    HistoryRequest.prototype.limit = 0;

                    /**
                     * Creates a new HistoryRequest instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.HistoryRequest} HistoryRequest instance
                     */
                    HistoryRequest.create = function create(properties) {
                        return new HistoryRequest(properties);
                    };

                    /**
                     * Encodes the specified HistoryRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.HistoryRequest.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest} message HistoryRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    HistoryRequest.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.address != null && Object.hasOwnProperty.call(message, "address"))
                            writer.uint32(/* id 1, wireType 2 =*/10).string(message.address);
                        if (message.limit != null && Object.hasOwnProperty.call(message, "limit"))
                            writer.uint32(/* id 2, wireType 0 =*/16).int32(message.limit);
                        return writer;
                    };

                    /**
                     * Encodes the specified HistoryRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.HistoryRequest.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest} message HistoryRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    HistoryRequest.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a HistoryRequest message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.HistoryRequest} HistoryRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    HistoryRequest.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.HistoryRequest();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.address = reader.string();
                                    break;
                                }
                            case 2: {
                                    message.limit = reader.int32();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a HistoryRequest message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.HistoryRequest} HistoryRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    HistoryRequest.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a HistoryRequest message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    HistoryRequest.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.address != null && message.hasOwnProperty("address"))
                            if (!$util.isString(message.address))
                                return "address: string expected";
                        if (message.limit != null && message.hasOwnProperty("limit"))
                            if (!$util.isInteger(message.limit))
                                return "limit: integer expected";
                        return null;
                    };

                    /**
                     * Creates a HistoryRequest message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.HistoryRequest} HistoryRequest
                     */
                    HistoryRequest.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.HistoryRequest)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.HistoryRequest();
                        if (object.address != null)
                            message.address = String(object.address);
                        if (object.limit != null)
                            message.limit = object.limit | 0;
                        return message;
                    };

                    /**
                     * Creates a plain object from a HistoryRequest message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.HistoryRequest} message HistoryRequest
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    HistoryRequest.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.address = "";
                            object.limit = 0;
                        }
                        if (message.address != null && message.hasOwnProperty("address"))
                            object.address = message.address;
                        if (message.limit != null && message.hasOwnProperty("limit"))
                            object.limit = message.limit;
                        return object;
                    };

                    /**
                     * Converts this HistoryRequest to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    HistoryRequest.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for HistoryRequest
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.HistoryRequest
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    HistoryRequest.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.HistoryRequest";
                    };

                    return HistoryRequest;
                })();

                grpc.WalletInfo = (function() {

                    /**
                     * Properties of a WalletInfo.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface IWalletInfo
                     * @property {string|null} [address] WalletInfo address
                     * @property {number|null} [balance] WalletInfo balance
                     */

                    /**
                     * Constructs a new WalletInfo.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a WalletInfo.
                     * @implements IWalletInfo
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.IWalletInfo=} [properties] Properties to set
                     */
                    function WalletInfo(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * WalletInfo address.
                     * @member {string} address
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @instance
                     */
                    WalletInfo.prototype.address = "";

                    /**
                     * WalletInfo balance.
                     * @member {number} balance
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @instance
                     */
                    WalletInfo.prototype.balance = 0;

                    /**
                     * Creates a new WalletInfo instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IWalletInfo=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.WalletInfo} WalletInfo instance
                     */
                    WalletInfo.create = function create(properties) {
                        return new WalletInfo(properties);
                    };

                    /**
                     * Encodes the specified WalletInfo message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.WalletInfo.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IWalletInfo} message WalletInfo message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    WalletInfo.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.address != null && Object.hasOwnProperty.call(message, "address"))
                            writer.uint32(/* id 1, wireType 2 =*/10).string(message.address);
                        if (message.balance != null && Object.hasOwnProperty.call(message, "balance"))
                            writer.uint32(/* id 2, wireType 1 =*/17).double(message.balance);
                        return writer;
                    };

                    /**
                     * Encodes the specified WalletInfo message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.WalletInfo.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.IWalletInfo} message WalletInfo message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    WalletInfo.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a WalletInfo message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.WalletInfo} WalletInfo
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    WalletInfo.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.WalletInfo();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.address = reader.string();
                                    break;
                                }
                            case 2: {
                                    message.balance = reader.double();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a WalletInfo message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.WalletInfo} WalletInfo
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    WalletInfo.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a WalletInfo message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    WalletInfo.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.address != null && message.hasOwnProperty("address"))
                            if (!$util.isString(message.address))
                                return "address: string expected";
                        if (message.balance != null && message.hasOwnProperty("balance"))
                            if (typeof message.balance !== "number")
                                return "balance: number expected";
                        return null;
                    };

                    /**
                     * Creates a WalletInfo message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.WalletInfo} WalletInfo
                     */
                    WalletInfo.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.WalletInfo)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.WalletInfo();
                        if (object.address != null)
                            message.address = String(object.address);
                        if (object.balance != null)
                            message.balance = Number(object.balance);
                        return message;
                    };

                    /**
                     * Creates a plain object from a WalletInfo message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.WalletInfo} message WalletInfo
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    WalletInfo.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.address = "";
                            object.balance = 0;
                        }
                        if (message.address != null && message.hasOwnProperty("address"))
                            object.address = message.address;
                        if (message.balance != null && message.hasOwnProperty("balance"))
                            object.balance = options.json && !isFinite(message.balance) ? String(message.balance) : message.balance;
                        return object;
                    };

                    /**
                     * Converts this WalletInfo to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    WalletInfo.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for WalletInfo
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.WalletInfo
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    WalletInfo.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.WalletInfo";
                    };

                    return WalletInfo;
                })();

                grpc.SendRequest = (function() {

                    /**
                     * Properties of a SendRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface ISendRequest
                     * @property {string|null} [recipient] SendRequest recipient
                     * @property {number|null} [amount] SendRequest amount
                     */

                    /**
                     * Constructs a new SendRequest.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a SendRequest.
                     * @implements ISendRequest
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest=} [properties] Properties to set
                     */
                    function SendRequest(properties) {
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * SendRequest recipient.
                     * @member {string} recipient
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @instance
                     */
                    SendRequest.prototype.recipient = "";

                    /**
                     * SendRequest amount.
                     * @member {number} amount
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @instance
                     */
                    SendRequest.prototype.amount = 0;

                    /**
                     * Creates a new SendRequest instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.SendRequest} SendRequest instance
                     */
                    SendRequest.create = function create(properties) {
                        return new SendRequest(properties);
                    };

                    /**
                     * Encodes the specified SendRequest message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.SendRequest.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest} message SendRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    SendRequest.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.recipient != null && Object.hasOwnProperty.call(message, "recipient"))
                            writer.uint32(/* id 1, wireType 2 =*/10).string(message.recipient);
                        if (message.amount != null && Object.hasOwnProperty.call(message, "amount"))
                            writer.uint32(/* id 2, wireType 1 =*/17).double(message.amount);
                        return writer;
                    };

                    /**
                     * Encodes the specified SendRequest message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.SendRequest.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest} message SendRequest message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    SendRequest.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a SendRequest message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.SendRequest} SendRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    SendRequest.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.SendRequest();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    message.recipient = reader.string();
                                    break;
                                }
                            case 2: {
                                    message.amount = reader.double();
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a SendRequest message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.SendRequest} SendRequest
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    SendRequest.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a SendRequest message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    SendRequest.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.recipient != null && message.hasOwnProperty("recipient"))
                            if (!$util.isString(message.recipient))
                                return "recipient: string expected";
                        if (message.amount != null && message.hasOwnProperty("amount"))
                            if (typeof message.amount !== "number")
                                return "amount: number expected";
                        return null;
                    };

                    /**
                     * Creates a SendRequest message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.SendRequest} SendRequest
                     */
                    SendRequest.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.SendRequest)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.SendRequest();
                        if (object.recipient != null)
                            message.recipient = String(object.recipient);
                        if (object.amount != null)
                            message.amount = Number(object.amount);
                        return message;
                    };

                    /**
                     * Creates a plain object from a SendRequest message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.SendRequest} message SendRequest
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    SendRequest.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.defaults) {
                            object.recipient = "";
                            object.amount = 0;
                        }
                        if (message.recipient != null && message.hasOwnProperty("recipient"))
                            object.recipient = message.recipient;
                        if (message.amount != null && message.hasOwnProperty("amount"))
                            object.amount = options.json && !isFinite(message.amount) ? String(message.amount) : message.amount;
                        return object;
                    };

                    /**
                     * Converts this SendRequest to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    SendRequest.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for SendRequest
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.SendRequest
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    SendRequest.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.SendRequest";
                    };

                    return SendRequest;
                })();

                grpc.TxList = (function() {

                    /**
                     * Properties of a TxList.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @interface ITxList
                     * @property {Array.<de.flashyotter.blockchain_node.grpc.ITransaction>|null} [txs] TxList txs
                     */

                    /**
                     * Constructs a new TxList.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a TxList.
                     * @implements ITxList
                     * @constructor
                     * @param {de.flashyotter.blockchain_node.grpc.ITxList=} [properties] Properties to set
                     */
                    function TxList(properties) {
                        this.txs = [];
                        if (properties)
                            for (let keys = Object.keys(properties), i = 0; i < keys.length; ++i)
                                if (properties[keys[i]] != null)
                                    this[keys[i]] = properties[keys[i]];
                    }

                    /**
                     * TxList txs.
                     * @member {Array.<de.flashyotter.blockchain_node.grpc.ITransaction>} txs
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @instance
                     */
                    TxList.prototype.txs = $util.emptyArray;

                    /**
                     * Creates a new TxList instance using the specified properties.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxList=} [properties] Properties to set
                     * @returns {de.flashyotter.blockchain_node.grpc.TxList} TxList instance
                     */
                    TxList.create = function create(properties) {
                        return new TxList(properties);
                    };

                    /**
                     * Encodes the specified TxList message. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxList.verify|verify} messages.
                     * @function encode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxList} message TxList message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxList.encode = function encode(message, writer) {
                        if (!writer)
                            writer = $Writer.create();
                        if (message.txs != null && message.txs.length)
                            for (let i = 0; i < message.txs.length; ++i)
                                $root.de.flashyotter.blockchain_node.grpc.Transaction.encode(message.txs[i], writer.uint32(/* id 1, wireType 2 =*/10).fork()).ldelim();
                        return writer;
                    };

                    /**
                     * Encodes the specified TxList message, length delimited. Does not implicitly {@link de.flashyotter.blockchain_node.grpc.TxList.verify|verify} messages.
                     * @function encodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.ITxList} message TxList message or plain object to encode
                     * @param {$protobuf.Writer} [writer] Writer to encode to
                     * @returns {$protobuf.Writer} Writer
                     */
                    TxList.encodeDelimited = function encodeDelimited(message, writer) {
                        return this.encode(message, writer).ldelim();
                    };

                    /**
                     * Decodes a TxList message from the specified reader or buffer.
                     * @function decode
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @param {number} [length] Message length if known beforehand
                     * @returns {de.flashyotter.blockchain_node.grpc.TxList} TxList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxList.decode = function decode(reader, length, error) {
                        if (!(reader instanceof $Reader))
                            reader = $Reader.create(reader);
                        let end = length === undefined ? reader.len : reader.pos + length, message = new $root.de.flashyotter.blockchain_node.grpc.TxList();
                        while (reader.pos < end) {
                            let tag = reader.uint32();
                            if (tag === error)
                                break;
                            switch (tag >>> 3) {
                            case 1: {
                                    if (!(message.txs && message.txs.length))
                                        message.txs = [];
                                    message.txs.push($root.de.flashyotter.blockchain_node.grpc.Transaction.decode(reader, reader.uint32()));
                                    break;
                                }
                            default:
                                reader.skipType(tag & 7);
                                break;
                            }
                        }
                        return message;
                    };

                    /**
                     * Decodes a TxList message from the specified reader or buffer, length delimited.
                     * @function decodeDelimited
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {$protobuf.Reader|Uint8Array} reader Reader or buffer to decode from
                     * @returns {de.flashyotter.blockchain_node.grpc.TxList} TxList
                     * @throws {Error} If the payload is not a reader or valid buffer
                     * @throws {$protobuf.util.ProtocolError} If required fields are missing
                     */
                    TxList.decodeDelimited = function decodeDelimited(reader) {
                        if (!(reader instanceof $Reader))
                            reader = new $Reader(reader);
                        return this.decode(reader, reader.uint32());
                    };

                    /**
                     * Verifies a TxList message.
                     * @function verify
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {Object.<string,*>} message Plain object to verify
                     * @returns {string|null} `null` if valid, otherwise the reason why it is not
                     */
                    TxList.verify = function verify(message) {
                        if (typeof message !== "object" || message === null)
                            return "object expected";
                        if (message.txs != null && message.hasOwnProperty("txs")) {
                            if (!Array.isArray(message.txs))
                                return "txs: array expected";
                            for (let i = 0; i < message.txs.length; ++i) {
                                let error = $root.de.flashyotter.blockchain_node.grpc.Transaction.verify(message.txs[i]);
                                if (error)
                                    return "txs." + error;
                            }
                        }
                        return null;
                    };

                    /**
                     * Creates a TxList message from a plain object. Also converts values to their respective internal types.
                     * @function fromObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {Object.<string,*>} object Plain object
                     * @returns {de.flashyotter.blockchain_node.grpc.TxList} TxList
                     */
                    TxList.fromObject = function fromObject(object) {
                        if (object instanceof $root.de.flashyotter.blockchain_node.grpc.TxList)
                            return object;
                        let message = new $root.de.flashyotter.blockchain_node.grpc.TxList();
                        if (object.txs) {
                            if (!Array.isArray(object.txs))
                                throw TypeError(".de.flashyotter.blockchain_node.grpc.TxList.txs: array expected");
                            message.txs = [];
                            for (let i = 0; i < object.txs.length; ++i) {
                                if (typeof object.txs[i] !== "object")
                                    throw TypeError(".de.flashyotter.blockchain_node.grpc.TxList.txs: object expected");
                                message.txs[i] = $root.de.flashyotter.blockchain_node.grpc.Transaction.fromObject(object.txs[i]);
                            }
                        }
                        return message;
                    };

                    /**
                     * Creates a plain object from a TxList message. Also converts values to other types if specified.
                     * @function toObject
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {de.flashyotter.blockchain_node.grpc.TxList} message TxList
                     * @param {$protobuf.IConversionOptions} [options] Conversion options
                     * @returns {Object.<string,*>} Plain object
                     */
                    TxList.toObject = function toObject(message, options) {
                        if (!options)
                            options = {};
                        let object = {};
                        if (options.arrays || options.defaults)
                            object.txs = [];
                        if (message.txs && message.txs.length) {
                            object.txs = [];
                            for (let j = 0; j < message.txs.length; ++j)
                                object.txs[j] = $root.de.flashyotter.blockchain_node.grpc.Transaction.toObject(message.txs[j], options);
                        }
                        return object;
                    };

                    /**
                     * Converts this TxList to JSON.
                     * @function toJSON
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @instance
                     * @returns {Object.<string,*>} JSON object
                     */
                    TxList.prototype.toJSON = function toJSON() {
                        return this.constructor.toObject(this, $protobuf.util.toJSONOptions);
                    };

                    /**
                     * Gets the default type url for TxList
                     * @function getTypeUrl
                     * @memberof de.flashyotter.blockchain_node.grpc.TxList
                     * @static
                     * @param {string} [typeUrlPrefix] your custom typeUrlPrefix(default "type.googleapis.com")
                     * @returns {string} The default type url
                     */
                    TxList.getTypeUrl = function getTypeUrl(typeUrlPrefix) {
                        if (typeUrlPrefix === undefined) {
                            typeUrlPrefix = "type.googleapis.com";
                        }
                        return typeUrlPrefix + "/de.flashyotter.blockchain_node.grpc.TxList";
                    };

                    return TxList;
                })();

                grpc.Mining = (function() {

                    /**
                     * Constructs a new Mining service.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a Mining
                     * @extends $protobuf.rpc.Service
                     * @constructor
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     */
                    function Mining(rpcImpl, requestDelimited, responseDelimited) {
                        $protobuf.rpc.Service.call(this, rpcImpl, requestDelimited, responseDelimited);
                    }

                    (Mining.prototype = Object.create($protobuf.rpc.Service.prototype)).constructor = Mining;

                    /**
                     * Creates new Mining service using the specified rpc implementation.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Mining
                     * @static
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     * @returns {Mining} RPC service. Useful where requests and/or responses are streamed.
                     */
                    Mining.create = function create(rpcImpl, requestDelimited, responseDelimited) {
                        return new this(rpcImpl, requestDelimited, responseDelimited);
                    };

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Mining#mine}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Mining
                     * @typedef MineCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.Block} [response] Block
                     */

                    /**
                     * Calls Mine.
                     * @function mine
                     * @memberof de.flashyotter.blockchain_node.grpc.Mining
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Mining.MineCallback} callback Node-style callback called with the error, if any, and Block
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Mining.prototype.mine = function mine(request, callback) {
                        return this.rpcCall(mine, $root.de.flashyotter.blockchain_node.grpc.Empty, $root.de.flashyotter.blockchain_node.grpc.Block, request, callback);
                    }, "name", { value: "Mine" });

                    /**
                     * Calls Mine.
                     * @function mine
                     * @memberof de.flashyotter.blockchain_node.grpc.Mining
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.Block>} Promise
                     * @variation 2
                     */

                    return Mining;
                })();

                grpc.Wallet = (function() {

                    /**
                     * Constructs a new Wallet service.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a Wallet
                     * @extends $protobuf.rpc.Service
                     * @constructor
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     */
                    function Wallet(rpcImpl, requestDelimited, responseDelimited) {
                        $protobuf.rpc.Service.call(this, rpcImpl, requestDelimited, responseDelimited);
                    }

                    (Wallet.prototype = Object.create($protobuf.rpc.Service.prototype)).constructor = Wallet;

                    /**
                     * Creates new Wallet service using the specified rpc implementation.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @static
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     * @returns {Wallet} RPC service. Useful where requests and/or responses are streamed.
                     */
                    Wallet.create = function create(rpcImpl, requestDelimited, responseDelimited) {
                        return new this(rpcImpl, requestDelimited, responseDelimited);
                    };

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#send}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @typedef SendCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.Transaction} [response] Transaction
                     */

                    /**
                     * Calls Send.
                     * @function send
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest} request SendRequest message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Wallet.SendCallback} callback Node-style callback called with the error, if any, and Transaction
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Wallet.prototype.send = function send(request, callback) {
                        return this.rpcCall(send, $root.de.flashyotter.blockchain_node.grpc.SendRequest, $root.de.flashyotter.blockchain_node.grpc.Transaction, request, callback);
                    }, "name", { value: "Send" });

                    /**
                     * Calls Send.
                     * @function send
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.ISendRequest} request SendRequest message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.Transaction>} Promise
                     * @variation 2
                     */

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#info}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @typedef InfoCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.WalletInfo} [response] WalletInfo
                     */

                    /**
                     * Calls Info.
                     * @function info
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Wallet.InfoCallback} callback Node-style callback called with the error, if any, and WalletInfo
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Wallet.prototype.info = function info(request, callback) {
                        return this.rpcCall(info, $root.de.flashyotter.blockchain_node.grpc.Empty, $root.de.flashyotter.blockchain_node.grpc.WalletInfo, request, callback);
                    }, "name", { value: "Info" });

                    /**
                     * Calls Info.
                     * @function info
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.WalletInfo>} Promise
                     * @variation 2
                     */

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Wallet#history}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @typedef HistoryCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.TxList} [response] TxList
                     */

                    /**
                     * Calls History.
                     * @function history
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest} request HistoryRequest message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Wallet.HistoryCallback} callback Node-style callback called with the error, if any, and TxList
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Wallet.prototype.history = function history(request, callback) {
                        return this.rpcCall(history, $root.de.flashyotter.blockchain_node.grpc.HistoryRequest, $root.de.flashyotter.blockchain_node.grpc.TxList, request, callback);
                    }, "name", { value: "History" });

                    /**
                     * Calls History.
                     * @function history
                     * @memberof de.flashyotter.blockchain_node.grpc.Wallet
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IHistoryRequest} request HistoryRequest message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.TxList>} Promise
                     * @variation 2
                     */

                    return Wallet;
                })();

                grpc.Chain = (function() {

                    /**
                     * Constructs a new Chain service.
                     * @memberof de.flashyotter.blockchain_node.grpc
                     * @classdesc Represents a Chain
                     * @extends $protobuf.rpc.Service
                     * @constructor
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     */
                    function Chain(rpcImpl, requestDelimited, responseDelimited) {
                        $protobuf.rpc.Service.call(this, rpcImpl, requestDelimited, responseDelimited);
                    }

                    (Chain.prototype = Object.create($protobuf.rpc.Service.prototype)).constructor = Chain;

                    /**
                     * Creates new Chain service using the specified rpc implementation.
                     * @function create
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @static
                     * @param {$protobuf.RPCImpl} rpcImpl RPC implementation
                     * @param {boolean} [requestDelimited=false] Whether requests are length-delimited
                     * @param {boolean} [responseDelimited=false] Whether responses are length-delimited
                     * @returns {Chain} RPC service. Useful where requests and/or responses are streamed.
                     */
                    Chain.create = function create(rpcImpl, requestDelimited, responseDelimited) {
                        return new this(rpcImpl, requestDelimited, responseDelimited);
                    };

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Chain#latest}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @typedef LatestCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.Block} [response] Block
                     */

                    /**
                     * Calls Latest.
                     * @function latest
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Chain.LatestCallback} callback Node-style callback called with the error, if any, and Block
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Chain.prototype.latest = function latest(request, callback) {
                        return this.rpcCall(latest, $root.de.flashyotter.blockchain_node.grpc.Empty, $root.de.flashyotter.blockchain_node.grpc.Block, request, callback);
                    }, "name", { value: "Latest" });

                    /**
                     * Calls Latest.
                     * @function latest
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IEmpty} request Empty message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.Block>} Promise
                     * @variation 2
                     */

                    /**
                     * Callback as used by {@link de.flashyotter.blockchain_node.grpc.Chain#page}.
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @typedef PageCallback
                     * @type {function}
                     * @param {Error|null} error Error, if any
                     * @param {de.flashyotter.blockchain_node.grpc.BlockList} [response] BlockList
                     */

                    /**
                     * Calls Page.
                     * @function page
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest} request PageRequest message or plain object
                     * @param {de.flashyotter.blockchain_node.grpc.Chain.PageCallback} callback Node-style callback called with the error, if any, and BlockList
                     * @returns {undefined}
                     * @variation 1
                     */
                    Object.defineProperty(Chain.prototype.page = function page(request, callback) {
                        return this.rpcCall(page, $root.de.flashyotter.blockchain_node.grpc.PageRequest, $root.de.flashyotter.blockchain_node.grpc.BlockList, request, callback);
                    }, "name", { value: "Page" });

                    /**
                     * Calls Page.
                     * @function page
                     * @memberof de.flashyotter.blockchain_node.grpc.Chain
                     * @instance
                     * @param {de.flashyotter.blockchain_node.grpc.IPageRequest} request PageRequest message or plain object
                     * @returns {Promise<de.flashyotter.blockchain_node.grpc.BlockList>} Promise
                     * @variation 2
                     */

                    return Chain;
                })();

                return grpc;
            })();

            return blockchain_node;
        })();

        return flashyotter;
    })();

    return de;
})();

export { $root as default };
