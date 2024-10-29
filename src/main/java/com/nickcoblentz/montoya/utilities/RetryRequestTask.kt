package com.nickcoblentz.montoya.utilities

import burp.api.montoya.MontoyaApi
import burp.api.montoya.http.message.HttpRequestResponse
import burp.api.montoya.http.message.requests.HttpRequest
import com.nickcoblentz.montoya.LogLevel
import com.nickcoblentz.montoya.MontoyaLogger
import java.lang.String

class RetryRequestsTask(private val api: MontoyaApi, private var request: HttpRequest) {

    fun run() {
        if (request.hasHeader("Content-Length"))
            request = request.withUpdatedHeader("Content-Length", request.body().length().toString())
        api.http().sendRequest(request)
    }
}