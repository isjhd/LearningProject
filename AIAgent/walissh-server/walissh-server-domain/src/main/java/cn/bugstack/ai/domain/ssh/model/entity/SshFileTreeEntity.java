package cn.bugstack.ai.domain.ssh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件树实体（单层）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshFileTreeEntity {

    private String rootPath;
    private String homePath;
    private String currentPath;
    private String parentPath;
    private List<SshFileEntryEntity> items;

}
