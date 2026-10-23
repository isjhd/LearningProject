package cn.isjhd.domain.award.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: isjhd
 * @Date: 2025-12-09 13:02
 * @Description: 任务状态值对象
 */
@Getter
@AllArgsConstructor
public enum TaskStateVO {

    create("create", "创建"),
    complete("complete", "发送完成"),
    fail("fail", "发送失败"),
    ;

    private final String code;
    private final String desc;

}

