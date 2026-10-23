package github.javaguide.utils;

/**
 * String 工具类
 * 自定义字符串空白判断工具类
 *
 * @author zhanghua
 * @createTime 2022/3/6 12:58 上午
 */
public class StringUtil {

    public static boolean isBlank(String s) {
        // 1. 先判断：字符串为null 或 空字符串"" → 直接返回true
        if (s == null || s.length() == 0) {
            return true;
        }
        // 2. 遍历字符串的每一个字符
        for (int i = 0; i < s.length(); ++i) {
            // 3. 只要存在【非空白字符】，说明不是空白串 → 返回false
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        // 4. 遍历完所有字符都是空白 → 返回true
        return true;
    }
}
