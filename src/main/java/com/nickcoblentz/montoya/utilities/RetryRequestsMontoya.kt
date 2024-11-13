package com.nickcoblentz.montoya.utilities

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.proxy.websocket.*
import com.nickcoblentz.montoya.LogLevel
import com.nickcoblentz.montoya.MontoyaLogger
import com.nickcoblentz.montoya.settings.IntegerExtensionSetting
import com.nickcoblentz.montoya.settings.BooleanExtensionSetting
import com.nickcoblentz.montoya.settings.ExtensionSettingSaveLocation
import com.nickcoblentz.montoya.settings.GenericExtensionSettingsFormGenerator
import com.nickcoblentz.montoya.settings.ExtensionSettingsContextMenuProvider
import com.nickcoblentz.montoya.settings.ExtensionSettingsUnloadHandler
import de.milchreis.uibooster.model.FormBuilder
import de.milchreis.uibooster.model.Form
import java.util.LinkedList

class RetryRequestsMontoya : BurpExtension, ProxyWebSocketCreationHandler {
    private lateinit var api: MontoyaApi
    private lateinit var logger: MontoyaLogger
    private lateinit var myExecutor: MyExecutor

    private lateinit var limitConcurrentRequestsSetting: BooleanExtensionSetting
    private lateinit var concurrentRequestLimitSetting: IntegerExtensionSetting
    private val proxyWebSockets = mutableListOf<ProxyWebSocketCreation>()

    override fun initialize(api: MontoyaApi?) {
        this.api = requireNotNull(api) { "api : MontoyaApi is not allowed to be null" }
        logger = MontoyaLogger(api,LogLevel.DEBUG)

        logger.debugLog("Started loading the extension...")

        api.extension().setName("Retry Requests")

        myExecutor = MyExecutor(api)


        api.userInterface().registerContextMenuItemsProvider(RetryRequestsContextMenuProvider(api, myExecutor,proxyWebSockets))
        api.proxy().registerWebSocketCreationHandler(this)
        limitConcurrentRequestsSetting = BooleanExtensionSetting(
            api,
            "Limit the number of concurrent HTTP requests?",
            "RetryRequests.shouldLimit",
            false,
            ExtensionSettingSaveLocation.PROJECT
        )

        concurrentRequestLimitSetting = IntegerExtensionSetting(
            api,
            "Concurrent HTTP Request Limit",
            "RetryRequests.limit",
            10,
            ExtensionSettingSaveLocation.PROJECT
        )

        val extensionSetting = listOf(limitConcurrentRequestsSetting,concurrentRequestLimitSetting)
        val gen = GenericExtensionSettingsFormGenerator(extensionSetting, "Retry Requests Settings")
        val settingsFormBuilder: FormBuilder = gen.getSettingsFormBuilder()
        gen.addSaveCallback { formElement, form ->
            if(limitConcurrentRequestsSetting.currentValue)
                myExecutor.limitConcurrentRequests(concurrentRequestLimitSetting.currentValue)
            else
                myExecutor.removeConcurrentRequestLimit()
        }
        val settingsForm: Form = settingsFormBuilder.run()

        // Tell Burp we want a right mouse click context menu for accessing the settings
        api.userInterface().registerContextMenuItemsProvider(ExtensionSettingsContextMenuProvider(api, settingsForm))

        // When we unload this extension, include a callback that closes any Swing UI forms instead of just leaving them still open
        api.extension().registerUnloadingHandler(ExtensionSettingsUnloadHandler(settingsForm))

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