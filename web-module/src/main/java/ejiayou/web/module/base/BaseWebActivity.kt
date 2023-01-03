package ejiayou.web.module.base

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.leon.channel.helper.ChannelReaderUtil
import com.orhanobut.logger.Logger
import ejiayou.common.module.ui.BaseActivityKot
import ejiayou.common.module.utils.*
import ejiayou.coupon.export.router.CouponServiceUtil
import ejiayou.login.export.router.LoginServiceUtil
import ejiayou.station.export.router.StationServiceUtil
import ejiayou.uikit.module.dpToPx
import ejiayou.web.export.model.JsBridgeData
import ejiayou.web.export.model.JsBridgeDto
import ejiayou.web.export.router.WebServiceUtil
import ejiayou.web.module.R
import ejiayou.web.module.dialog.WebCallMobileDialog
import ejiayou.web.module.web.jsbride.Callback
import ejiayou.web.module.web.jsbride.ConsolePipe
import ejiayou.web.module.web.jsbride.Handler
import ejiayou.web.module.web.jsbride.WebViewJavascriptBridge
import ejiayou.web.module.web.BaseWebChromeClient
import ejiayou.web.module.web.BaseWebViewClient
import ejiayou.web.module.web.WebViewPool
import java.lang.reflect.InvocationTargetException

/**
 * @author:
 * @created on: 2022/7/29 16:27
 * @description:
 */
abstract class BaseWebActivity : BaseActivityKot() {

    /***
     * 关于jsbridge注入失败问题
     * 需要在以下方法注册,提升注入成功率
     * onPageStarted
     * onPageCommitVisible
     * onProgressChanged
     */
    companion object {
        const val currentJsBridge = "JsBridge"
        const val currentRoutine = "Routine"
    }

    protected val mWebJsBridge by lazy {
        WebViewPool.getInstance().getWebView(this)
    }
    protected val mWebRoutine by lazy {
        WebViewPool.getInstance().getWebView(this)
    }

    open fun webPageFinished(view: WebView?, url: String?) {}

    open fun webPageTitle(view: WebView?, url: String?) {}

    //1.JsBridg 2.Routine
    var currentWebView: String = currentJsBridge

    open var javascriptBridge: WebViewJavascriptBridge? = null
    lateinit var settings: WebSettings
    var webProgress: ProgressBar? = null
    var callback: Callback? = null

    abstract fun addProgress(): ProgressBar?

    open fun initWebViewType(typeWebView: String) {
        currentWebView = typeWebView
        when (currentWebView) {
            currentJsBridge -> {
                setupJsBridge()
            }
            currentRoutine -> {
                setupRoutine()
            }
        }

    }

