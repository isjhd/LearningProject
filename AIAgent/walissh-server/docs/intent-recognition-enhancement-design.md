# WaLiSSH 意图识别增强方案 — 技术设计文档

> 基于 WaLiCode 上下文记忆与意图识别架构，适配 WaLiSSH 的 Spring Boot DDD + Google ADK 技术栈。

---

## 一、设计背景与目标

### 1.1 现状问题

WaLiSSH 当前存在以下能力缺口：

| 维度 | 现状 | 问题 |
|------|------|------|
| **System Prompt** | YAML 静态 `instruction` 字段 | 无法在运行时注入环境信息、历史上下文、用户意图等动态内容 |
| **消息历史管理** | ADK `InMemoryRunner` 内存管理，无裁剪 | 对话轮次增加后 context 持续增长，可能超出模型 context window |
| **意图识别** | 无 | Agent 无法提前感知用户意图，无法主动准备相关上下文 |
| **意图增强** | 无 | 用户输入中的服务名、文件路径、错误码等信号未被提取利用 |
| **会话持久化** | 仅内存，重启丢失 | 对话历史无法跨重启保留 |
| **上下文感知** | ThreadLocal 终端会话绑定 | 仅有终端绑定，缺乏终端状态、命令历史等环境感知 |

### 1.2 设计目标

1. **动态 Prompt 构建**：System Prompt 从"静态 YAML"升级为"运行时动态组装"
2. **上下文记忆管理**：实现 Provider-Reducer 管道，解决 context 超限问题
3. **意图识别系统**：两层分类器链（规则 + LLM），适配 SSH 运维场景
4. **意图增强**：信号提取 → 服务器上下文搜索 → Prompt 注入
5. **会话持久化**：MySQL 持久化 + Redis 缓存，服务重启不丢失

### 1.3 WaLiCode 设计范式参考

WaLiCode 的核心设计思想：

- **Provider-Reducer 管道模式**：上下文收集（Provider）与消息裁剪（Reducer）解耦
- **三层意图分类器链**：规则（快但粗）→ 模型（中等）→ LLM（慢但准），逐级升级
- **"信号提取 → 代码搜索 → LLM 决策"增强范式**：关键词只做信息提取，不做决策
- **里程碑系统**：独立于消息裁剪的关键事件记忆

---

## 二、总体架构

### 2.1 架构总览

```
用户输入 "nginx 502了，帮我排查"
    │
    ▼
[AgentServiceController.chatStream()]
    │
    ▼
[AIAgentReActServiceCase.chatStream()]
    │
    ├─ ① IIntentService.classify()                    意图识别
    │     ├─ RuleIntentClassifier  → DIAGNOSE (0.7)
    │     └─ LLMIntentClassifier   → DIAGNOSE (0.95)
    │
    ├─ ② IIntentEnhancerService.enhance()             意图增强
    │     ├─ SignalExtractor  → {services: ["nginx"], errors: ["502"]}
    │     └─ ContextSearch    → {nginx_status: "failed", logs: "..."}
    │
    ├─ ③ IChatContextService.buildContext()           上下文管理
    │     ├─ TerminalStateProvider  → {os: "Ubuntu 22.04", user: "root"}
    │     ├─ MilestoneProvider      → [{type: ERROR, content: "..."}]
    │     ├─ ToolResultProvider     → {summary: "已安装 nginx 1.24"}
    │     └─ HybridReducer          → 裁剪到 token 预算内
    │
    ├─ ④ IPromptService.buildEnrichedMessage()         动态 Prompt (领域服务)
    │     ├─ TerminalState采集 (ISshTerminalService)
    │     ├─ MilestoneTracker (记录与获取关键事件)
    │     └─ DynamicPromptBuilder (基础 instruction + 环境信息 + 意图分析 + 上下文)
    │
    └─ ⑤ AiCallNode → runner.runAsync()               ADK 调用
          │
          ▼
        LLM 返回 → 工具调用 → 结果 → promptService.detectAndRecordMilestone() → 流式输出
```

### 2.2 新增模块目录结构

```
walissh-server-domain/src/main/java/cn/bugstack/ai/domain/agent/
├── service/
│   ├── IChatContextService.java              上下文管理领域服务接口
│   ├── IIntentService.java                   意图识别领域服务接口
│   ├── IIntentEnhancerService.java           意图增强领域服务接口
│   ├── IPromptService.java                   提示词构建领域服务接口
│   ├── armory/                               智能体装配（google adk）
│   ├── context/                              上下文记忆服务实现包
│   │   ├── ChatContextService.java           领域服务实现
│   │   ├── provider/
│   │   │   ├── ContextProvider.java          Provider 接口
│   │   │   ├── TerminalStateProvider.java    终端状态（OS、用户、目录）
│   │   │   ├── TaskProvider.java             当前任务
│   │   │   ├── MilestoneProvider.java        里程碑事件
│   │   │   └── ToolResultProvider.java       工具结果摘要
│   │   └── reducer/
│   │       ├── MessageReducer.java           Reducer 接口
│   │       ├── PriorityReducer.java          优先级裁剪
│   │       ├── SlidingWindowReducer.java     滑动窗口裁剪
│   │       └── HybridReducer.java            混合裁剪（默认）
│   ├── intent/                               意图识别服务实现包
│   │   ├── IntentService.java                领域服务实现
│   │   ├── ContextTracker.java               对话上下文追踪器 (内部组件)
│   │   └── classifier/
│   │       ├── IntentClassifier.java         分类器接口
│   │       ├── RuleIntentClassifier.java     第1层：规则分类
│   │       └── LLMIntentClassifier.java      第2层：LLM 分类
│   ├── enhance/                              意图增强服务实现包
│   │   ├── IntentEnhancerService.java        领域服务实现
│   │   └── processor/
│   │       ├── SignalExtractor.java          信号提取 (内部组件)
│   │       └── ContextSearch.java            服务器上下文搜索 (内部组件)
│   ├── prompt/                               提示词构建服务实现包
│   │   ├── PromptService.java                领域服务实现
│   │   └── dynamic/
│   │       ├── DynamicPromptBuilder.java     动态 Prompt 组装器 (内部组件)
│   │       └── MilestoneTracker.java         里程碑追踪器 (内部组件)
├── model/
│   ├── valobj/
│   │   ├── prompt/
│   │   │   ├── PromptContextVO.java          Prompt 上下文值对象
│   │   │   └── MilestoneVO.java              里程碑
│   │   ├── IntentResult.java                 意图识别结果
│   │   ├── ExtractedSignals.java             提取的信号
│   │   ├── SearchContext.java                搜索上下文
│   │   └── ConversationContext.java          对话上下文
│   └── entity/
│       └── ChatMessageEntity.java            对话消息实体
└── adaper/
    └── IChatHistoryRepository.java              对话历史持久化网关接口
```

