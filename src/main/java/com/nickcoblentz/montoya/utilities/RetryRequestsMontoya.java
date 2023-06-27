package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;

public class RetryRequestsMontoya implements BurpExtension {
    private MontoyaApi _API;
    private MyThreadPool _MyThreadPool;
    private Registration _ContextItemsProviderRegistration;

    @Override
    public void initialize(MontoyaApi api) {
        _API=api;
        api.extension().setName("Retry Requests");
         _MyThreadPool = MyThreadPool.getInstance();
        _ContextItemsProviderRegistration = api.userInterface().registerContextMenuItemsProvider(new RetryRequestsContextMenuProvider(api,_MyThreadPool));
    }



}
