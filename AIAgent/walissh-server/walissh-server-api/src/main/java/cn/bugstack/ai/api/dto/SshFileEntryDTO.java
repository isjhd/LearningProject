package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件节点 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshFileEntryDTO {

    /** 节点名称（不含路径） */
    private String name;

    /** 绝对路径 */
    private String path;

    /** 是否目录 */
    private boolean directory;

    /** 文件大小（目录可能为空） */
    private Long size;

    /** 修改时间（毫秒时间戳） */
    private Long modifiedAt;

}
