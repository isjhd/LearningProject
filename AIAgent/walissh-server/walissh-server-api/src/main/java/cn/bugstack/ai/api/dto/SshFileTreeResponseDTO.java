package cn.bugstack.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件树查询响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshFileTreeResponseDTO {

    /** 根路径，固定 "/" */
    private String rootPath;

    /** 用户家目录 */
    private String homePath;

    /** 当前目录 */
    private String currentPath;

    /** 父目录，根目录时为空 */
    private String parentPath;

    /** 当前目录下的子项 */
    private List<SshFileEntryDTO> items;

}