---

## 三、Phase 1：动态 Prompt 构建

### 3.1 设计说明

当前 System Prompt 完全来自 YAML 的 `instruction` 静态文本，无法在运行时注入环境信息、历史上下文等动态内容。本阶段引入 `IPromptService` 领域服务，在调用 LLM 前动态组装完整 Prompt。为符合 DDD 规范，Case 层（`AiCallNode`）仅依赖 `IPromptService` 接口，而不直接依赖 `DynamicPromptBuilder`、`MilestoneTracker` 等内部组件。

### 3.2 领域模型 (Value Objects)

```java
package cn.bugstack.ai.domain.agent.model.valobj.prompt;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PromptContextVO {
    private String serverInfo;
    private String osInfo;
    private String currentUser;
    private String currentDirectory;

    private List<String> recentCommands;
    private List<MilestoneVO> milestoneVOS;

    // 后续 Phase 扩展
    // private IntentResult intentResult;
    // private Map<String, String> serviceStatus;
}
```

### 3.3 领域服务 (Domain Service)

#### IPromptService 接口

暴露给 Case 层的统一门面：

```java
package cn.bugstack.ai.domain.agent.service;

public interface IPromptService {
    void detectAndRecordMilestone(String sessionId, String role, String content);
    String buildEnrichedMessage(String userMessage, String sessionId, String terminalSessionId, List<String> recentCommands);
    void clearMilestones(String sessionId);
}
```

#### PromptService 实现

组合内部组件：

```java
package cn.bugstack.ai.domain.agent.service.prompt;

@Service
public class PromptService implements IPromptService {
    @Resource private DynamicPromptBuilder dynamicPromptBuilder;
    @Resource private MilestoneTracker milestoneTracker;
    @Resource private ISshTerminalService sshTerminalService;

    @Override
    public String buildEnrichedMessage(String userMessage, String sessionId, String terminalSessionId, List<String> recentCommands) {
        // 1. 从 SSH 终端采集环境信息
        // 2. 从 milestoneTracker 获取事件
        // 3. 构建 PromptContextVO
        // 4. 调用 dynamicPromptBuilder.buildMessagePrefix() 生成前缀
        // 5. 拼接返回
    }
    
    // ... 其他方法委托给 tracker
}
```

### 3.4 动态组装器 (DynamicPromptBuilder)

```java
package cn.bugstack.ai.domain.agent.service.prompt.dynamic;

@Component
public class DynamicPromptBuilder {
    /**
     * 将动态上下文构建为用户消息前缀（注入到用户消息中）
     * 适用于 ADK 无法直接在运行时修改 system instruction 的场景
     */
    public String buildMessagePrefix(PromptContextVO ctx) {
        if (ctx == null) return "";
        StringBuilder sb = new StringBuilder();
        
        // 拼接 [系统环境]
        // 拼接 [最近执行的命令]
        // 拼接 [关键事件] (Milestones)
        
        return sb.toString();
    }
}
```

### 3.5 改造 AiCallNode (Case 层)

在 `AiCallNode` 中，仅注入 `IPromptService`：

```java
// AiCallNode.java 改造点
@Resource
private IPromptService promptService;

private String buildEnrichedMessage(String userMessage, DynamicContext dynamicContext) {
    // 记录用户消息的里程碑
    promptService.detectAndRecordMilestone(dynamicContext.getSessionId(), "user", userMessage);

    // 委托领域服务构建富化消息
    return promptService.buildEnrichedMessage(
            userMessage,
            dynamicContext.getSessionId(),
            dynamicContext.getTerminalSessionId(),
            dynamicContext.getRecentCommands()
    );
}

// 在工具执行结果回调处：
promptService.detectAndRecordMilestone(dynamicContext.getSessionId(), "tool", resultContent);
```

---

## 四、Phase 2：上下文记忆管理

### 4.1 设计说明

采用 WaLiCode 的 **Provider-Reducer 管道模式**，将上下文收集与消息裁剪解耦。Provider 负责收集各维度上下文，Reducer 负责在 token 预算内裁剪消息。

### 4.2 ContextProvider 接口

```java
package cn.bugstack.ai.domain.agent.service.context.provider;

import java.util.Map;

public interface ContextProvider {
    String getName();
    int getOrder();
    boolean enabled();
    Map<String, Object> provide(String sessionId, String userId);
}
```

### 4.3 四个 Provider 实现

#### TerminalStateProvider（order=10）

提供当前终端的系统环境信息：

```java
@Component
public class TerminalStateProvider implements ContextProvider {
    @Resource
    private ISshTerminalService sshTerminalService;

    @Override public String getName() { return "terminal-state"; }
    @Override public int getOrder() { return 10; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId) {
        Map<String, Object> result = new HashMap<>();
        String osInfo = safeExec(sessionId, "uname -srm");
        String user   = safeExec(sessionId, "whoami");
        String pwd    = safeExec(sessionId, "pwd");
        String uptime = safeExec(sessionId, "uptime -p 2>/dev/null || uptime");

        result.put("osInfo", osInfo);
        result.put("currentUser", user);
        result.put("currentDirectory", pwd);
        result.put("uptime", uptime);
        return result;
    }

    private String safeExec(String sessionId, String cmd) {
        try { return sshTerminalService.executeCommand(sessionId, cmd).trim(); }
        catch (Exception e) { return ""; }
    }
}
```

#### TaskProvider（order=20）

提取当前对话中的任务描述：

