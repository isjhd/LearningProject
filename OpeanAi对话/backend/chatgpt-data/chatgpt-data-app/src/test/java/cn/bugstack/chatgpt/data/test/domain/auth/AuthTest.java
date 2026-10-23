package cn.bugstack.chatgpt.data.test.domain.auth;

import cn.bugstack.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import cn.bugstack.chatgpt.data.domain.auth.service.IAuthService;
import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-08-05 19:48
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthTest {

    @Resource
    private IAuthService authService;

    @Resource
    private Cache<String, String> cache;

    /** SecretKey 原始秘钥字符串 */
    private static final String defaultBase64EncodedSecretKey = "isjhd";

    // Base64 编码（格式转换），统一字符编码
    private final String base64EncodedSecretKey = Base64.encodeBase64String(defaultBase64EncodedSecretKey.getBytes());

    // HS256 对称加密算法，保护敏感数据、生成 JWT 签名
    private final Algorithm algorithm = Algorithm.HMAC256(Base64.decodeBase64(base64EncodedSecretKey));

    @Test
    public void test_AuthService() {

        cache.put("1000", "xiaofuge");
        cache.put("xiaofuge", "1000");

        AuthStateEntity authStateEntity = authService.doLogin("1000");
        log.info(JSON.toJSONString(authStateEntity));

        authService.checkToken(authStateEntity.getToken());
    }

    @Test
    public void test_code() {

        // 3. 获取 Token 并返回
        Map<String, Object> chaim = new HashMap<>();
        chaim.put("openId", "wj");
        String token = encode("wj", 7 * 24 * 60 * 60 * 1000, chaim);
        log.info("token:{}", token);

        Claims claims = decode(token);
        log.info("claims:{}", claims);

        boolean verify = isVerify(token);
        log.info("verify:{}", verify);


    }

    protected String encode(String issuer, long ttlMillis, Map<String, Object> claims) {
        // iss签发人，ttlMillis生存时间，claims是指还想要在jwt中存储的一些非隐私信息
        if (claims == null) {
            claims = new HashMap<>();
        }
        long nowMillis = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                // 荷载部分，存放内容
                .setClaims(claims)
                // 这个是JWT的唯一标识，一般设置成唯一的，这个方法可以生成唯一标识
                .setId(UUID.randomUUID().toString())//2.
                // 签发时间
                .setIssuedAt(new Date(nowMillis))
                // 签发人，也就是JWT是给谁的（逻辑上一般都是username或者userId）
                .setSubject(issuer)
                .signWith(SignatureAlgorithm.HS256, base64EncodedSecretKey);//这个地方是生成jwt使用的算法和秘钥
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);// 4. 过期时间，这个也是使用毫秒生成的，使用当前时间+前面传入的持续时间生成
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    // 相当于encode的方向，传入jwtToken生成对应的username和password等字段。Claim就是一个map
    // 也就是拿到荷载部分所有的键值对
    protected Claims decode(String jwtToken) {
        // 得到 DefaultJwtParser
        return Jwts.parser()
                // 设置签名的秘钥
                .setSigningKey(base64EncodedSecretKey)
                // 设置需要解析的 jwt
                .parseClaimsJws(jwtToken)
                .getBody();
    }

    // 判断jwtToken是否合法
    protected boolean isVerify(String jwtToken) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(jwtToken);
            // 校验不通过会抛出异常
            // 判断合法的标准：1. 头部和荷载部分没有篡改过。2. 没有过期
            return true;
        } catch (Exception e) {
            log.error("jwt isVerify Err", e);
            return false;
        }

    }

}
