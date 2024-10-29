package com.nickcoblentz.montoya.utilities

import burp.api.montoya.MontoyaApi
import com.nickcoblentz.montoya.LogLevel
import com.nickcoblentz.montoya.MontoyaLogger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

class MyExecutor (val api : MontoyaApi,var limitConcurrentRequests : Boolean = false, var concurrentRequestLimit : Int = 10){
    private val executorService: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
    private val logger : MontoyaLogger = MontoyaLogger(api,LogLevel.DEBUG)
    private var queuedRequests = 0

    var semaphore = Semaphore(concurrentRequestLimit)
        public get
        private set

    fun limitConcurrentRequests(concurrentRequestCount: Int = 10) {
        if(!limitConcurrentRequests && concurrentRequestCount!=concurrentRequestLimit) {
            concurrentRequestLimit=concurrentRequestCount
            semaphore=Semaphore(concurrentRequestLimit)
            limitConcurrentRequests = true
        }
        printLoggingInfo()
    }

    fun removeConcurrentRequestLimit()
    {
        limitConcurrentRequests=false
        printLoggingInfo()
    }

    fun runTask(retryRequestTask: RetryRequestsTask) {
        executorService.submit {
            queuedRequests++
            printLoggingInfo()

            if(limitConcurrentRequests)
                semaphore.acquire()

            retryRequestTask.run()

            if(limitConcurrentRequests)
                semaphore.release()

            printLoggingInfo()
            queuedRequests--
        }
    }

    fun printLoggingInfo() {
        if(limitConcurrentRequests) {
            logger.debugLog("Concurrent Request Limit = ${concurrentRequestLimit}")
        }
        else {
            logger.debugLog("No concurrent request limits")
        }

        logger.debugLog("Semaphore: Queue Length = ${semaphore.queueLength}, Available Permits = ${semaphore.availablePermits()}")
        logger.debugLog("Queued Requests: $queuedRequests")
    }


}