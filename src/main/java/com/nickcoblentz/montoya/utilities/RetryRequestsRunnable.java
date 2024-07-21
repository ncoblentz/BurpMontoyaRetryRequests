package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.nickcoblentz.montoya.LogLevel;
import com.nickcoblentz.montoya.MontoyaLogger;

import java.util.List;

public class RetryRequestsRunnable implements Runnable {
    private MontoyaApi _API;
    private HttpRequest _Request;
    private int _myNumber=0;
    private int _total=0;
    public RetryRequestsRunnable(MontoyaApi api, HttpRequestResponse requestResponse, int myNumber, int total)
    {
        this(api,requestResponse.request(),myNumber, total);
    }

    public RetryRequestsRunnable(MontoyaApi api, HttpRequest request, int myNumber, int total)
    {
        _API=api;
        _Request = request;
        _myNumber=myNumber;
        _total=total;
    }
    @Override
    public void run() {
        if(_Request!=null)
        {
            MontoyaLogger logger = new MontoyaLogger(_API, LogLevel.DEBUG);
            _API.http().sendRequest(_Request);
            logger.debugLog(this.getClass().getCanonicalName(),String.format("Finished %s of %s",_myNumber,_total));
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
