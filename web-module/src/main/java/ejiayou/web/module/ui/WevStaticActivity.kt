package ejiayou.web.module.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import ejiayou.common.module.ui.BarHelperConfig
import ejiayou.web.module.base.BaseAppWebBindActivity
import ejiayou.web.export.router.WebRouterTable
import ejiayou.web.module.R
import ejiayou.web.module.databinding.WebStaticActivityBinding

/**
 * @author:
 * @created on: 2022/8/25 16:06
 * @description:共用常用
 */
@Route(path = WebRouterTable.PATH_WEB_SHARED)
class WevStaticActivity : BaseAppWebBindActivity<WebStaticActivityBinding>() {

    @Autowired
    @JvmField
    var webTitle: String? = null

    @Autowired
    @JvmField
    var webData: String? = null

    @Autowired
    @JvmField
    var webUrl: String? = null

    override fun layoutRes(): Int {
        return R.layout.web_static_activity
    }

    override fun injectTarget(): View? { return null
    }

    private val webViewContainer by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer)
    }

    override fun addProgress(): ProgressBar {
        return binding.progress
    }


    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        initWebViewType(currentRoutine)
        webUrl = "file:///android_asset/index.html"
        webUrl?.let { mWebRoutine.loadUrl(it) }
        webViewContainer.addView(mWebRoutine, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }


    override fun initBarHelperConfig(): BarHelperConfig? {
        return BarHelperConfig.builder().setBack(true).setTitle(title = "").setBgColor(R.color.white).build()
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