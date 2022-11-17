package ejiayou.web.module.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.orhanobut.logger.Logger
import ejiayou.common.module.ui.BarHelperConfig
import ejiayou.common.module.ui.BarOnBackListener
import ejiayou.web.module.base.BaseAppWebBindActivity
import ejiayou.web.export.router.WebRouterTable
import ejiayou.web.module.R
import ejiayou.web.module.databinding.WebMaskActivityBinding

/**
 * @author:
 * @created on: 2022/10/13 16:12
 * @description:
 */
@Route(path = WebRouterTable.PATH_WEB_MASK)
class WebMaskActivity : BaseAppWebBindActivity<WebMaskActivityBinding>() {

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
        return R.layout.web_mask_activity
    }

    override fun injectTarget(): View? {
        return null
    }

    override fun addProgress(): ProgressBar {
        return binding.progress
    }

    override fun registerJsBridgeWeb() {
        super.registerJsBridgeWeb()

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        //设置否则无法获取intent的数据
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
                        finishPage(this)
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