```java
@Component
public class TaskProvider implements ContextProvider {
    @Override public String getName() { return "task"; }
    @Override public int getOrder() { return 20; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId) {
        Map<String, Object> result = new HashMap<>();
        // 从 DynamicContext.messageHistory 中提取第一条用户消息作为任务描述
        List<Map<String, Object>> history = messageHistoryCache.get(sessionId);
        if (history != null) {
            history.stream()
                .filter(m -> "user".equals(m.get("role")))
                .findFirst()
                .ifPresent(m -> result.put("taskDescription", m.get("content")));
        }
        return result;
    }
}
```

#### MilestoneProvider（order=30）

提供不受裁剪影响的关键事件：

```java
@Component
public class MilestoneProvider implements ContextProvider {
    @Resource
    private MilestoneTracker milestoneTracker;

    @Override public String getName() { return "milestoneVO"; }
    @Override public int getOrder() { return 30; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId) {
        Map<String, Object> result = new HashMap<>();
        List<MilestoneVO> milestoneVOS = milestoneTracker.getRecent(sessionId, 10);
        result.put("milestoneVOS", milestoneVOS);
        return result;
    }
}
```

#### ToolResultProvider（order=40）

工具执行结果的懒摘要策略：

```java
@Component
public class ToolResultProvider implements ContextProvider {
    private final Map<String, List<ToolResultEntry>> results = new ConcurrentHashMap<>();
    private final Map<String, String> summaryCache = new ConcurrentHashMap<>();

    @Override public String getName() { return "tool-result"; }
    @Override public int getOrder() { return 40; }
    @Override public boolean enabled() { return true; }

    @Override
    public Map<String, Object> provide(String sessionId, String userId) {
        Map<String, Object> result = new HashMap<>();
        List<ToolResultEntry> entries = results.getOrDefault(sessionId, Collections.emptyList());
        if (entries.isEmpty()) return result;

        // 懒摘要：有缓存直接返回，否则重新生成
        String summary = summaryCache.computeIfAbsent(sessionId, id -> generateSummary(entries));
        result.put("toolResultSummary", summary);
        return result;
    }

    public void pushResult(String sessionId, ToolResultEntry entry) {
        results.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(entry);
        summaryCache.remove(sessionId);  // 失效摘要缓存
    }

    private String generateSummary(List<ToolResultEntry> entries) {
        // 少量结果直接拼接，大量结果模板化压缩
        if (entries.size() <= 5) {
            return entries.stream()
                .map(e -> e.getToolName() + ": " + truncate(e.getResult(), 100))
                .collect(Collectors.joining("\n"));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("最近执行了 ").append(entries.size()).append(" 个工具调用:\n");
        // 只取最近 5 条详细 + 总结
        List<ToolResultEntry> recent = entries.subList(entries.size() - 5, entries.size());
        for (ToolResultEntry e : recent) {
            sb.append("- ").append(e.getToolName()).append(": ")
              .append(truncate(e.getResult(), 80)).append("\n");
        }
        return sb.toString();
    }
}
```

### 4.4 MessageReducer 裁剪策略

#### 接口定义

```java
package cn.bugstack.ai.domain.agent.service.context.reducer;

import java.util.List;
import java.util.Map;

public interface MessageReducer {
    List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget);
}
```

#### PriorityReducer — 优先级裁剪

```java
@Component
public class PriorityReducer implements MessageReducer {

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        // 为每条消息推断优先级
        List<PrioritizedMessage> prioritized = messages.stream()
            .map(m -> new PrioritizedMessage(m, inferPriority(m)))
            .collect(Collectors.toList());

        // 至少保留最近 2 条
        int minKeep = Math.min(2, prioritized.size());
        List<PrioritizedMessage> kept = new ArrayList<>(prioritized.subList(
            prioritized.size() - minKeep, prioritized.size()));

        // 从低优先级开始丢弃，直到满足 token 预算
        int usedTokens = estimateTokens(kept);
        for (int i = prioritized.size() - minKeep - 1; i >= 0; i--) {
            PrioritizedMessage pm = prioritized.get(i);
            int msgTokens = estimateToken(pm.getMessage());
            if (usedTokens + msgTokens <= tokenBudget) {
                kept.add(0, pm);
                usedTokens += msgTokens;
            }
        }

        return kept.stream().map(PrioritizedMessage::getMessage).collect(Collectors.toList());
    }

    private Priority inferPriority(Map<String, Object> message) {
        String role = (String) message.get("role");
        String content = String.valueOf(message.get("content"));

        if ("tool".equals(role) && containsAny(content, "error", "failed", "exception", "permission denied")) {
            return Priority.CRITICAL;
        }
        if ("user".equals(role) && containsAny(content, "/", ".conf", ".yml", ".properties")) {
            return Priority.HIGH;
        }
        if ("system".equals(role)) {
            return Priority.HIGH;
        }
        if ("assistant".equals(role) && content.length() > 5000) {
            return Priority.LOW;
        }
        return Priority.MEDIUM;
    }

    enum Priority { CRITICAL, HIGH, MEDIUM, LOW }
}
```

#### SlidingWindowReducer — 滑动窗口裁剪

```java
@Component
public class SlidingWindowReducer implements MessageReducer {
    private static final int DEFAULT_WINDOW_SIZE = 20;

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        List<Map<String, Object>> window = new ArrayList<>();
        int usedTokens = 0;

        // 从新到旧逐条添加，直到超出 token 预算或窗口大小
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = messages.get(i);
            int msgTokens = estimateToken(msg);
            if (window.size() >= DEFAULT_WINDOW_SIZE || usedTokens + msgTokens > tokenBudget) break;
            window.add(0, msg);
            usedTokens += msgTokens;
        }
        return window;
    }
}
```

#### HybridReducer — 混合裁剪（默认策略）

```java
@Component
public class HybridReducer implements MessageReducer {
    @Resource private PriorityReducer priorityReducer;
    @Resource private SlidingWindowReducer slidingReducer;

    @Override
    public List<Map<String, Object>> reduce(List<Map<String, Object>> messages, int tokenBudget) {
        Set<Integer> priorityKeep = indexSet(priorityReducer.reduce(messages, tokenBudget), messages);
        Set<Integer> slidingKeep  = indexSet(slidingReducer.reduce(messages, tokenBudget), messages);

        // 取交集
        Set<Integer> keepIndices = new HashSet<>(priorityKeep);
        keepIndices.retainAll(slidingKeep);

        // 保证至少有最近 2 条
        int minKeep = Math.min(2, messages.size());
        for (int i = messages.size() - minKeep; i < messages.size(); i++) {
            keepIndices.add(i);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            if (keepIndices.contains(i)) result.add(messages.get(i));
        }
        return result;
    }

    private Set<Integer> indexSet(List<Map<String, Object>> subset, List<Map<String, Object>> all) {
        Set<Integer> indices = new HashSet<>();
        for (Map<String, Object> msg : subset) {
            int idx = all.indexOf(msg);
            if (idx >= 0) indices.add(idx);
        }
        return indices;
    }
}
```

