package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
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
    public static final String RETRY_VERBS_CONTENT_LENGTH = "RetryVerbsWContentLength";
    public static final String RETRY_VERBS_CONTENT_LENGTH_JSON = "RetryVerbsWContentLengthJson";

    private List<String> _Verbs = List.of("OPTIONS","POST","PUT","PATCH","HEAD","GET","TRACE","TRACK","LOCK","UNLOCK","FAKE","CONNECT","COPY","MOVE","LABEL","UPDATE","VERSION-CONTROL","UNCHECKOUT","CHECKOUT","DELETE");
    private List<String> _VerbsNoBody = List.of("GET","OPTIONS","HEAD","CONNECT","TRACE");
    private ArrayList<Component> _MenuItemList;
    private MontoyaApi _API;
    private JMenuItem _RetryRequestJMenu = new JMenuItem(RETRY_REQUESTS);
    private JMenuItem _RetryVerbsJMenu = new JMenuItem(RETRY_VERBS);
    private JMenuItem _RetryVerbsCLJMenu = new JMenuItem(RETRY_VERBS_CONTENT_LENGTH);
    private JMenuItem _RetryVerbsCLJSONJMenu = new JMenuItem(RETRY_VERBS_CONTENT_LENGTH_JSON);
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
        _RetryVerbsCLJMenu.addActionListener(this);
        _RetryVerbsCLJMenu.setActionCommand(RETRY_VERBS_CONTENT_LENGTH);
        _RetryVerbsCLJSONJMenu.addActionListener(this);
        _RetryVerbsCLJSONJMenu.setActionCommand(RETRY_VERBS_CONTENT_LENGTH_JSON);


        _MenuItemList = new ArrayList<Component>();
        _MenuItemList.add(_RetryRequestJMenu);
        _MenuItemList.add(_RetryVerbsJMenu);
        _MenuItemList.add(_RetryVerbsCLJMenu);
        _MenuItemList.add(_RetryVerbsCLJSONJMenu);

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
            int count=1;
            int total=requestResponses.size();
            for (HttpRequestResponse requestResponse : requestResponses) {
                MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API, requestResponse,count++,total));
            }
        }
        else {
            int total = requestResponses.size()*_Verbs.size();
            int count=1;
            for (HttpRequestResponse requestResponse : requestResponses) {
                for(String verb : _Verbs)
                {
                    HttpRequestResponse newRequestResponse = requestResponse;

                    if(actionEvent.getActionCommand().equals(RETRY_VERBS_CONTENT_LENGTH) || actionEvent.getActionCommand().equals(RETRY_VERBS_CONTENT_LENGTH_JSON))
                    {
                        if(!newRequestResponse.request().hasHeader("Content-Length"))
                        {
                            newRequestResponse = HttpRequestResponse.httpRequestResponse(newRequestResponse.request().withAddedHeader("Content-Length","0"),newRequestResponse.response());
                        }
                    }

                    if(actionEvent.getActionCommand().equals(RETRY_VERBS_CONTENT_LENGTH_JSON))
                    {
                        if(newRequestResponse.request().hasHeader("Content-Type")) {
                            newRequestResponse = HttpRequestResponse.httpRequestResponse(newRequestResponse.request().withUpdatedHeader("Content-Type", "application/json"), newRequestResponse.response());
                        }
                        else {
                            newRequestResponse = HttpRequestResponse.httpRequestResponse(newRequestResponse.request().withAddedHeader("Content-Type", "application/json"), newRequestResponse.response());
                        }

                        if(newRequestResponse.request().bodyToString().isEmpty() && !_VerbsNoBody.contains(verb))
                            newRequestResponse = HttpRequestResponse.httpRequestResponse(newRequestResponse.request().withBody("{}"),newRequestResponse.response());
                        if(_VerbsNoBody.contains(verb) && !newRequestResponse.request().bodyToString().isEmpty())
                        {
                            MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API, newRequestResponse.request().withMethod(verb),count++,total));
                            newRequestResponse = HttpRequestResponse.httpRequestResponse(newRequestResponse.request().withBody(""),newRequestResponse.response());
                        }
                    }

                    MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API, newRequestResponse.request().withMethod(verb),count++,total));
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
