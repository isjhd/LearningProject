package cn.bugstack.ai.domain.agent.model.valobj.intent;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum IntentTypeEnumVO {
    DIAGNOSE("诊断问题"),
    CONFIGURE("配置修改"),
    DEPLOY("部署操作"),
    MONITOR("监控查看"),
    SECURITY("安全相关"),
    BACKUP("备份恢复"),
    EXECUTE("直接执行"),
    EXPLAIN("解释说明"),
    SEARCH("搜索查找"),
    CHAT("闲聊"),
    CONTINUE("继续"),
    UNKNOWN("未知");

    private final String label;
}