### 4.5 MilestoneTracker — 里程碑系统

```java
@Component
public class MilestoneTracker {
    private static final int MAX_MILESTONES = 50;
    private final Map<String, LinkedList<MilestoneVO>> milestoneVOS = new ConcurrentHashMap<>();

    public void detectAndRecord(String sessionId, String role, String content) {
        MilestoneVO.Type type = null;

        if ("user".equals(role)) {
            if (matches(content, "不对|不是这样|改一下|换个思路|换种方式|错了")) {
                type = MilestoneVO.Type.TASK_CHANGE;
            } else if (matches(content, "完成了|搞定|结束|好了")) {
                type = MilestoneVO.Type.TASK_COMPLETE;
            } else if (matches(content, "不要|停|别")) {
                type = MilestoneVO.Type.USER_CORRECTION;
            }
        }

        if ("tool".equals(role)) {
            if (matches(content, "(?i)error|failed|exception|permission denied|not found|refused")) {
                type = MilestoneVO.Type.ERROR;
            }
        }

        if (type != null) {
            push(sessionId, MilestoneVO.builder()
                .type(type)
                .content(truncate(content, 200))
                .timestamp(System.currentTimeMillis())
                .build());
        }
    }

    private void push(String sessionId, MilestoneVO milestoneVO) {
        LinkedList<MilestoneVO> list = milestoneVOS.computeIfAbsent(
            sessionId, k -> new LinkedList<>());
        synchronized (list) {
            list.addLast(milestoneVO);
            while (list.size() > MAX_MILESTONES) list.removeFirst();
        }
    }

    public List<MilestoneVO> getRecent(String sessionId, int limit) {
        LinkedList<MilestoneVO> list = milestoneVOS.getOrDefault(sessionId, new LinkedList<>());
        synchronized (list) {
            int from = Math.max(0, list.size() - limit);
            return new ArrayList<>(list.subList(from, list.size()));
        }
    }

    public void clear(String sessionId) {
        milestoneVOS.remove(sessionId);
    }

    private boolean matches(String content, String regex) {
        return content != null && content.matches(".*(" + regex + ").*");
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
```

### 4.6 IChatContextService 与 ChatContextService — 上下文管理领域服务

为符合 DDD 规范，提取统一的接口 `IChatContextService`，并在 `ChatContextService` 中编排 Provider 和 Reducer：

```java
package cn.bugstack.ai.domain.agent.service;

public interface IChatContextService {
    PromptContextVO buildPromptContext(String sessionId, String userId, String terminalSessionId);
    List<Map<String, Object>> trimHistory(List<Map<String, Object>> history, int tokenBudget);
}
```

```java
package cn.bugstack.ai.domain.agent.service.context;

import cn.bugstack.ai.domain.agent.service.IChatContextService;
import cn.bugstack.ai.domain.agent.service.IPromptService;
@Service
public class ChatContextService implements IChatContextService {
    private static final int DEFAULT_MAX_CONTEXT_TOKENS = 8000;

    @Resource
    private List<ContextProvider> providers;
    @Resource
    private HybridReducer hybridReducer;
    @Resource
    private IPromptService promptService;
    @Resource
    private ISshTerminalService sshTerminalService;

    @PostConstruct
    public void init() {
        providers.sort(Comparator.comparingInt(ContextProvider::getOrder));
    }

    @Override
    public PromptContextVO buildPromptContext(String sessionId, String userId, String terminalSessionId) {
        // 由于 Lombok Builder 的特性，我们直接通过链式调用或者临时变量存储构建
        Map<String, Object> finalCtx = new HashMap<>();

        for (ContextProvider provider : providers) {
            if (!provider.enabled()) continue;
            Map<String, Object> ctx = provider.provide(sessionId, userId);
            finalCtx.putAll(ctx);
        }

        return PromptContextVO.builder()
                .osInfo((String) finalCtx.get("osInfo"))
                .currentUser((String) finalCtx.get("currentUser"))
                .currentDirectory((String) finalCtx.get("currentDirectory"))
                .serverInfo((String) finalCtx.get("serverInfo"))
                .milestoneVOS((List<MilestoneVO>) finalCtx.get("milestoneVOS"))
                // 后续 Phase 扩展
                // .serviceStatus((Map<String, String>) finalCtx.get("serviceStatus"))
                // .fileContents((Map<String, String>) finalCtx.get("fileContents"))
                // .recentLogs((Map<String, String>) finalCtx.get("recentLogs"))
                .build();
    }

    @Override
    public List<Map<String, Object>> trimHistory(List<Map<String, Object>> history, int tokenBudget) {
        if (history == null || history.isEmpty()) return Collections.emptyList();
        return hybridReducer.reduce(history, tokenBudget > 0 ? tokenBudget : DEFAULT_MAX_CONTEXT_TOKENS);
    }
}
```

---

## 五、Phase 3：意图识别系统

### 5.1 设计说明

采用 **两层分类器链**（规则 + LLM），适配后端服务场景（省去 WaLiCode 前端的"模型分类器"层，直接规则 + LLM 两层，降低延迟）。

### 5.2 意图类型定义

```java
public enum IntentType {
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
    IntentType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
```

### 5.3 IntentResult 值对象

```java
@Data
@Builder
public class IntentResult {
    private IntentType intent;
    private double confidence;
    private Map<String, String> entities;
    private String rawResponse;
}
```

### 5.4 IIntentService 与 IntentService — 意图分类领域服务

提供统一的意图分类领域服务接口，隐藏内部的分类器编排：

```java
package cn.bugstack.ai.domain.agent.service;

public interface IIntentService {
    IntentResult classify(String sessionId, String userId, String message);
}
```

