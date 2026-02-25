package com.livewave.ticket_api.graphql;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

@Component
public class GraphQlHttpRequestInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {

        HttpServletRequest servletRequest = null;
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            servletRequest = sra.getRequest();
        }

        // важно: переменная должна быть final для лямбды
        final HttpServletRequest finalReq = servletRequest;

        if (finalReq != null) {
            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(ctx -> ctx.put(HttpServletRequest.class, finalReq)).build()
            );
        }

        return chain.next(request);
    }
}