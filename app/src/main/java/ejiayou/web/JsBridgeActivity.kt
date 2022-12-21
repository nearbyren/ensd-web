package ejiayou.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.Logger
import ejiayou.web.module.ui.WebMaskActivity
import ejiayou.web.module.ui.WevStaticActivity
import ejiayou.web.module.web.jsbride.Callback
import ejiayou.web.module.web.jsbride.ConsolePipe
import ejiayou.web.module.web.jsbride.Handler
import ejiayou.web.module.web.jsbride.WebViewJavascriptBridge
import java.lang.reflect.InvocationTargetException
import java.net.URISyntaxException

class JsBridgeActivity : AppCompatActivity(), View.OnClickListener {
    private var mWebView: WebView? = null
    private var mWebUrl: String? = null
    private var bridge: WebViewJavascriptBridge? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webUrl = intent.getStringExtra("webUrl")
        webUrl?.let {
            mWebUrl = it
        }
        setContentView(R.layout.test_js_bridge)
        setupView()
    }

    open class MyChromeWebClient : WebChromeClient() {
        // For Android 3.0-
        fun openFileChooser(uploadMsg: ValueCallback<Uri>) {

//            mUploadMessage = uploadMsg;
            val i = Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
//          startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

        // For Android 3.0+
        fun openFileChooser(uploadMsg: Any, acceptType: String) {
//            mUploadMessage = uploadMsg;
            val i = Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
//            startActivityForResult(Intent.createChooser(i, "File Browser"),FILECHOOSER_RESULTCODE);
        }

        //For Android 4.1
        fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
//            mUploadMessage = uploadMsg;
            val i = Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
//        startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }

        // For Android 5.0+会调用此方法
        //针对 Android 5.0+
        override fun onShowFileChooser(
            webView: WebView, valueCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
//            mUploadCallbackAboveL = filePathCallback;
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
//            startActivityForResult(Intent.createChooser(i, "File Browser"),FILECHOOSER_RESULTCODE);
            return true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupView() {
        val appWeb = findViewById<Button>(R.id.app_web)
        mWebView = findViewById(R.id.webView)
        setAllowUniversalAccessFromFileURLs(mWebView!!)
        appWeb.setOnClickListener(this)
        bridge = WebViewJavascriptBridge(_context = this, _webView = mWebView)
        mWebView!!.webChromeClient = MyChromeWebClient()
        mWebView!!.settings.userAgentString = mWebView!!.settings.userAgentString + ";ensd"
        WebView.setWebContentsDebuggingEnabled(true)
        bridge?.consolePipe = object : ConsolePipe {
            override fun post(string: String) {
                println("Javascript ->  consolePipe Next line is javascript console.log->>>")
                println(string)
            }
        }

        bridge?.register("testObjcCallback", object : Handler {
            override fun handler(map: HashMap<String, Any>?, json: String, callback: Callback) {
                println("Javascript ->  Handler  Next line is javascript data->>>")
                println("Javascript ->  Handler  ${map}")
                val data = java.util.HashMap<String, Any>()
                data["AndroidKey00"] = "web data -> app ->web data "
                callback.call(data)
            }

        })
        mWebView!!.webViewClient = webClient
        // Loading html in local ，This way maybe meet cross domain. So You should not forget to set
        // /*...setAllowUniversalAccessFromFileURLs... */
        // If you loading remote web server,That can be ignored.
//        mWebView!!.loadUrl("file:///android_asset/Demo.html")
//        mWebView!!.loadUrl("http://172.18.5.161:8080/home")
//        mWebUrl?.let { mWebView!!.loadUrl(it) }

//      index.html use SDBridge.js. This js file was create by webpack.
//        mWebView!!.loadUrl("file:///android_asset/index.html")
        mWebView!!.loadUrl("file:///android_asset/scheme.html")

    }

    private fun gotoOtherAppBySchemeProtocol(url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //通过直接处理抛出的ActivityNotFound异常来确保程序不会崩溃
            e.printStackTrace()
        }
    }

    private fun gotoOtherAppByIntentProtocol(url: Uri) {
        val stringUrl = url.toString()
        val fallbackUrl: String = if (stringUrl.contains("S.browser_fallback_url")) {
            stringUrl.substring(stringUrl.indexOf("S.browser_fallback_url"), stringUrl.indexOf(";end"))
        } else {
            ""
        }
        Logger.d("启动参数 fallbackUrl $fallbackUrl")

        try {
            val intent = Intent.parseUri(url.toString(), Intent.URI_INTENT_SCHEME)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: ActivityNotFoundException) {
            //通过直接处理抛出的ActivityNotFound异常来确保程序不会崩溃
            e.printStackTrace()
        }
    }

    private val webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            println("shouldOverrideUrlLoading")
            val url = request?.url
            val scheme = url?.scheme
            val host = url?.host
            when (scheme) {
                "https" -> {
                    //仅过滤某些host进行判断是否跳转，也可不过滤
                    if ("ejiayou.com" == host) {
                        gotoOtherAppBySchemeProtocol(url)
                        return true
                    }
                }
                "module" -> {
                    gotoOtherAppBySchemeProtocol(url)
                    return true
                }
                "intent" -> {
                    gotoOtherAppByIntentProtocol(url)
                    return true
                }
                else -> {
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            println("onPageStarted")
            bridge?.injectJavascript()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            println("onPageFinished")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.app_web -> {
                val data = java.util.HashMap<String, Any>()
                data["AndroidKey00"] = "AndroidValue00"
                //call js Sync function
//                bridge?.call("GetToken", data, object : Callback {
//                    override fun call(map: HashMap<String, Any>?) {
//                        println("Next line is javascript data->>>")
//                        println(map)
//                    }
//                })

                bridge?.call("testJavascriptHandler", data, object : Callback {
                    override fun call(map: HashMap<String, Any>?) {
                        println("Javascript ->  call Next line is javascript data->>>")
                        println("Javascript ->  call $map")
                    }
                })

            }

        }
    }

    //Allow Cross Domain
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
}