```java
package cn.bugstack.ai.domain.agent.service.intent;

import cn.bugstack.ai.domain.agent.service.IIntentService;

@Service
public class IntentService implements IIntentService {
    @Resource private RuleIntentClassifier ruleClassifier;
    @Resource private LLMIntentClassifier llmClassifier;
    @Resource private ContextTracker contextTracker;

    private final Cache<String, IntentResult> cache = Caffeine.newBuilder()
        .maximumSize(200)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    @Override
    public IntentResult classify(String sessionId, String userId, String message) {
        String cacheKey = sessionId + ":" + hashMessage(message);
        IntentResult cached = cache.getIfPresent(cacheKey);
        if (cached != null) return cached;

        ConversationContext context = contextTracker.getContext(sessionId);

        // 第1层：规则分类（< 1ms）
        IntentResult ruleResult = ruleClassifier.classify(message, context);
        if (ruleResult.getConfidence() >= 0.8) {
            recordAndCache(sessionId, cacheKey, ruleResult);
            return ruleResult;
        }

        // 第2层：LLM 分类（100-500ms）
        IntentResult llmResult = llmClassifier.classify(message, context);
        IntentResult finalResult = llmResult.getConfidence() >= 0.5 ? llmResult : ruleResult;

        recordAndCache(sessionId, cacheKey, finalResult);
        return finalResult;
    }

    private void recordAndCache(String sessionId, String cacheKey, IntentResult result) {
        contextTracker.updateContext(sessionId, result);
        cache.put(cacheKey, result);
    }

    private String hashMessage(String message) {
        return Integer.toHexString(message.hashCode());
    }
}
```

### 5.5 RuleIntentClassifier — 规则分类器

```java
@Component
public class RuleIntentClassifier implements IntentClassifier {

    private static final List<IntentRule> RULES = List.of(
        rule(IntentType.DIAGNOSE,
            List.of("挂了", "宕机", "down", "502", "503", "504", "OOM", "满", "过高", "异常",
                    "报错", "告警", "超时", "timeout", "crash", "panic", "fatal"),
            List.of("为什么.*(?:挂|报错|失败|不通)", "排查.*问题", "分析.*原因"),
            Map.of(List.of("修复", "fix", "解决"), 0.1)),

        rule(IntentType.CONFIGURE,
            List.of("配置", "config", "修改配置", "参数", "调整", "设置", "调优"),
            List.of("修改.*(?:conf|cfg|yml|properties|xml|json)", "设置.*参数"),
            Map.of()),

        rule(IntentType.DEPLOY,
            List.of("部署", "deploy", "发布", "回滚", "rollback", "上线", "更新版本", "重启服务"),
            List.of("(?:发布|部署).*版本", "回滚.*版本"),
            Map.of()),

        rule(IntentType.MONITOR,
            List.of("查看", "监控", "日志", "log", "cpu", "内存", "磁盘", "网络", "流量",
                    "负载", "load", "进程", "端口", "连接数"),
            List.of("(?:看|查|check).*(?:状态|情况|使用率)", "tail.*log"),
            Map.of()),

        rule(IntentType.SECURITY,
            List.of("防火墙", "firewall", "iptables", "权限", "permission", "ssh", "密钥",
                    "证书", "ssl", "tls", "安全", "漏洞", "CVE"),
            List.of("(?:开放|关闭).*端口", "配置.*(?:ssl|证书|密钥)"),
            Map.of()),

        rule(IntentType.BACKUP,
            List.of("备份", "backup", "恢复", "restore", "导出", "import", "迁移"),
            List.of("备份.*(?:数据库|文件|配置)", "恢复.*数据"),
            Map.of()),

        rule(IntentType.EXPLAIN,
            List.of("什么意思", "怎么理解", "解释", "说明", "explain", "what is", "how to"),
            List.of("这个命令.*(?:意思|作用|用途)"),
            Map.of()),

        rule(IntentType.SEARCH,
            List.of("找", "搜索", "grep", "find", "locate", "查找", "哪个进程", "哪个文件"),
            List.of("(?:找|搜索).*(?:文件|进程|端口)"),
            Map.of())
    );

    @Override
    public IntentResult classify(String message, ConversationContext context) {
        String lowerMsg = message.toLowerCase();
        IntentResult best = IntentResult.builder()
            .intent(IntentType.UNKNOWN).confidence(0.0).entities(Map.of()).build();

        for (IntentRule rule : RULES) {
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
            if (context.getRecentIntents().stream()
                .anyMatch(h -> h.getIntent() == rule.getIntent())) {
                score += 0.1;
            }

            score = Math.min(1.0, score);

            if (score > best.getConfidence()) {
                best = IntentResult.builder()
                    .intent(rule.getIntent())
                    .confidence(score)
                    .entities(extractEntities(message, rule.getIntent()))
                    .build();
            }
        }
        return best;
    }

    private Map<String, String> extractEntities(String message, IntentType intent) {
        Map<String, String> entities = new HashMap<>();
        // 提取服务名
        List<String> services = List.of("nginx", "redis", "mysql", "postgres", "docker",
            "kafka", "rabbitmq", "elasticsearch", "tomcat", "spring");
        services.stream().filter(message.toLowerCase()::contains)
            .forEach(svc -> entities.put("service", svc));
        return entities;
    }

    private static IntentRule rule(IntentType intent, List<String> keywords,
                                   List<String> patterns, Map<List<String>, Double> contextBoost) {
        IntentRule r = new IntentRule();
        r.setIntent(intent);
        r.setKeywords(keywords);
        r.setPatterns(patterns);
        r.setContextBoost(contextBoost);
        return r;
    }
}
```

### 5.6 LLMIntentClassifier — LLM 分类器

