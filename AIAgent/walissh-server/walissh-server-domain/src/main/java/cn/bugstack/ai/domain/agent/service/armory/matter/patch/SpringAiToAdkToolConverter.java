package cn.bugstack.ai.domain.agent.service.armory.matter.patch;

import com.google.adk.tools.FunctionTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI ToolCallback → Google ADK FunctionTool 转换器
 *
 * 通过反射从 Spring AI 的 MethodToolCallback 提取 toolMethod 和 toolObject，
 * 然后使用 FunctionTool.create(obj, methodName) 转换为 ADK FunctionTool。
 */
@Slf4j
@Component
public class SpringAiToAdkToolConverter {

    /**
     * 将 Spring AI ToolCallback 列表转换为 ADK FunctionTool 列表
     */
    public List<Object> convert(List<ToolCallback> toolCallbacks) {
        List<Object> adkTools = new ArrayList<>();
        if (toolCallbacks == null || toolCallbacks.isEmpty()) {
            return adkTools;
        }

        for (ToolCallback callback : toolCallbacks) {
            try {
                Object adkTool = convertSingle(callback);
                if (adkTool != null) {
                    adkTools.add(adkTool);
                }
            } catch (Exception e) {
                log.warn("转换 ToolCallback 失败: {}, 原因: {}", callback.getClass().getName(), e.getMessage(), e);
            }
        }

        return adkTools;
    }

    private Object convertSingle(ToolCallback callback) {
        // 检查是否是 MethodToolCallback 类型
        if (!callback.getClass().getName().contains("MethodToolCallback")) {
            log.warn("不支持的 ToolCallback 类型: {}", callback.getClass().getName());
            return null;
        }

        ToolDefinition def = callback.getToolDefinition();
        String toolName = def != null ? def.name() : "unknown";

        // 通过反射获取 toolMethod 和 toolObject 字段
        Method toolMethod = null;
        Object toolObject = null;

        try {
            toolMethod = (Method) getPrivateField(callback, "toolMethod");
            toolObject = getPrivateField(callback, "toolObject");
        } catch (Exception e) {
            log.warn("反射获取字段失败: {}", e.getMessage());
            return null;
        }

        if (toolMethod == null || toolObject == null) {
            log.warn("未获取到 toolMethod 或 toolObject");
            return null;
        }

        log.info("转换工具: name={}, method={}, object={}",
                toolName, toolMethod.getName(), toolObject.getClass().getName());

        try {
            FunctionTool functionTool = FunctionTool.create(toolObject, toolMethod.getName());
            log.info("FunctionTool 创建成功: name={}", functionTool.name());
            return functionTool;
        } catch (Exception e) {
            log.warn("FunctionTool.create 失败: {}", e.getMessage());
            return null;
        }
    }

    private Object getPrivateField(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    try {
                        field.setAccessible(true);
                        return field.get(obj);
                    } catch (Exception e) {
                        log.debug("读取字段 {} 失败: {}", fieldName, e.getMessage());
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
