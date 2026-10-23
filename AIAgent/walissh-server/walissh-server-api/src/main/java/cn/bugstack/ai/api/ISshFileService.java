package cn.bugstack.ai.api;

import cn.bugstack.ai.api.dto.SshFileContentResponseDTO;
import cn.bugstack.ai.api.dto.SshFileTreeResponseDTO;
import cn.bugstack.ai.api.response.Response;

/**
 * SSH 文件浏览服务接口
 */
public interface ISshFileService {

    /**
     * 查询目录（仅当前层，按需懒加载）
     *
     * @param connectionId 连接ID
     * @param path         目录路径，空时默认用户目录
     */
    Response<SshFileTreeResponseDTO> tree(String connectionId, String path);

    /**
     * 读取文件内容（文本）
     *
     * @param connectionId 连接ID
     * @param path         文件路径
     */
    Response<SshFileContentResponseDTO> content(String connectionId, String path);

    /** 读取文件内容（支持分片 offset/limit） */
    Response<SshFileContentResponseDTO> contentChunk(String connectionId, String path, Long offset, Integer limit);

}
