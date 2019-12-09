package com.cloudproject2.lambda.license;

public class GatewayRequest {
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public GatewayRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String toString() {
        return "GatewayRequest{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
