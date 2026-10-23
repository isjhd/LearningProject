package cn.bugstack.ai.api;

import cn.bugstack.ai.api.dto.*;
import cn.bugstack.ai.api.response.Response;

/**
 * SSH终端服务远程接口
 *
 * @author waissh dev
 */
public interface ISshTerminalService {

    /**
     * 打开终端会话
     */
    Response<TerminalOpenResponseDTO> openTerminal(TerminalOpenRequestDTO requestDTO);

    /**
     * 执行命令（整行模式）
     */
    Response<TerminalExecResponseDTO> execCommand(TerminalExecRequestDTO requestDTO);

    /**
     * 写入原始输入到终端（逐字节模式，Shell 自身处理 echo）
     */
    Response<Void> writeToTerminal(TerminalWriteRequestDTO requestDTO);

    /**
     * 读取终端缓冲输出（轮询模式）
     */
    Response<TerminalReadResponseDTO> readFromTerminal(String sessionId);

    /**
     * 调整终端大小
     */
    Response<Void> resizeTerminal(TerminalResizeRequestDTO requestDTO);

    /**
     * 关闭终端会话
     */
    Response<Void> closeTerminal(String sessionId);

}