```java
@Component
public class LLMIntentClassifier implements IntentClassifier {
    @Resource
    private ChatModel chatModel;

    private static final String CLASSIFY_PROMPT_TEMPLATE = """
        你是一个 SSH 运维场景的意图识别系统。分析用户输入，返回 JSON 格式的意图分类结果。
        
        ## 意图类型
        - DIAGNOSE: 诊断问题（服务挂了、报错、异常排查）
        - CONFIGURE: 配置修改（改配置文件、调参数）
        - DEPLOY: 部署操作（部署、发布、回滚）
        - MONITOR: 监控查看（看日志、查状态、看资源使用）
        - SECURITY: 安全相关（防火墙、权限、证书）
        - BACKUP: 备份恢复（备份数据、恢复数据）
        - EXECUTE: 直接执行（帮我跑某命令）
        - EXPLAIN: 解释说明（这个命令什么意思）
        - SEARCH: 搜索查找（找文件、查进程）
        - CHAT: 闲聊
        - CONTINUE: 继续上一个任务
        - UNKNOWN: 无法判断
        
        ## 输出格式（仅返回 JSON，无其他内容）
        {"intent":"类型","confidence":0.0-1.0,"entities":{"key":"value"}}
        
        ## 示例
        输入: "nginx 502了，帮我看看"
        输出: {"intent":"DIAGNOSE","confidence":0.95,"entities":{"service":"nginx","error":"502"}}
        
        输入: "帮我改下 redis 的 maxmemory 配置"
        输出: {"intent":"CONFIGURE","confidence":0.9,"entities":{"service":"redis","config":"maxmemory"}}
        
        输入: "看下服务器磁盘使用情况"
        输出: {"intent":"MONITOR","confidence":0.9,"entities":{"resource":"disk"}}
        
        输入: "这个命令 awk '{print $1}' access.log 是什么意思"
        输出: {"intent":"EXPLAIN","confidence":0.95,"entities":{"command":"awk"}}
        
        ## 对话上下文
        最近意图: %s
        
        ## 分析以下输入
        输入: "%s"
        输出:
        """;

    @Override
    public IntentResult classify(String message, ConversationContext context) {
        String recentIntents = context.getRecentIntents().stream()
            .map(h -> h.getIntent().name())
            .collect(Collectors.joining(", "));

        String prompt = String.format(CLASSIFY_PROMPT_TEMPLATE,
            recentIntents.isEmpty() ? "无" : recentIntents, message);

        try {
            String response = chatModel.call(prompt).getResult().getOutput().getContent();
            return parseResponse(response);
        } catch (Exception e) {
            return IntentResult.builder()
                .intent(IntentType.UNKNOWN).confidence(0.0).entities(Map.of()).build();
        }
    }

    private IntentResult parseResponse(String response) {
        try {
            // 提取 JSON 部分
            String json = response.replaceAll("(?s).*?(\\{.*}).*", "$1");
            Map<String, Object> parsed = new ObjectMapper().readValue(json, Map.class);

            IntentType intent = IntentType.valueOf(
                String.valueOf(parsed.get("intent")).toUpperCase());
            double confidence = parsed.containsKey("confidence")
                ? Double.parseDouble(String.valueOf(parsed.get("confidence"))) : 0.5;
            Map<String, String> entities = parsed.containsKey("entities")
                ? (Map<String, String>) parsed.get("entities") : Map.of();

            return IntentResult.builder()
                .intent(intent).confidence(confidence)
                .entities(entities).rawResponse(response).build();
        } catch (Exception e) {
            return IntentResult.builder()
                .intent(IntentType.UNKNOWN).confidence(0.0)
                .entities(Map.of()).rawResponse(response).build();
        }
    }
}
```

### 5.7 ContextTracker — 对话上下文追踪器

```java
@Component
public class ContextTracker {
    private static final int WINDOW_SIZE = 10;
    private final Map<String, ConversationContext> contexts = new ConcurrentHashMap<>();

    public ConversationContext getContext(String sessionId) {
        return contexts.computeIfAbsent(sessionId, id -> ConversationContext.builder()
            .recentIntents(new LinkedList<>())
            .turnCount(0)
            .sessionStartTime(System.currentTimeMillis())
            .build());
    }

    public void updateContext(String sessionId, IntentResult result) {
        ConversationContext ctx = getContext(sessionId);
        ctx.getRecentIntents().addLast(IntentHistoryEntry.builder()
            .intent(result.getIntent())
            .confidence(result.getConfidence())
            .timestamp(System.currentTimeMillis())
            .build());
        if (ctx.getRecentIntents().size() > WINDOW_SIZE) {
            ctx.getRecentIntents().removeFirst();
        }
        ctx.setTurnCount(ctx.getTurnCount() + 1);
        ctx.setLastIntent(result.getIntent());
    }

    public void clear(String sessionId) {
        contexts.remove(sessionId);
    }
}
```

---

## 六、Phase 4：意图增强 — 信号提取与上下文注入

### 6.1 设计说明

从用户输入中提取结构化信号（服务名、文件路径、错误码等），到目标服务器上搜索相关上下文（服务状态、配置文件、日志），注入到 Prompt 中辅助 LLM 决策。

核心思想：**关键词只做信息提取，不做决策** — SignalExtractor 负责提取信号，ContextSearch 负责查找上下文，LLM 统一做理解决策。

### 6.2 ExtractedSignals 值对象

```java
@Data
@Builder
public class ExtractedSignals {
    private List<String> serverHosts;
    private List<String> filePaths;
    private List<String> serviceNames;
    private List<String> commandHints;
    private List<String> errorPatterns;
    private List<String> logKeywords;
}
```

### 6.3 SignalExtractor — 信号提取器

