package com.yupi.springbootinit.manager;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用于对接 AI 平台
 */
@Service
@Slf4j
public class AiManager {

    // DeepSeek API 地址
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"; // 替换为实际 API 地址

    //你的 API Key
    private static final String API_KEY = "替换成自己的key";

    /**
     * AI 对话
     *
     * @param message
     * @return
     */
    public String doChat(String message) {
        // 写系统预设
        final String SYSTEM_PROMPT = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象js代码（输出 json 格式），合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【\n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释}\n" +
                "【【【【【";

        // 构造请求参数
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat"); // 模型名称

        // 构造 messages 数组
        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        JSONObject userMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(systemMessage);
        messages.add(userMessage);

        requestBody.put("messages", messages); // 设置 messages
//        requestBody.put("temperature", 0.7); // 温度参数
//        requestBody.put("max_tokens", 100); // 最大 token 数

        // 发送 POST 请求
        String response = HttpUtil.createPost(DEEPSEEK_API_URL)
                .header("Authorization", "Bearer " + API_KEY) // 添加认证头
                .header("Content-Type", "application/json") // 设置请求头
                .body(requestBody.toString()) // 设置请求体
                .execute().body();

        // 处理响应
        //System.out.println("API 响应: " + response);

        // 解析响应 JSON
        JSONObject jsonResponse = JSONUtil.parseObj(response);
        return jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getStr("content");

    }
}
