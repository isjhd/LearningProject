package github.javaguide.remoting.transport.netty.codec;

import github.javaguide.remoting.constants.RpcConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @ClassName RpcMessageFrameDecoder
 * @Description RPCMessageFrame解析器 不能共享
 * @Author jiangyang1556
 * @Date 2025/9/28 9:36
 * @Version 1.0
 * 基于协议中的长度字段，将 TCP 无界的二进制流，切割成一个完整的 RPC 消息帧（ByteBuf）。
 * 只拆包，不做任何业务解码（魔数校验、序列化、解压都不做）
 **/
public class RpcMessageFrameDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageFrameDecoder() {
        // maxFrameLength 允许接收的最大报文长度，防止 OOM
        // lengthFieldOffset 长度字段偏移量：魔数 (4)+ 版本 (1) 后才是长度字段，所以偏移 5
        // lengthFieldLength 长度字段字节数：总长度占 4 字节
        // lengthAdjustment 长度调整值：已读取 4 魔数 + 1 版本 + 4 长度 = 9 字节，剩余字节 = fullLength-9
        // initialBytesToStrip 不剥离任何字节：保留完整报文，交给后续业务解码器处理。
        super(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }
}
