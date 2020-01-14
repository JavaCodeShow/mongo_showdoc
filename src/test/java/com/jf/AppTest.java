package com.jf;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void hello() {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("接受到了请求");
        String str = restTemplate.getForObject("http://139.224.103.236", String.class);
        System.out.println("调用结束");
        System.out.println(str);
    }
}
