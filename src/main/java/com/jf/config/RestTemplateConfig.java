package com.jf.config;

import com.jf.conventer.WxMappingJackson2HttpMessageConverter;
import com.jf.ssl.Myssl;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;


/**
 * @author 江峰
 * @create 2020-01-14   17:32
 */
public class RestTemplateConfig {
    public static RestTemplate getRestTemplate() {
        Myssl factory = new Myssl();
        factory.setReadTimeout(5000);
        factory.setConnectTimeout(15000);
        RestTemplate restTemplate = new RestTemplate(factory);
        // RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        // List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        // 请求响应日志拦截
        // interceptors.add(new LoggingRequestInterceptor());
        // restTemplate.setInterceptors(interceptors);
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
}