```java
@Component
public class SignalExtractor {

    private static final List<String> KNOWN_SERVICES = List.of(
        "nginx", "apache", "httpd", "redis", "mysql", "mariadb", "postgres", "postgresql",
        "mongodb", "kafka", "rabbitmq", "docker", "containerd", "kubernetes", "kubelet",
        "jenkins", "gitlab", "elasticsearch", "kibana", "logstash", "prometheus", "grafana",
        "zookeeper", "etcd", "consul", "nacos", "tomcat", "spring", "node", "php-fpm",
        "sshd", "firewalld", "iptables", "crond", "rsyslogd"
    );

    private static final Pattern FILE_PATH_PATTERN = Pattern.compile(
        "(?:/[\\w.-]+)+\\.(?:conf|cfg|yml|yaml|properties|xml|json|log|sh|service|ini|cnf)");

    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");

    public ExtractedSignals extract(String message) {
        String lower = message.toLowerCase();
        return ExtractedSignals.builder()
            .serverHosts(extract(IP_PATTERN, message))
            .filePaths(extract(FILE_PATH_PATTERN, message))
            .serviceNames(KNOWN_SERVICES.stream().filter(lower::contains).collect(Collectors.toList()))
            .commandHints(extractCommandHints(lower))
            .errorPatterns(extractErrorPatterns(message))
            .logKeywords(extractLogKeywords(message))
            .build();
    }

    private List<String> extract(Pattern pattern, String text) {
        List<String> results = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) results.add(matcher.group());
        return results;
    }

    private List<String> extractCommandHints(String lower) {
        List<String> hints = new ArrayList<>();
        List<String> cmds = List.of("systemctl", "service", "journalctl", "tail", "grep",
            "awk", "sed", "find", "curl", "wget", "ping", "telnet", "netstat", "ss",
            "top", "htop", "free", "df", "du", "ps", "kill", "lsof", "iptables",
            "docker", "kubectl", "apt", "yum", "rpm");
        cmds.stream().filter(lower::contains).forEach(hints::add);
        return hints;
    }

    private List<String> extractErrorPatterns(String message) {
        List<String> patterns = new ArrayList<>();
        List<Pattern> errorPatterns = List.of(
            Pattern.compile("(?i)(?:HTTP\\s*)?5\\d{2}"),
            Pattern.compile("(?i)\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d+"),
            Pattern.compile("(?i)(?:error|exception|fatal|panic|oom|segfault)[\\s:]?.{0,50}"),
            Pattern.compile("(?i)connection\\s+(?:refused|timed\\s*out|reset)"),
            Pattern.compile("(?i)permission\\s+denied"),
            Pattern.compile("(?i)no\\s+such\\s+file"),
            Pattern.compile("(?i)disk\\s+(?:full|space)"),
            Pattern.compile("(?i)port\\s+\\d+")
        );
        for (Pattern p : errorPatterns) {
            Matcher m = p.matcher(message);
            while (m.find()) patterns.add(m.group());
        }
        return patterns;
    }

    private List<String> extractLogKeywords(String message) {
        List<String> keywords = new ArrayList<>();
        List<String> logLevels = List.of("error", "warn", "warning", "fatal", "critical",
            "exception", "timeout", "refused", "denied", "failed", "oom");
        String lower = message.toLowerCase();
        logLevels.stream().filter(lower::contains).forEach(keywords::add);
        return keywords;
    }
}
```

### 6.4 ContextSearch — 服务器上下文搜索

```java
@Component
public class ContextSearch {
    @Resource
    private ISshTerminalService sshTerminalService;

    public SearchContext searchBySignals(String terminalSessionId, ExtractedSignals signals) {
        if (terminalSessionId == null || terminalSessionId.isEmpty()) {
            return SearchContext.builder().build();
        }

        SearchContext.SearchContextBuilder builder = SearchContext.builder();

        // 1. 查询服务状态
        if (!signals.getServiceNames().isEmpty()) {
            Map<String, String> statusMap = new LinkedHashMap<>();
            for (String svc : signals.getServiceNames()) {
                String status = safeExec(terminalSessionId,
                    "systemctl is-active " + svc + " 2>/dev/null || " +
                    "service " + svc + " status 2>&1 | head -3");
                statusMap.put(svc, status);
            }
            builder.serviceStatus(statusMap);
        }

        // 2. 读取相关配置文件（取前 50 行）
        if (!signals.getFilePaths().isEmpty()) {
            Map<String, String> contentMap = new LinkedHashMap<>();
            for (String path : signals.getFilePaths()) {
                String content = safeExec(terminalSessionId,
                    "head -50 " + path + " 2>/dev/null");
                if (!content.isEmpty() && !content.contains("No such file")) {
                    contentMap.put(path, content);
                }
            }
            builder.fileContents(contentMap);
        }

        // 3. 搜索最近日志
        if (!signals.getErrorPatterns().isEmpty() || !signals.getLogKeywords().isEmpty()) {
            Map<String, String> logMap = new LinkedHashMap<>();
            for (String svc : signals.getServiceNames()) {
                String logs = safeExec(terminalSessionId,
                    "journalctl -u " + svc + " --no-pager -n 30 --since '1 hour ago' 2>/dev/null || " +
                    "tail -30 /var/log/" + svc + "/*.log 2>/dev/null || " +
                    "tail -30 /var/log/" + svc + ".log 2>/dev/null");
                if (!logs.isEmpty()) logMap.put(svc, logs);
            }
            // 如果没有指定服务但有错误模式，搜索系统日志
            if (logMap.isEmpty() && !signals.getErrorPatterns().isEmpty()) {
                String sysLogs = safeExec(terminalSessionId,
                    "dmesg --time-format iso -T 2>/dev/null | tail -30 || dmesg | tail -30");
                if (!sysLogs.isEmpty()) logMap.put("system", sysLogs);
            }
            builder.recentLogs(logMap);
        }

        return builder.build();
    }

    private String safeExec(String sessionId, String cmd) {
        try {
            String result = sshTerminalService.executeCommand(sessionId, cmd);
            return result != null ? result.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
```

### 6.5 IIntentEnhancerService 与 IntentEnhancerService — 意图增强领域服务

通过暴露接口供 Case 层调用：

```java
package cn.bugstack.ai.domain.agent.service;

public interface IIntentEnhancerService {
    SearchContext enhance(String terminalSessionId, String userMessage);
}
```

```java
package cn.bugstack.ai.domain.agent.service.enhance;

import cn.bugstack.ai.domain.agent.service.IIntentEnhancerService;

@Service
public class IntentEnhancerService implements IIntentEnhancerService {
    @Resource private SignalExtractor signalExtractor;
    @Resource private ContextSearch contextSearch;

    @Override
    public SearchContext enhance(String terminalSessionId, String userMessage) {
        // Step 1: 信号提取
        ExtractedSignals signals = signalExtractor.extract(userMessage);

        boolean hasSignals = !signals.getServiceNames().isEmpty()
            || !signals.getFilePaths().isEmpty()
            || !signals.getErrorPatterns().isEmpty()
            || !signals.getServerHosts().isEmpty();

        if (!hasSignals) {
            return SearchContext.builder().build();
        }

        // Step 2: 根据信号查找服务器上下文
        return contextSearch.searchBySignals(terminalSessionId, signals);
    }
}
```

### 6.6 SearchContext 值对象

```java
@Data
@Builder
public class SearchContext {
    @Builder.Default
    private Map<String, String> serviceStatus = Map.of();
    @Builder.Default
    private Map<String, String> fileContents = Map.of();
    @Builder.Default
    private Map<String, String> recentLogs = Map.of();
}
```

