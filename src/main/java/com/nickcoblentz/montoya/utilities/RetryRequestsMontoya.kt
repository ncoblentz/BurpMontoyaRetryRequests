package com.nickcoblentz.montoya.utilities

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.proxy.websocket.*
import burp.api.montoya.ui.settings.SettingsPanelBuilder
import burp.api.montoya.ui.settings.SettingsPanelPersistence
import com.nickcoblentz.montoya.LogLevel
import com.nickcoblentz.montoya.MontoyaLogger
import com.nickcoblentz.montoya.settings.PanelSettingsDelegate

class RetryRequestsMontoya : BurpExtension, ProxyWebSocketCreationHandler {
    private lateinit var api: MontoyaApi
    private lateinit var logger: MontoyaLogger
    private lateinit var myExecutor: MyExecutor

    private var currentLimit = 10
    var pollingThread: Thread? = null
    var exiting = false

    private lateinit var myExtensionSettings : MyExtensionSettings


    private val proxyWebSockets = mutableListOf<ProxyWebSocketCreation>()

    override fun initialize(api: MontoyaApi?) {
        this.api = requireNotNull(api) { "api : MontoyaApi is not allowed to be null" }
        logger = MontoyaLogger(api,LogLevel.DEBUG)

        logger.debugLog("Started loading the extension...")

        api.extension().setName("Retry Requests")



        myExtensionSettings = MyExtensionSettings()
        api.userInterface().registerSettingsPanel(myExtensionSettings.settingsPanel)

        myExecutor = MyExecutor(api,myExtensionSettings)
        api.userInterface().registerContextMenuItemsProvider(RetryRequestsContextMenuProvider(api, myExecutor,proxyWebSockets))
        api.proxy().registerWebSocketCreationHandler(this)


        logger.debugLog("...Finished loading the extension")
    }

    override fun handleWebSocketCreation(proxyWebSocketCreation: ProxyWebSocketCreation?) {
        proxyWebSocketCreation?.let {
            it.proxyWebSocket().registerProxyMessageHandler(object : ProxyMessageHandler {
                override fun handleTextMessageReceived(interceptedTextMessage: InterceptedTextMessage): TextMessageReceivedAction {
                    return TextMessageReceivedAction.continueWith(interceptedTextMessage)
                }

                override fun handleTextMessageToBeSent(interceptedTextMessage: InterceptedTextMessage?): TextMessageToBeSentAction {
                    return TextMessageToBeSentAction.continueWith(interceptedTextMessage)
                }

                override fun handleBinaryMessageReceived(interceptedBinaryMessage: InterceptedBinaryMessage?): BinaryMessageReceivedAction {
                    return BinaryMessageReceivedAction.continueWith(interceptedBinaryMessage)
                }

                override fun handleBinaryMessageToBeSent(interceptedBinaryMessage: InterceptedBinaryMessage?): BinaryMessageToBeSentAction {
                    return BinaryMessageToBeSentAction.continueWith(interceptedBinaryMessage)
                }

                override fun onClose() {
                    super.onClose()
                    proxyWebSockets.remove(it)
                    logger.debugLog("Removing one - closed")

                }
            })
            proxyWebSockets.add(it)
            logger.debugLog("Added: one")

        }


    }

}

class MyExtensionSettings {
    val settingsPanelBuilder : SettingsPanelBuilder = SettingsPanelBuilder.settingsPanel()
        .withPersistence(SettingsPanelPersistence.PROJECT_SETTINGS)
        .withTitle("Retry Requests")
        .withDescription("Reload the extension for settings change to take place")
        .withKeywords("Retry")

    private val settingsManager = PanelSettingsDelegate(settingsPanelBuilder)

    val limitConcurrentRequestsSetting: Boolean by settingsManager.booleanSetting("Limit the number of concurrent HTTP requests?", false)
    val requestLimit: Int by settingsManager.integerSetting("Concurrent HTTP Request Limit", 10)



    val settingsPanel = settingsManager.buildSettingsPanel()


}
