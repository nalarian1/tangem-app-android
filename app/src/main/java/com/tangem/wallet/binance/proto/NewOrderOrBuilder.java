// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: dex.proto

package com.tangem.wallet.binance.proto;

public interface NewOrderOrBuilder extends
    // @@protoc_insertion_point(interface_extends:transaction.NewOrder)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   *    0xCE6DC043 // hardcoded, object type prefix in 4 bytes
   * </pre>
   *
   * <code>bytes sender = 1;</code>
   */
  com.google.protobuf.ByteString getSender();

  /**
   * <pre>
   * order id, optional
   * </pre>
   *
   * <code>string id = 2;</code>
   */
  java.lang.String getId();
  /**
   * <pre>
   * order id, optional
   * </pre>
   *
   * <code>string id = 2;</code>
   */
  com.google.protobuf.ByteString
      getIdBytes();

  /**
   * <pre>
   * symbol for trading pair in full name of the tokens
   * </pre>
   *
   * <code>string symbol = 3;</code>
   */
  java.lang.String getSymbol();
  /**
   * <pre>
   * symbol for trading pair in full name of the tokens
   * </pre>
   *
   * <code>string symbol = 3;</code>
   */
  com.google.protobuf.ByteString
      getSymbolBytes();

  /**
   * <pre>
   * only accept 2 for now, meaning limit order
   * </pre>
   *
   * <code>int64 ordertype = 4;</code>
   */
  long getOrdertype();

  /**
   * <pre>
   * 1 for buy and 2 fory sell
   * </pre>
   *
   * <code>int64 side = 5;</code>
   */
  long getSide();

  /**
   * <pre>
   * price of the order, which is the real price multiplied by 1e8 (10^8) and rounded to integer
   * </pre>
   *
   * <code>int64 price = 6;</code>
   */
  long getPrice();

  /**
   * <pre>
   * quantity of the order, which is the real price multiplied by 1e8 (10^8) and rounded to integer
   * </pre>
   *
   * <code>int64 quantity = 7;</code>
   */
  long getQuantity();

  /**
   * <pre>
   * 1 for Good Till Expire(GTE) order and 3 for Immediate Or Cancel (IOC)
   * </pre>
   *
   * <code>int64 timeinforce = 8;</code>
   */
  long getTimeinforce();
}
