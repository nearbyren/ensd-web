package ejiayou.web.module.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import ejiayou.common.module.bus.BusConstants
import ejiayou.common.module.signal.livebus.LiveBus
import ejiayou.common.module.ui.BarHelperConfig
import ejiayou.common.module.ui.BarOnBackListener
import ejiayou.common.module.utils.MMKVUtil
import ejiayou.web.export.model.LivePayResult
import ejiayou.web.export.router.WebRouterTable
import ejiayou.web.module.R
import ejiayou.web.module.base.BaseAppWebBindActivity
import ejiayou.web.module.databinding.WebOrderPayBinding

/**
 * @author:
 * @created on: 2022/7/18 20:27
 * @description:支付订单
 */
@Route(path = WebRouterTable.PATH_WEB_ORDER_PAY)
class WebOrderPayActivity : BaseAppWebBindActivity<WebOrderPayBinding>() {

    @Autowired
    @JvmField
    var webTitle: String? = null

    @Autowired
    @JvmField
    var webData: String? = null

    @Autowired
    @JvmField
    var webUrl: String? = null

    @Autowired
    @JvmField
    var customScenes: Int = -1

    @Autowired
    @JvmField
    var payType: Int = -1

    override fun layoutRes(): Int {
        return R.layout.web_order_pay
    }

    override fun injectTarget(): View? {
        return null
    }

    private val webViewContainer by lazy {
        findViewById<ViewGroup>(R.id.webViewContainer)
    }

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        initWebViewType(currentRoutine)
        webUrl?.let {
            webViewContainer.addView(
                mWebRoutine, LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            val params = mutableMapOf<String, String>()
            val adCode = MMKVUtil.decode("adCode", "")
            params["clientType"] = "2"
            params["sourceType"] = "1"
            params["customScence"] = "1"
            params["adCode"] = adCode.toString()
            mWebRoutine.loadUrl(it, params)
        }
    }

    override fun addProgress(): ProgressBar? {
        return binding.progress
    }

    override fun initBarHelperConfig(): BarHelperConfig? {
        return BarHelperConfig.builder().setBack(true)
            .setOnBackListener(object : BarOnBackListener {
                override fun onBackClick() {
                    LiveBus.get(BusConstants.PAY_UNITE_NOTICE_RESULT)
                        .post(LivePayResult(customScenes = customScenes, payType = payType))
                    finishPage(this@WebOrderPayActivity)
                }
            }).setTitle(title = "").setBgColor(R.color.white).build()
    }

    override fun webPageFinished(view: WebView?, url: String?) {
        super.webPageFinished(view, url)
        webTitle?.let {
            barHelper.util().setTitle(it)
        } ?: view?.let {
            val title = view.title ?: ""
            barHelper.util().setTitle(title)
        }

    }
}