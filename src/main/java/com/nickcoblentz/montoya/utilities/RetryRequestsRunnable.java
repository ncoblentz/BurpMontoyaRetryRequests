package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;

import java.util.List;

public class RetryRequestsRunnable implements Runnable {
    private MontoyaApi _API;
    private HttpRequest _Request;
    public RetryRequestsRunnable(MontoyaApi api, HttpRequestResponse requestResponse)
    {
        _API=api;
        _Request = requestResponse.request();
    }

    public RetryRequestsRunnable(MontoyaApi api, HttpRequest request)
    {
        _API=api;
        _Request = request;
    }
    @Override
    public void run() {
        if(_Request!=null)
        {
            _API.http().sendRequest(_Request);
        }
    }
}


/*

    @Override
    public void run() {
        for(int i = 0;i<this.requestResponseArray.length;i++){
            IHttpRequestResponse requestResponse = this.requestResponseArray[i];

            IHttpService httpService = requestResponse.getHttpService();
            byte[] request = requestResponse.getRequest();

            IHttpRequestResponse httpRequestResponseResult = this.callbacks.makeHttpRequest(httpService, request);
        }
    }
 */
