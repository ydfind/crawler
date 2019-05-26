package util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class RestTemplateRequest {

    private static class RestTemplateHolder {
        private static final RestTemplate TEMPLATE = restTemplate();
    }

    private static RestTemplate restTemplate(){

        RestTemplate restTemplate = null;
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectTimeout(30000);
            requestFactory.setReadTimeout(30000);
            restTemplate = new RestTemplate(requestFactory);
            List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
            HttpMessageConverter<?> converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            messageConverters.add(1,converter);
            restTemplate.setMessageConverters(messageConverters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return restTemplate;
    }

    public static final RestTemplate getRestTemplate() {
        return RestTemplateHolder.TEMPLATE;
    }

    public static String requestGet(String url){
        URI uri = URI.create(url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAcceptCharset(Arrays.asList(StandardCharsets.UTF_8));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> exchange = getRestTemplate().exchange(uri, HttpMethod.GET, httpEntity, String.class);
        return exchange.getBody();
    }
}
