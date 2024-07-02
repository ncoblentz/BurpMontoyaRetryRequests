package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import com.nickcoblentz.montoya.MontoyaLogger;

public class RetryRequestsMontoya implements BurpExtension {
    private MontoyaApi _API;
    private MyThreadPool _MyThreadPool;
    private Registration _ContextItemsProviderRegistration;

    @Override
    public void initialize(MontoyaApi api) {
        _API=api;
        api.extension().setName("Retry Requests");
        MontoyaLogger logger = new MontoyaLogger(api,MontoyaLogger.DebugLogLevel);
        logger.debugLog(this.getClass().getName(),"Loading Plugin");
         _MyThreadPool = MyThreadPool.getInstance();
        _ContextItemsProviderRegistration = api.userInterface().registerContextMenuItemsProvider(new RetryRequestsContextMenuProvider(api,_MyThreadPool));
        logger.debugLog(this.getClass().getName(),"Finished Loading Plugin");
    }



}
