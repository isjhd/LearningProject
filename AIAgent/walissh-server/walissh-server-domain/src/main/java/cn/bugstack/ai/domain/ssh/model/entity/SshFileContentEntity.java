package cn.bugstack.ai.domain.ssh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件内容实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshFileContentEntity {

    private String path;
    private String name;
    private String charset;
    private Long size;
    private boolean binary;
    private boolean truncated;
    /** 分片读取时的起始偏移量 */
    private Long offset;
    private String content;

}
