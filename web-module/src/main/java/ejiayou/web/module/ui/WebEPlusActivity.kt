package ejiayou.web.module.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alipay.sdk.app.PayTask
import com.example.caller.BankABCCaller
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.unionpay.UPPayAssistEx
import ejiayou.common.module.bus.BusConstants
import ejiayou.common.module.signal.livebus.LiveBus
import ejiayou.common.module.ui.BarHelperConfig
import ejiayou.common.module.ui.BarOnBackListener
import ejiayou.common.module.utils.MMKVUtil
import ejiayou.common.module.utils.WxUtil
import ejiayou.web.export.model.JsBridgeDto
import ejiayou.web.module.base.BaseAppWebBindActivity
import ejiayou.web.module.web.jsbride.Callback
import ejiayou.web.module.web.jsbride.Handler
import ejiayou.web.export.router.WebRouterTable
import ejiayou.web.module.R
import ejiayou.web.module.databinding.WebMaskActivityBinding
import ejiayou.web.module.databinding.WebPlusActivityBinding
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * @author:
 * @created on: 2022/10/13 16:12
 * @description:
 */
@Route(path = WebRouterTable.PATH_WEB_EPLUS_PAY)
class WebEPlusActivity : BaseAppWebBindActivity<WebPlusActivityBinding>() {

    @Autowired
    @JvmField
    var webTitle: String? = null

    @Autowired
    @JvmField
    var webData: String? = null

    @Autowired
    @JvmField
    var webUrl: String? = null