---

## 七、Phase 5：会话持久化

### 7.1 数据库表设计

```sql
-- 会话元数据
CREATE TABLE `chat_session` (
    `id`            VARCHAR(64)     NOT NULL COMMENT '会话ID',
    `agent_id`      VARCHAR(64)     NOT NULL COMMENT '智能体ID',
    `user_id`       VARCHAR(64)     NOT NULL COMMENT '用户ID',
    `title`         VARCHAR(200)    DEFAULT NULL COMMENT '会话标题',
    `created_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `message_count` INT             DEFAULT 0 COMMENT '消息数量',
    PRIMARY KEY (`id`),
    INDEX `idx_user_agent` (`user_id`, `agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话';

-- 对话消息
CREATE TABLE `chat_message` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT,
    `session_id`    VARCHAR(64)     NOT NULL COMMENT '会话ID',
    `role`          VARCHAR(20)     NOT NULL COMMENT '角色: user/assistant/tool/system',
    `content`       TEXT            COMMENT '消息内容',
    `tool_name`     VARCHAR(100)    DEFAULT NULL COMMENT '工具名称',
    `tool_call_id`  VARCHAR(100)    DEFAULT NULL COMMENT '工具调用ID',
    `priority`      VARCHAR(20)     DEFAULT 'MEDIUM' COMMENT '优先级: CRITICAL/HIGH/MEDIUM/LOW',
    `token_count`   INT             DEFAULT 0 COMMENT '预估 token 数',
    `created_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_session_time` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话消息';

-- 里程碑事件
CREATE TABLE `chat_milestone` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT,
    `session_id`    VARCHAR(64)     NOT NULL COMMENT '会话ID',
    `type`          VARCHAR(30)     NOT NULL COMMENT '类型: TASK_CHANGE/ERROR/DECISION/...',
    `content`       TEXT            COMMENT '内容摘要',
    `created_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_session_time` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话里程碑';
```

### 7.2 网关接口

```java
public interface IChatHistoryGateway {
    void saveMessage(String sessionId, ChatMessageEntity message);
    List<ChatMessageEntity> getRecentMessages(String sessionId, int limit);
    List<ChatMessageEntity> getMessagesWithBudget(String sessionId, int tokenBudget);
    void saveMilestone(String sessionId, MilestoneVO milestoneVO);
    List<MilestoneVO> getRecentMilestones(String sessionId, int limit);
}
```

---

## 八、实施计划

### 8.1 分阶段优先级

| 阶段 | 内容 | 依赖 | 预估工作量 |
|------|------|------|-----------|
| **Phase 1** | 动态 Prompt 构建 | 无 | 2 天 |
| **Phase 2** | 上下文记忆管理 | Phase 1 | 4 天 |
| **Phase 5** | 会话持久化 | Phase 2 | 3 天 |
| **Phase 3** | 意图识别系统 | Phase 1 | 3 天 |
| **Phase 4** | 意图增强 | Phase 3 | 3 天 |

### 8.2 建议落地顺序

```
Phase 1（动态 Prompt）
    ↓
Phase 2（上下文记忆）
    ↓
Phase 5（会话持久化）
    ↓
Phase 3（意图识别）
    ↓
Phase 4（意图增强）
```

理由：
1. Phase 1 改动最小、收益最直接，是后续所有功能的基础
2. Phase 2 解决核心痛点（context 超限），Phase 5 是其必要补充
3. Phase 3 + 4 是体验增强层，在基础稳定后叠加

### 8.3 关键改造文件清单

| 文件 | 改造内容 |
|------|---------|
| `IPromptService.java` / `PromptService.java` | **【Phase 1】** 提示词与上下文构建的统一领域服务 |
| `DynamicPromptBuilder.java` | 动态组装提示词的底层组件 |
| `MilestoneTracker.java` | 里程碑关键事件的检测与存储 |
| `IChatContextService.java` / `ChatContextService.java` | **【Phase 2】** 上下文管理领域服务，封装 Provider 和 Reducer 逻辑 |
| `IIntentService.java` / `IntentService.java` | **【Phase 3】** 意图分类领域服务，封装两层分类器逻辑 |
| `IIntentEnhancerService.java` / `IntentEnhancerService.java` | **【Phase 4】** 意图增强领域服务，封装信号提取与搜索 |
| `AiCallNode.java` | **【Phase 1】** 注入 `IPromptService`，实现环境信息和意图等上下文的动态注入 |
| `AIAgentReActServiceCase.java` | 在 chatStream 入口处调用领域服务（如 `IIntentService`、`IIntentEnhancerService`）进行意图识别和增强 |
| `DefaultReActFactory.java` | DynamicContext 新增意图结果和搜索上下文字段 |
| `ChatService.java` | 集成会话持久化 |
| `SshExecuteAdkTool.java` | 工具结果推送到 MilestoneTracker 和 ToolResultProvider |
| `application-dev.yml` | 新增意图识别和上下文管理的配置开关 |

---

## 九、与 WaLiCode 设计的关键适配差异

| 维度 | WaLiCode | WaLiSSH 适配 |
|------|----------|-------------|
| 运行环境 | Tauri 前端进程 | Spring Boot 后端，多用户并发 |
| LLM 调用 | 直接 HTTP API | Spring AI → ADK 桥接 |
| 上下文来源 | 编辑器文件、光标、Tab | SSH 终端状态、命令历史、服务状态 |
| 信号提取 | 代码文件路径、符号名 | 服务器地址、文件路径、服务名、错误码 |
| 会话存储 | Tauri 文件系统（本地 JSON） | MySQL + Redis（服务端持久化） |
| 并发模型 | 单用户单进程 | 多用户多线程（ConcurrentHashMap + Redis） |
| 意图类型 | code_edit / debug / refactor | DIAGNOSE / CONFIGURE / DEPLOY / MONITOR |
| Provider 适配 | FileProvider（编辑器文件） | TerminalStateProvider（终端状态、系统信息） |
| 分类器层数 | 三层（规则→模型→LLM） | 两层（规则→LLM），后端省去中间层降低延迟 |
| 缓存策略 | 内存 Map | Caffeine 本地缓存 + Redis 分布式缓存 |
