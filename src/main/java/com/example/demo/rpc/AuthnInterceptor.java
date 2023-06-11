package com.example.demo.rpc;

import com.example.demo.service.TokenService;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthnInterceptor implements ServerInterceptor {
    public static final Context.Key<String> CTX_KEY = Context.key("username");
    private static final String JWT_KEY = "authn";
    private final TokenService tokens;

    @Autowired
    AuthnInterceptor(
            TokenService tokens
    ) {
        this.tokens = tokens;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String token = headers.get(Metadata.Key.of(JWT_KEY, Metadata.ASCII_STRING_MARSHALLER));
        if (token == null) {
            call.close(Status.INVALID_ARGUMENT.withDescription("no JWT token was provided"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }

        String username;
        try {
            username = tokens.verify(token);
        } catch (TokenService.VerificationException ex) {
            call.close(Status.UNAUTHENTICATED.withDescription("token is invalid"), new Metadata());
            return new ServerCall.Listener<>() {
            };
        }
        return Contexts.interceptCall(
                Context.current().withValue(CTX_KEY, username), call, headers, next
        );
    }
}