    private val webViewContainer by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer)
    }
    private val webViewContainer2 by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer2)
    }

    override fun layoutRes(): Int {
        return R.layout.web_plus_activity
    }

    override fun injectTarget(): View? {
        return null
    }

    override fun addProgress(): ProgressBar {
        return binding.progress
    }

    override fun registerJsBridgeWeb() {
        super.registerJsBridgeWeb()
        //?????????eplus????????????
        initPayEplus()
    }

    /***
     * eplus ????????????
     */
    private fun initPayEplus() {
        /***
         * ????????? ???????????????
         */
        javascriptBridge?.register("jsAliPay", object : Handler {
            override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                println("WebViewJavascriptBridge ->  jsAliPay map = $map - json = $json ")
                val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                jsBridgeDto?.let {
                    println("WebViewJavascriptBridge ->  jsAliPay jsBridgeDto = $jsBridgeDto")
                    it.tn?.let { tn ->
                        aliPay(tn, callback)
                    }
                }
            }
        })

        /***
         * ????????? ???????????????
         */
        javascriptBridge?.register("jsUpPay", object : Handler {
            override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                println("WebViewJavascriptBridge ->  jsUpPay map = $map - json = $json ")
                val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                jsBridgeDto?.let {
                    println("WebViewJavascriptBridge ->  jsUpPay jsBridgeDto = $jsBridgeDto")
                    it.tn?.let { tn ->
                        upPayAssistEx(tn, callback)
                    }
                }
            }
        })
        /***
         * ????????? ????????????
         */
        javascriptBridge?.register("jsAbcPay", object : Handler {
            override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                println("WebViewJavascriptBridge ->  jsAbcPay map = $map - json = $json ")
                val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                jsBridgeDto?.let {
                    it.tn?.let { tn ->
                        println("WebViewJavascriptBridge ->  jsAbcPay jsBridgeDto = $jsBridgeDto")
                        abcPayWeb(tn, callback)
                    }
                }
            }
        })
        //?????????????????????
        javascriptBridge?.register("jsJumpMiniProgram", object : Handler {
            override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                println("WebViewJavascriptBridge ->  jsJumpMiniProgram map = $map - json = $json ")
                val jsBridgeDto = Gson().fromJson(json, JsBridgeDto::class.java)
                jsBridgeDto?.let {
                    if (!it.appId.isNullOrEmpty() && !it.path.isNullOrEmpty()) {
                        println("WebViewJavascriptBridge ->  jsJumpMiniProgram jsBridgeDto = $jsBridgeDto")
                        //?????????????????????????????????
                        MMKVUtil.encode("customScenes", 5)
                        WxUtil.openMiniProgram(
                            this@WebEPlusActivity, it.appId, it.path, it.miniprogramType, "miniProgram"
                        )
                    }
                }
            }
        })

        //??????????????????????????????
        LiveBus.get(BusConstants.PAY_UNITE_NOTICE_WEB_RESULT, String::class.java).observe(this) {
            println("WebViewJavascriptBridge ->  PAY_UNITE_NOTICE_WEB_RESULT  = $it")
            val result = HashMap<String, Any>()
            result["extMsg"] = it
            /***
             * ????????? ??????????????????
             */
            javascriptBridge?.call("jsMiniProgramResp", result, object : Callback {
                override fun call(map: HashMap<String, Any>?) {
                    println("WebViewJavascriptBridge ->  jsMiniProgramResp map = $map ")
                }
            })
        }
    }

    private fun abcPayWeb(tn: String, callback: Callback) {
        this.callback = callback
        this.runOnUiThread {
            barHelper.util().toolBarRoot(true)
            //false ????????? true ??????
            val bankABCAvaiable = BankABCCaller.isBankABCAvaiable(this)
            if (bankABCAvaiable) {
                val keyValue = tn.split("?", "=")
                BankABCCaller.startBankABC(this, "net.iusky.yijiayou",  "ejiayou.web.module.ui.WebEPlusActivity", "pay", keyValue[2])
            } else {
                mWebRoutine.loadUrl(tn)
                initWebViewType(currentRoutine)
                webViewContainer2.removeAllViews()
                webViewContainer2.addView(
                    mWebRoutine, LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                webViewContainer2.isVisible = true
                println("WebViewJavascriptBridge ->  abcPayWeb ")
            }
        }
    }


    /***
     * ???????????????
     */
    private fun aliPay(tn: String, callback: Callback) {
        val observable = Observable.create(ObservableOnSubscribe<String> { e ->
            val alipay = PayTask(this)
            // ???????????????????????????????????????
            val result = alipay.pay(tn, true)
            e?.let {
                it.onNext(result)
                it.onComplete()
            }
        })
        val observer = object : Observer<String> {
            override fun onSubscribe(d: Disposable?) {
                if (d != null) {
                    Logger.d("pay onSubscribe  ${d.isDisposed}")
                }
            }

            override fun onNext(value: String?) {
//                aliPayStatus(result, value)
            }

            override fun onError(e: Throwable?) {
                Logger.d("pay onError  ${e.toString()}")
            }

            override fun onComplete() {
                Logger.d("pay onComplete  ")
                sendWebPayResData(code = 1, message = "????????????", callback = callback)
            }

        }
        observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
    }

    //????????????
    private fun upPayAssistEx(tn: String, callback: Callback) {
        this.callback = callback
        tn.let {
            // ???00??? ??? ??????????????????   ???01??? ??? ??????????????????????????????????????????????????????
            UPPayAssistEx.startPay(this, null, null, it, "00")
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        //????????????????????????intent?????????
    }

    override fun onResume() {
        super.onResume()
        println("WebViewJavascriptBridge ->  onResume ")
        //????????????
        try {
            val param = intent.getStringExtra("from_bankabc_param")
            //TokenID=16659961778743231518&STT=9999&Msg=
            Logger.d("???????????? ?????????????????????$param")
            if (param == null) return
            callback?.let {
                Logger.d("???????????? ?????????????????????")
                sendWebPayResData(code = 1, message = "????????????", callback = it)
            }
//            val params = param.split("&")
//            for (i in params.indices) {
//                if (params[i].startsWith("STT")) {
//                    val stt = params[i].split("=")[1]
//                    //0000 ????????????
//                    if (stt == "0000") {
////                        toPaySuccess()
//                    } else {
//                        Logger.d("??????????????????")
//                    }
//                }
//            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.d("onActivityResult")
        Logger.d("???????????? ?????????????????????")
        data?.let {
            it.extras?.let { bundle ->
                //??????????????????
                val payResult = bundle.getString("pay_result")
                if (!payResult.isNullOrEmpty()) {
                    callback?.let {
                        sendWebPayResData(code = 1, message = "????????????", callback = it)
                    }
                }
                when (payResult) {
                    "success", "SUCCESS" -> {
                        Logger.d("pay ?????????????????????")
                    }
                    "fail", "FAIL" -> {
                        Logger.d("pay ?????????????????????")
                    }
                    "cancel", "CANCEL" -> {
                        Logger.d("pay ????????????????????????")

                    }
                }
            }
        }

    }

    private fun nativeToWeb() {
        val result = HashMap<String, Any>()
        result["message"] = "????????????"
        result["code"] = 1
        javascriptBridge?.call("jsRefreshOrderStatus", result, object : Callback {
            override fun call(map: HashMap<String, Any>?) {
                println("WebViewJavascriptBridge ->  jsRefreshOrderStatus map = $map ")
            }
        })
    }

    /***
     * app - web ??????
     */
    private fun sendWebPayResData(
        code: Int, message: String, data: Any? = null, callback: Callback
    ) {
        val result = HashMap<String, Any>()
        result["message"] = message
        result["code"] = code
        data?.let {
            result["data"] = data
        }
        callback.let {
            it.call(result)
        }
        nativeToWeb()
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        initWebViewType(currentJsBridge)
        webUrl?.let {
            println("WebViewJavascriptBridge ->  loadUrl ")
            mWebJsBridge.loadUrl(it)
            webViewContainer.addView(
                mWebJsBridge, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

    }

    override fun initBarHelperConfig(): BarHelperConfig? {
        return BarHelperConfig.builder().setBack(true)
                .setOnBackListener(object : BarOnBackListener {
                    override fun onBackClick() {
                        callback?.let {
                            println("WebViewJavascriptBridge ->  initBarHelperConfig goBack ")
                            barHelper.util().toolBarRoot(false)
                            webViewContainer2.isVisible = false
                            sendWebPayResData(code = 1, message = "????????????", callback = it)
                        }
                    }
                }).setTitle(title = "").setBgColor(R.color.white).build()
    }

    override fun webPageTitle(view: WebView?, url: String?) {
        super.webPageTitle(view, url)
        webTitle?.let {
            barHelper.util().setTitle(it)
        } ?: view?.let {
            val title = view.title ?: ""
            barHelper.util().setTitle(title)
        }
    }
}