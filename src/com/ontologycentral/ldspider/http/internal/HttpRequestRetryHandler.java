package com.ontologycentral.ldspider.http.internal;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public class HttpRequestRetryHandler implements
	org.apache.http.client.HttpRequestRetryHandler {
	
	int _retries;
	
	public HttpRequestRetryHandler(int retries) {
		_retries = retries;
	}

    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
	if (executionCount >= _retries) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof NoHttpResponseException) {
            // Retry if the server dropped connection on us
            return true;
        }
        if (exception instanceof SSLHandshakeException) {
            // Do not retry on SSL handshake exception
            return false;
        }
        HttpRequest request = (HttpRequest) context.getAttribute(
                ExecutionContext.HTTP_REQUEST);
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest); 
        if (idempotent) {
            // Retry if the request is considered idempotent 
            return true;
        }
        return false;
    }

}
