package com.nickcoblentz.montoya.utilities

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
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

class RetryRequestsMontoya : BurpExtension {
    private lateinit var api: MontoyaApi
    private lateinit var logger: MontoyaLogger
    private lateinit var myExecutor: MyExecutor

    private lateinit var limitConcurrentRequestsSetting: BooleanExtensionSetting
    private lateinit var concurrentRequestLimitSetting: IntegerExtensionSetting

    override fun initialize(api: MontoyaApi?) {
        this.api = requireNotNull(api) { "api : MontoyaApi is not allowed to be null" }
        logger = MontoyaLogger(api,LogLevel.DEBUG)

        logger.debugLog("Started loading the extension...")

        api.extension().setName("Retry Requests")

        myExecutor = MyExecutor(api)


        api.userInterface().registerContextMenuItemsProvider(RetryRequestsContextMenuProvider(api, myExecutor))

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
}