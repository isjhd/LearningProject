package cn.bugstack.ai.domain.ssh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件节点实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshFileEntryEntity {

    private String name;
    private String path;
    private boolean directory;
    private Long size;
    private Long modifiedAt;

}
