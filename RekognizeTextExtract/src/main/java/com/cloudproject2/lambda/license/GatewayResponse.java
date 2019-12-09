package com.cloudproject2.lambda.license;

import java.util.Map;

public class GatewayResponse {
    private UserLicense body;
    private Integer statusCode;
    private Map<String, String> headers;

    public UserLicense getBody() {
        return body;
    }

    public GatewayResponse setBody(UserLicense body) {
        this.body = body;
        return this;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public GatewayResponse setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public GatewayResponse setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }
}
