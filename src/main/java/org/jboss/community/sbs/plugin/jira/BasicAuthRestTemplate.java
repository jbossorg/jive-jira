package org.jboss.community.sbs.plugin.jira;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Inspired by
 * https://github.com/izeye/samples-spring-boot-branches/blob/rest-and-actuator-with-security/src/main/java/samples/springboot/util/BasicAuthRestTemplate.java
 *
 * @author Libor Krzyzanek
 */
public class BasicAuthRestTemplate extends RestTemplate {

    private static final Logger log = LogManager.getLogger(RemoteJiraManagerImpl.class);

    public BasicAuthRestTemplate(String username, String password) {
        addAuthentication(username, password);
        setErrorHandler(new LogErrorHandler());
    }

    private void addAuthentication(String username, String password) {
        if (username == null) {
            return;
        }
        List<ClientHttpRequestInterceptor> interceptors = Collections
                .<ClientHttpRequestInterceptor>singletonList(
                        new BasicAuthorizationInterceptor(username, password));
        setRequestFactory(new InterceptingClientHttpRequestFactory(getRequestFactory(),
                interceptors));
    }

    private static class BasicAuthorizationInterceptor implements
            ClientHttpRequestInterceptor {

        private final String username;

        private final String password;

        public BasicAuthorizationInterceptor(String username, String password) {
            this.username = username;
            this.password = (password == null ? "" : password);
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            byte[] token = Base64.getEncoder().encode(
                    (this.username + ":" + this.password).getBytes());
            request.getHeaders().add("Authorization", "Basic " + new String(token));
            return execution.execute(request, body);
        }

    }

    public class LogErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            try {
                super.handleError(response);
            } catch (HttpClientErrorException e) {
                log.error("Error invoking rest api. Body: " + e.getResponseBodyAsString());
                throw e;
            }

        }

    }

}
