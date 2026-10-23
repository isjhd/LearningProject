package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.model.valobj.intent.ConversationContextVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentResultVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentRuleVO;
import cn.bugstack.ai.domain.agent.model.valobj.intent.IntentTypeEnumVO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// 通过关键词匹配 + 正则匹配 + 上下文加权计算得分，选择最高分作为最终意图
// 无网络 IO、毫秒级响应、精准匹配高频运维指令，负责简单 / 标准意图；
@Component
public class RuleIntentClassifier implements IIntentClassifier {

    // 静态规则池：定义所有运维意图的匹配规则（关键词、正则、上下文加权）
    private static final List<IntentRuleVO> RULES = List.of(
        rule(IntentTypeEnumVO.DIAGNOSE,
            List.of("挂了", "宕机", "down", "502", "503", "504", "OOM", "满", "过高", "异常",
                    "报错", "告警", "超时", "timeout", "crash", "panic", "fatal"),
            List.of("为什么.*(?:挂|报错|失败|不通)", "排查.*问题", "分析.*原因"),
            Map.of(List.of("修复", "fix", "解决"), 0.1)),

        rule(IntentTypeEnumVO.CONFIGURE,
            List.of("配置", "config", "修改配置", "参数", "调整", "设置", "调优"),
            List.of("修改.*(?:conf|cfg|yml|properties|xml|json)", "设置.*参数"),
            Map.of()),

        rule(IntentTypeEnumVO.DEPLOY,
            List.of("部署", "deploy", "发布", "回滚", "rollback", "上线", "更新版本", "重启服务"),
            List.of("(?:发布|部署).*版本", "回滚.*版本"),
            Map.of()),

        rule(IntentTypeEnumVO.MONITOR,
            List.of("查看", "监控", "日志", "log", "cpu", "内存", "磁盘", "网络", "流量",
                    "负载", "load", "进程", "端口", "连接数"),
            List.of("(?:看|查|check).*(?:状态|情况|使用率)", "tail.*log"),
            Map.of()),

        rule(IntentTypeEnumVO.SECURITY,
            List.of("防火墙", "firewall", "iptables", "权限", "permission", "ssh", "密钥",
                    "证书", "ssl", "tls", "安全", "漏洞", "CVE"),
            List.of("(?:开放|关闭).*端口", "配置.*(?:ssl|证书|密钥)"),
            Map.of()),

        rule(IntentTypeEnumVO.BACKUP,
            List.of("备份", "backup", "恢复", "restore", "导出", "import", "迁移"),
            List.of("备份.*(?:数据库|文件|配置)", "恢复.*数据"),
            Map.of()),

        rule(IntentTypeEnumVO.EXPLAIN,
            List.of("什么意思", "怎么理解", "解释", "说明", "explain", "what is", "how to"),
            List.of("这个命令.*(?:意思|作用|用途)"),
            Map.of()),

        rule(IntentTypeEnumVO.SEARCH,
            List.of("找", "搜索", "grep", "find", "locate", "查找", "哪个进程", "哪个文件"),
            List.of("(?:找|搜索).*(?:文件|进程|端口)"),
            Map.of())
    );

    @Override
    public IntentResultVO classify(String message, ConversationContextVO context) {
        // 统一小写，忽略大小写
        String lowerMsg = message.toLowerCase();
        // 初始化默认结果：未知意图，置信度0
        IntentResultVO best = IntentResultVO.builder()
            .intent(IntentTypeEnumVO.UNKNOWN).confidence(0.0).entities(Map.of()).build();

        // 遍历所有规则，计算匹配得分
        for (IntentRuleVO rule : RULES) {
            double score = 0.0;

            // 关键词匹配（最高 0.6）
            long hits = rule.getKeywords().stream()
                .filter(lowerMsg::contains).count();
            score += Math.min(0.6, hits * 0.2);

            // 正则匹配（额外 +0.2）
            boolean patternHit = rule.getPatterns().stream()
                .anyMatch(p -> Pattern.matches(".*" + p + ".*", message));
            if (patternHit) score += 0.2;

            // 上下文加权：最近意图一致则 +0.1
            if (context != null && context.getRecentIntents() != null) {
                if (context.getRecentIntents().stream()
                    .anyMatch(h -> h.getIntent() == rule.getIntent())) {
                    score += 0.1;
                }
            }

            score = Math.min(1.0, score);

            // 取最高分规则作为结果
            if (score > best.getConfidence()) {
                best = IntentResultVO.builder()
                    .intent(rule.getIntent())
                    .confidence(score)
                    .entities(extractEntities(message, rule.getIntent()))
                    .build();
            }
        }
        return best;
    }

    // 预定义常见运维服务，从输入中提取服务名（nginx/redis/mysql等）
    private Map<String, String> extractEntities(String message, IntentTypeEnumVO intent) {
        Map<String, String> entities = new HashMap<>();
        // 提取服务名
        List<String> services = List.of("nginx", "redis", "mysql", "postgres", "docker",
            "kafka", "rabbitmq", "elasticsearch", "tomcat", "spring");
        services.stream().filter(message.toLowerCase()::contains)
            .forEach(svc -> entities.put("service", svc));
        return entities;
    }

    // 快速构建规则对象，简化代码
    private static IntentRuleVO rule(IntentTypeEnumVO intent, List<String> keywords,
                                   List<String> patterns, Map<List<String>, Double> contextBoost) {
        IntentRuleVO r = new IntentRuleVO();
        r.setIntent(intent);
        r.setKeywords(keywords);
        r.setPatterns(patterns);
        r.setContextBoost(contextBoost);
        return r;
    }
}
