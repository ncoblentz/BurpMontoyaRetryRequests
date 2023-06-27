package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.List;

public class RetryRequestsRunnable implements Runnable {
    private MontoyaApi _API;
    private HttpRequestResponse _RequestResponse;
    public RetryRequestsRunnable(MontoyaApi api, HttpRequestResponse requestResponse)
    {
        _API=api;
        _RequestResponse = requestResponse;
    }

    @Override
    public void run() {
        if(_RequestResponse.request()!=null)
        {
            _API.http().sendRequest(_RequestResponse.request());
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
