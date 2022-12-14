package ejiayou.web.module.web

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.orhanobut.logger.Logger

open class BaseWebChromeClient : WebChromeClient() {


    /**
     * 网页控制台输入日志
     */
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        Logger.d("onConsoleMessage() -> ${consoleMessage.message()}")
        return super.onConsoleMessage(consoleMessage)
    }

    /**
     * 网页警告弹框
     */
    override fun onJsAlert(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(view.context)
            .setTitle("警告")
            .setMessage(message)
            .setPositiveButton("确认") { dialog, which ->
                dialog?.dismiss()
                result.confirm()
            }
            .setNegativeButton("取消") { dialog, which ->
                dialog?.dismiss()
                result.cancel()
            }
            .create()
            .show()
        return true
    }

    /**
     * 网页弹出确认弹窗
     */
    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        AlertDialog.Builder(view.context)
            .setTitle("警告")
            .setMessage(message)
            .setPositiveButton("确认") { dialog, which ->
                dialog?.dismiss()
                result.confirm()
            }
            .setNegativeButton("取消") { dialog, which ->
                dialog?.dismiss()
                result.cancel()
            }
            .create()
            .show()
        return true
    }
}