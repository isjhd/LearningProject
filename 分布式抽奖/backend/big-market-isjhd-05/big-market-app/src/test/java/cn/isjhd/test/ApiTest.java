package cn.isjhd.test;

import cn.isjhd.trigger.api.dto.RaffleAwardListRequestDTO;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

//功能测试
@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class ApiTest {

    @Test
    public void test_integer(){
        System.out.println(Integer.parseInt("109"));
    }

    @Test
    public void test() {
        RaffleAwardListRequestDTO requestDTO = new RaffleAwardListRequestDTO();
        requestDTO.setUserId("xiaofuge");
        requestDTO.setActivityId(100301L);
        log.info(JSON.toJSONString(requestDTO));
    }

    public static void main(String[] args) {
        double convert = convert(0.0018);
        System.out.println(convert);
    }

    private static double convert(double min){
        double current = min;
        double max = 1;
        while (current % 1 != 0){
            current = current * 10;
            max = max * 10;
        }
        return max;
    }

}