    override fun onBackPressed() {
        if (currentWebView == currentJsBridge) {
            if (mWebJsBridge.canGoBack()) {
                println("WebViewJavascriptBridge ->  onBackPressed mWebJsBridge canGoBack ")
                mWebJsBridge.goBack()
                return
            }
        } else if (currentWebView == currentRoutine) {
            if (mWebRoutine.canGoBack()) {
                println("WebViewJavascriptBridge ->  onBackPressed  mWebRoutine canGoBack ")
                mWebRoutine.goBack()
                return
            }
        }

        super.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            println("WebViewJavascriptBridge ->  onKeyDown KEYCODE_BACK ")
            if (currentWebView == currentJsBridge) {
                if (mWebJsBridge.canGoBack()) {
                    println("WebViewJavascriptBridge ->  onKeyDown mWebJsBridge canGoBack ")
                    mWebJsBridge.goBack()
                    return true
                }
            } else if (currentWebView == currentRoutine) {
                if (mWebRoutine.canGoBack()) {
                    println("WebViewJavascriptBridge ->  onKeyDown mWebRoutine canGoBack ")
                    mWebRoutine.goBack()
                    return true
                }
            }

        }
        return super.onKeyDown(keyCode, event)
    }

    override fun initialize(savedInstanceState: Bundle?) {
        webProgress = addProgress()
        AndroidBug5497Workaround.assistActivity(this)
        println("WebViewJavascriptBridge ->  initialize ")
        initWebViewType(currentWebView)
    }


    //Allow Cross Domain 跨域处理
    private fun setAllowUniversalAccessFromFileURLs(webView: WebView) {
        try {
            val clazz: Class<*> = webView.settings.javaClass
            val method = clazz.getMethod(
                "setAllowUniversalAccessFromFileURLs", Boolean::class.javaPrimitiveType
            )
            method.invoke(webView.settings, true)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    /***
     * 拨打电话
     */
    fun goCallCustomer(url: String) {
        try {
            val callDialog =
                WebCallMobileDialog(url)
            callDialog.setGravity(Gravity.BOTTOM)
            callDialog.setHeight(177.dpToPx)
            callDialog.setAnimationRes(R.style.lib_uikit_anim_InBottom_OutBottom)
            callDialog.show(activity = this@BaseWebActivity, "web_call")


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*******************************************普通WebView***********************************************/
    private fun setupRoutine() {
        mWebRoutine.let {
            println("WebViewJavascriptBridge ->  setupRoutine")
            it.webViewClient = jsRoutineWebViewClient()
            it.webChromeClient = jsRoutineWebChromeClient()
            setAllowUniversalAccessFromFileURLs(it)
        }
    }

    inner class jsRoutineWebViewClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webPageFinished(view, url)
            println("WebViewJavascriptBridge ->  Routine onPageFinished url = $url ")
            webProgress?.let {
                it.isVisible = false
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            //拨打电话
            if (url.startsWith(WebView.SCHEME_TEL)) {
                goCallCustomer(url)
                return true
            }
            //短信、邮箱
            if (url.startsWith("sms:") ||
                url.startsWith(WebView.SCHEME_MAILTO) ||
                url.startsWith("bankabc://") ||
                url.startsWith("bocom://") ||
                url.startsWith("tmast://") ||
                url.startsWith("weixin://")

            ) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                } catch (ignored: Exception) {
                }
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            println("WebViewJavascriptBridge ->  Routine onPageStarted injectJavascript")
            webProgress?.let {
                it.isVisible = true
            }

        }
    }

    inner class jsRoutineWebChromeClient : BaseWebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            println("WebViewJavascriptBridge ->  Routine onProgressChanged newProgress = $newProgress ")
            webProgress?.let {
                it.isVisible = newProgress != 100
                it.progress = newProgress
            }

        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            Logger.d("来了 $title")
            webPageTitle(view, title)
        }
    }

    /*******************************************普通WebView***********************************************/


    /*******************************************普通JsBridgeWebView***********************************************/

    open fun registerJsBridgeWeb() {}

    private fun setupJsBridge() {
        mWebJsBridge.let {
            println("WebViewJavascriptBridge ->  setupJsBridge")
            it.webViewClient = jsBridgeWebViewClient()
            it.webChromeClient = jsBridgeWebChromeClient()
            setAllowUniversalAccessFromFileURLs(it)
            javascriptBridge = WebViewJavascriptBridge(_context = this, _webView = it)
            javascriptBridge?.consolePipe = object : ConsolePipe {
                override fun post(string: String) {
                    println("WebViewJavascriptBridge ->  post -> string = $string ")
                }
            }
            /***
             * 描述： Web 页面配置
             */
            javascriptBridge?.register("jsWebConfiguration", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsWebConfiguration map = $map - json = $json ")
                    val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                    jsBridgeDto?.let {
                        println("WebViewJavascriptBridge ->  jsWebConfiguration jsBridgeDto = $jsBridgeDto")
                        statusBarUi(it.showNavigator, it.backColor, it.textColor)
                        sendWebResData(code = 1, message = "调用成功", callback = callback)
                    }
                }
            })
            /***
             * 描述： 获取用户位置信息
             */
            javascriptBridge?.register("jsLocationInfo", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsLocationInfo map = $map - json = $json ")
                    val adCode = MMKVUtil.decode("adCode", "440300")
                    val longitude = MMKVUtil.decode("longitude", "114.062827")
                    val latitude = MMKVUtil.decode("latitude", "22.54899")
                    val cityName = MMKVUtil.decode("cityName", "深圳市")
                    val jsBridgeData = JsBridgeData(
                        longitude = longitude.toString(),
                        latitude = latitude.toString(),
                        adCode = adCode.toString(),
                        cityName = cityName.toString(),
                    )
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        data = jsBridgeData,
                        callback = callback
                    )
                }
            })
            /***
             * 描述： 跳转指定原生页面
             */
            javascriptBridge?.register("jsJumpNative", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsJumpNative map = $map - json = $json ")
                    val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                    when (jsBridgeDto.pageName) {
                        "loginPage" -> {
                            LoginServiceUtil.navigateLoginPage()
                            finishPage(this@BaseWebActivity)
                        }
                        "stationListPage" -> {
                            AppManager.getInstance().finishAllActivity()

                        }
                        "stationDetailPage" -> {
                            jsBridgeDto.data?.stationId?.let {
                                StationServiceUtil.navigateStationDetailPage(this@BaseWebActivity, stationId = it)
                            }
                        }
                        "couponListPage" -> {
                            CouponServiceUtil.navigateCouponActivityPage(this@BaseWebActivity)
                        }
                    }
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        callback = callback
                    )
                }
            })

            /***
             * 描述： 跳转小程序
             */
            javascriptBridge?.register("jsJumpMiniProgram", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsJumpMiniProgram map = $map - json = $json ")
                    val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                    jsBridgeDto?.let { dao ->
                        WxUtil.openMiniProgram(this@BaseWebActivity, dao.appId, dao.path, dao.miniprogramType, "web")
                    }
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        callback = callback
                    )
                }
            })

            /***
             * 描述： 微信分享 url（小程序，微信好友，朋友圈）
             */
            javascriptBridge?.register("jsShare", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsShare map = $map - json = $json ")
                    val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                    val map = mutableMapOf<String, String>()

                    jsBridgeDto?.let { dao ->
                        map[WxUtil.SHARE_TITLE] = dao.shareTitle.toString()
                        map[WxUtil.SHARE_LOGO_URL] = dao.logoUrl.toString()
                        map[WxUtil.SHARE_TARGET_URL] = dao.shareUrl.toString()
                        map[WxUtil.SHARE_DESCRIPTION] = dao.shareContent.toString()
                        when (dao.shareType) {
                            "0" -> {
                                WxUtil.shareToWXFriends(this@BaseWebActivity, map, "shareToWXFriends")
                            }
                            "1" -> {
                                WxUtil.shareToWxMoments(this@BaseWebActivity, map, "shareToWxMoments")
                            }
                            "2" -> {
                            }
                            "3" -> {
                                map[WxUtil.SHARE_PATH] = jsBridgeDto.path.toString()
                                map[WxUtil.SHARE_APP_ID] = jsBridgeDto.appId.toString()
                                Thread {
                                    try {
                                        WxUtil.shareToMiniProgram(this@BaseWebActivity, map, transaction = "shareToMiniProgram")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }.start()
                            }
                            else -> {
                            }
                        }
                    }
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        callback = callback
                    )
                }
            })

            /***
             * 描述： 获取 APP 基础信息
             */
            javascriptBridge?.register("jsAppInfo", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsAppInfo map = $map - json = $json ")
                    val machineNo = ChannelReaderUtil.getChannel(this@BaseWebActivity) ?: "EJIAYOU"
                    val adCode = MMKVUtil.decode("adCode", "440300")
                    val longitude = MMKVUtil.decode("longitude", "114.062827")
                    val latitude = MMKVUtil.decode("latitude", "22.54899")
                    val cityName = MMKVUtil.decode("cityName", "深圳市")
                    val jsBridgeData = JsBridgeData(
                        deviceModel = machineNo,
                        osType = "2",
                        deviceOSVersion = android.os.Build.VERSION.CODENAME,
                        versionBuild = AppUtils.getVersionName(),
                        version = AppUtils.getVersionCode().toString(),
                        longitude = longitude.toString(),
                        latitude = latitude.toString(),
                        adCode = adCode.toString(),
                        cityName = cityName.toString(),
                    )
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        data = jsBridgeData,
                        callback = callback
                    )
                }
            })

            /***
             * 描述： 获取用户基础信息
             */
            javascriptBridge?.register("jsUserInfo", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsUserInfo map = $map - json = $json ")
                    val phone = MMKVUtil.decode("phone", "")
                    val token =
                        MMKVUtil.decode("session_key", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIzQzRDRkQ4MDE2REQ0NTAzNDA5QUIxNUU3NEQ5M0ZBRCIsImF1ZCI6IjIxMjI0MDY3NzgiLCJpc3MiOiJlamlheW91IiwibmFtZSI6InNzc3MiLCJleHAiOjE2NjY5NDkxNzcsImlhdCI6MTY2NjkyMDM3N30.Y6G4co5q9FaGAe2PQv1Fp0s8BhIIuNipA_sd2ZJDV-k")
                    val userId = MMKVUtil.decode("user_id", "0")
                    val adCode = MMKVUtil.decode("adCode", "440300")
                    val longitude = MMKVUtil.decode("longitude", "114.062827")
                    val latitude = MMKVUtil.decode("latitude", "22.54899")
                    val cityName = MMKVUtil.decode("cityName", "深圳市")
                    val jsBridgeData = JsBridgeData(
                        userPhone = phone.toString(),
                        userId = userId.toString(),
                        token = token.toString(),
                        longitude = longitude.toString(),
                        latitude = latitude.toString(),
                        adCode = adCode.toString(),
                        cityName = cityName.toString(),
                    )
                    sendWebResData(
                        code = 1,
                        message = "调用成功",
                        data = jsBridgeData,
                        callback = callback
                    )
                }
            })

            /***
             * 描述： 关闭当前 Web 页面
             */
            javascriptBridge?.register("jsCloseWeb", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsCloseWeb map = $map - json = $json ")
                    sendWebResData(code = 1, message = "调用成功", callback = callback)
                    finishPage(this@BaseWebActivity)
                }
            })
            /***
             * 描述： 跳转新的 Web 页面
             */
            javascriptBridge?.register("jsOpenWeb", object : Handler {
                override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                    println("WebViewJavascriptBridge ->  jsOpenWeb map = $map - json = $json ")
                    val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                    jsBridgeDto?.let { dao ->
                        sendWebResData(code = 1, message = "调用成功", callback = callback)
                        dao.url?.let { url ->
                            this@BaseWebActivity.runOnUiThread {
                                WebServiceUtil.navigateMaskPage(webUrl = url)
                            }
                        }

                    }
                    sendWebResData(code = 1, message = "调用成功", callback = callback)
                }
            })
            //注册  web -> native  native -> web 通信
            registerJsBridgeWeb()
        }
    }


    inner class jsBridgeWebViewClient : BaseWebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webPageFinished(view, url)
            println("WebViewJavascriptBridge ->  jsBridge 时机 onPageFinished url = $url ")
            webProgress?.let {
                it.isVisible = false
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            println("WebViewJavascriptBridge ->  jsBridge 时机 shouldOverrideUrlLoading injectJavascript")
            //拨打电话
            if (url.startsWith(WebView.SCHEME_TEL)) {
                goCallCustomer(url)
                return true
            }
            //短信、邮箱
            if (
                url.startsWith("sms:") ||
                url.startsWith(WebView.SCHEME_MAILTO) ||
                url.startsWith("bankabc://") ||
                url.startsWith("bocom://") ||
                url.startsWith("tmast://") ||
                url.startsWith("weixin://")
            ) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            println("WebViewJavascriptBridge ->  jsBridge 时机 onPageStarted injectJavascript")
            webProgress?.let {
                it.isVisible = true
            }
            println("WebViewJavascriptBridge ->  jsBridge 时机 执行 onPageStarted  ")
            javascriptBridge?.injectJavascript()
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
            println("WebViewJavascriptBridge ->  jsBridge 时机 onLoadResource injectJavascript")
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
            println("WebViewJavascriptBridge ->  jsBridge 时机 onPageCommitVisible injectJavascript")
            println("WebViewJavascriptBridge ->  jsBridge 时机 执行 onPageCommitVisible  ")
            javascriptBridge?.injectJavascript()
        }

    }

    inner class jsBridgeWebChromeClient : BaseWebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            println("WebViewJavascriptBridge ->  jsBridge 时机 jsBridgeWebChromeClient injectJavascript $newProgress")
            when (newProgress) {
                in 10..80 -> {
                    println("WebViewJavascriptBridge ->  jsBridge 时机 执行  $newProgress")
                    javascriptBridge?.injectJavascript()
                }
            }
            webProgress?.let {
                it.isVisible = newProgress != 100
                it.progress = newProgress
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            println("WebViewJavascriptBridge ->  jsBridge 时机 onReceivedTitle injectJavascript")
            Logger.d("来了 $title")
            webPageTitle(view, title)
        }
    }

    /***
     * app - web 数据
     */
    private fun sendWebResData(code: Int, message: String, data: Any? = null, callback: Callback) {
        val result = HashMap<String, Any>()
        result["message"] = message
        result["code"] = code
        data?.let {
            result["data"] = data
        }
        callback.call(result)
    }

    private fun statusBarUi(isVisible: Boolean = true, backgColor: String = "#FFFFFF", titleColor: String = "#8f000000") {
        this.runOnUiThread {
            barHelper.util().toolBarRoot(isVisible, backgColor, titleColor)
        }
    }

    /*******************************************普通JsBridgeWebView***********************************************/


    override fun showEmptyView(isShow: Boolean) {

    }

    override fun showLoadingView(isShow: Boolean) {
    }

}