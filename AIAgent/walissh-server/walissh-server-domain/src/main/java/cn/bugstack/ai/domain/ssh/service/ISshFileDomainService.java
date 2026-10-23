package cn.bugstack.ai.domain.ssh.service;

import cn.bugstack.ai.domain.ssh.model.entity.SshFileContentEntity;
import cn.bugstack.ai.domain.ssh.model.entity.SshFileTreeEntity;

/**
 * SSH 文件领域服务
 */
public interface ISshFileDomainService {

    SshFileTreeEntity tree(String connectionId, String path) throws Exception;

    SshFileContentEntity content(String connectionId, String path) throws Exception;

    /** 支持大文件分片读取 */
    SshFileContentEntity content(String connectionId, String path, Long offset, Integer limit) throws Exception;

    void createFile(String connectionId, String path, boolean useSudo) throws Exception;

    void createDirectory(String connectionId, String path, boolean useSudo) throws Exception;

    void rename(String connectionId, String oldPath, String newPath, boolean useSudo) throws Exception;

    void delete(String connectionId, String path, boolean useSudo) throws Exception;

    void saveFile(String connectionId, String path, String content, boolean useSudo) throws Exception;

    void uploadFile(String connectionId, String path, java.io.InputStream inputStream) throws Exception;

    void downloadFile(String connectionId, String path, java.io.OutputStream outputStream) throws Exception;

}
