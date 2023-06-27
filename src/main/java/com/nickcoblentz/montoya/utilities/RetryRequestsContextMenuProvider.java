package com.nickcoblentz.montoya.utilities;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
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

    private ArrayList<Component> _MenuItemList;
    private MontoyaApi _API;
    private JMenuItem _RetryRequestJMenu = new JMenuItem("RetryRequests");
    private ContextMenuEvent _Event;

    private MyThreadPool _MyThreadPool;

    public RetryRequestsContextMenuProvider(MontoyaApi api, MyThreadPool threadPool)
    {
        _API=api;
        _MyThreadPool=threadPool;
        _RetryRequestJMenu.addActionListener(this);
        _MenuItemList = new ArrayList<Component>();
        _MenuItemList.add(_RetryRequestJMenu);

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

        if(!_Event.selectedRequestResponses().isEmpty())
        {
            for(HttpRequestResponse requestResponse : _Event.selectedRequestResponses())
            {
                MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API,requestResponse));
            }
        }
        else if(_Event.messageEditorRequestResponse().isPresent() && !_Event.messageEditorRequestResponse().isEmpty())
        {
            if(_Event.messageEditorRequestResponse().get().requestResponse().request()!=null)
            {
                MyThreadPool.getInstance().addRunnable(new RetryRequestsRunnable(_API,_Event.messageEditorRequestResponse().get().requestResponse()));
            }
        }
    }
}
