package github.javaguide.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author shuang.kou
 * @createTime 2020年07月21日 14:25:
 * 读取项目类路径下 .properties 配置文件的工具类
 **/
@Slf4j
public final class PropertiesFileUtil {

    private PropertiesFileUtil() {
    }

    /**
     * 读取类路径下的properties配置文件
     * @param fileName 配置文件名（如：rpc.properties）
     * @return 配置对象，读取失败返回null
     */
    public static Properties readPropertiesFile(String fileName) {
        // 1. 获取当前线程的上下文类加载器，读取类路径根目录的URL
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            // 2. 拼接配置文件完整路径
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        // 3. try-with-resources：自动关闭流，避免资源泄漏
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            // 4. 加载配置文件到Properties对象
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}
