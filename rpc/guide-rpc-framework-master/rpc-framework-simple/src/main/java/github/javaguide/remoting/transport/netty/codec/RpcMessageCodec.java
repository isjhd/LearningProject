package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.compress.Compress;
import github.javaguide.enums.CompressTypeEnum;
import github.javaguide.enums.SerializationTypeEnum;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.remoting.constants.RpcConstants;
import github.javaguide.remoting.dto.RpcMessage;
import github.javaguide.remoting.dto.RpcRequest;
import github.javaguide.remoting.dto.RpcResponse;
import github.javaguide.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static github.javaguide.remoting.constants.RpcConstants.HEARTBEAT_REQUEST_TYPE;
import static github.javaguide.remoting.constants.RpcConstants.HEARTBEAT_RESPONSE_TYPE;

/**
 * @ClassName RpcMessageCodec
 * @Description 可共享的 RPCMessage 编解码器
 * @Author jiangyang1556
 * @Date 2025/9/29 17:04
 * @Version 1.0
 * 完成 RPC消息对象 ↔ 网络二进制字节(ByteBuf) 的相互转换
 **/
@Slf4j
@ChannelHandler.Sharable// 关键：多Channel共享同一个编解码器（无状态才可以共享）
public class RpcMessageCodec extends MessageToMessageCodec<ByteBuf, RpcMessage> {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    /**
     * RPC Message -> ByteBuf
     * 编码
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, List<Object> list) throws Exception {
        /**
         * request header 16bytes
         * 分配堆外内存（Netty推荐）
         */
        ByteBuf out = ctx.alloc().buffer();
        // 4字节魔数
        out.writeBytes(RpcConstants.MAGIC_NUMBER);
        // 1字节版本
        out.writeByte(RpcConstants.VERSION);
        // 4字节总长度（占位）
        out.writeInt(0);
        // 1字节消息类型
        out.writeByte(rpcMessage.getMessageType());
        // 1字节序列化类型
        out.writeByte(rpcMessage.getCodec());
        // 1字节压缩类型
        out.writeByte(CompressTypeEnum.GZIP.getCode());
        // 4字节请求ID
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());
        /**
         * request body
         * 处理协议体（心跳消息无体）
         */
        byte[] bodyBytes = null;
        int fullLength = RpcConstants.HEAD_LENGTH;
        if(rpcMessage.getMessageType() != HEARTBEAT_REQUEST_TYPE && rpcMessage.getMessageType() != HEARTBEAT_RESPONSE_TYPE) {

            // 序列化
            String serialize = SerializationTypeEnum.getName(rpcMessage.getCodec());
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serialize);
            bodyBytes = serializer.serialize(rpcMessage.getData());

            // 压缩
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(CompressTypeEnum.getName(rpcMessage.getCompress()));
            log.debug("before compress request body size: [{}]", bodyBytes.length);
            bodyBytes = compress.compress(bodyBytes);
            log.debug("after compress request body size: [{}]", bodyBytes.length);
            // 加上请求体长度
            fullLength += bodyBytes.length;
        }
        // 写入数据
        if(bodyBytes != null) {
            out.writeBytes(bodyBytes);
        }

        // 写入包的长度字段
        // 获取下一次写入的位置
        int writeIndex = out.writerIndex();
        // 写指针指向 消息长度位置
        out.writerIndex(RpcConstants.MAGIC_NUMBER.length + 1);
        // 写入消息总长度
        out.writeInt(fullLength);
        // 让写指针回归原来状态
        out.writerIndex(writeIndex);

        // 加入结果列表
        // 为了解决双向编解码问题。
        // decode（入站）：输出 → RpcMessage
        // encode（出站）：输出 → ByteBuf
        list.add(out);
    }
    /**
     * ByteBuf -> RPC Message
     * 解码
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {

        // 1. 协议基础校验
        checkMagicNumber(in);
        checkVersion(in);

        // 2. 读取协议头
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();

        // 3. 封装基础RPC消息
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(messageType);
        rpcMessage.setCodec(codecType);
        rpcMessage.setCompress(compressType);
        rpcMessage.setRequestId(requestId);

        // 4. 心跳消息直接返回（无体）
        if (messageType == HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            list.add(rpcMessage);
            return;
        }
        if (messageType == HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            list.add(rpcMessage);
            return;
        }

        // 5. 处理RPC请求/响应体
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {

            // 读取字节数组
            byte[] bodyBytes = new byte[bodyLength];
            in.readBytes(bodyBytes);

            // 解压
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            log.debug("before decompress request body size: [{}]", bodyBytes.length);
            bodyBytes = compress.decompress(bodyBytes);
            log.debug("after decompress request body size: [{}]", bodyBytes.length);

            // 反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.debug("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);

            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            } else {
                RpcResponse rpcRequest = serializer.deserialize(bodyBytes, RpcResponse.class);
                rpcMessage.setData(rpcRequest);
            }
        }
        list.add(rpcMessage);
    }

    private void checkVersion(ByteBuf in) {
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("协议版本不匹配" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("未知魔数: " + Arrays.toString(tmp));
            }
        }
    }
}
