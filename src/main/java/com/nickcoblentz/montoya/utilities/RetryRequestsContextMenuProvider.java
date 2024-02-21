package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.AuditIssueContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.WebSocketContextMenuEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RetryRequestsContextMenuProvider implements ContextMenuItemsProvider, ActionListener {

    public static final String RETRY_REQUESTS = "RetryRequests";
    public static final String RETRY_VERBS = "RetryVerbs";

    private List<String> _Verbs = List.of("OPTIONS","POST","PUT","PATCH","HEAD","GET","TRACE","TRACK","LOCK","UNLOCK","FAKE","CONNECT","COPY","MOVE","LABEL","UPDATE","VERSION-CONTROL","UNCHECKOUT","CHECKOUT","DELETE");
    private ArrayList<Component> _MenuItemList;
    private MontoyaApi _API;
    private JMenuItem _RetryRequestJMenu = new JMenuItem(RETRY_REQUESTS);
    private JMenuItem _RetryVerbsJMenu = new JMenuItem(RETRY_VERBS);
    private ContextMenuEvent _Event;

    private MyThreadPool _MyThreadPool;

    public RetryRequestsContextMenuProvider(MontoyaApi api, MyThreadPool threadPool)
    {
        _API=api;
        _MyThreadPool=threadPool;
        _RetryRequestJMenu.addActionListener(this);
        _RetryRequestJMenu.setActionCommand(RETRY_REQUESTS);
        _RetryVerbsJMenu.addActionListener(this);
        _RetryVerbsJMenu.setActionCommand(RETRY_VERBS);
        _MenuItemList = new ArrayList<Component>();
        _MenuItemList.add(_RetryRequestJMenu);
        _MenuItemList.add(_RetryVerbsJMenu);

    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        _Event=event;

        if(!event.selectedRequestResponses().isEmpty() || (event.messageEditorRequestResponse().isPresent() && !event.messageEditorRequestResponse().isEmpty()))
        {
            return _MenuItemList;
        }

        return Collections.emptyList();
    }

    @Override
    public List<Component> provideMenuItems(WebSocketContextMenuEvent event) {
        return Collections.emptyList();
    }

    @Override
    public List<Component> provideMenuItems(AuditIssueContextMenuEvent event) {
        return Collections.emptyList();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        List<HttpRequestResponse> requestResponses = getRequestResponseFromEvent();

        if(actionEvent.getActionCommand().equals(RETRY_REQUESTS)) {
            for (HttpRequestResponse requestResponse : requestResponses) {
                MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API, requestResponse));
            }
        }
        else if(actionEvent.getActionCommand().equals(RETRY_VERBS)) {
            for (HttpRequestResponse requestResponse : requestResponses) {
                for(String verb : _Verbs)
                {
                    HttpRequestResponse newRequestResponse = requestResponse;

                    MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API, requestResponse.request().withMethod(verb)));
                }

            }
        }
    }

    private List<HttpRequestResponse> getRequestResponseFromEvent()
    {
        List<HttpRequestResponse> result= new ArrayList<HttpRequestResponse>();;
        if (!_Event.selectedRequestResponses().isEmpty()) {
            result=_Event.selectedRequestResponses();
        }
        else if (_Event.messageEditorRequestResponse().isPresent() && !_Event.messageEditorRequestResponse().isEmpty()) {
            if (_Event.messageEditorRequestResponse().get().requestResponse().request() != null) {
                result.add(_Event.messageEditorRequestResponse().get().requestResponse());
                return result;
            }
        }
        return result;
    }
}
