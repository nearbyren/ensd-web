package ejiayou.web.export.router

import com.alibaba.android.arouter.launcher.ARouter
import ejiayou.web.export.router.service.IWebService

/**
 * @author: lr
 * @created on: 2022/7/16 4:26 下午
 * @description: 提供启动activity  service 等动作
 */
open class WebServiceUtil {


    companion object {

        fun initService(): IWebService? {
            var service = ARouter.getInstance().build(WebRouterTable.PATH_WEB_SERVICE).navigation()
            if (service is IWebService) return service
            return null
        }


        fun navigateEPlusPayPage(webTitle: String? = null, webUrl: String? = null, webData: String? = null) {
            val postcard = ARouter.getInstance().build(WebRouterTable.PATH_WEB_EPLUS_PAY)
            webTitle?.let {
                postcard.withString("webTitle", webTitle)
            }
            webUrl?.let {
                postcard.withString("webUrl", webUrl)
            }
            webData?.let {
                postcard.withString("webData", webData)
            }
            postcard.navigation()

        }

        fun navigateOrderPayPage(customScenes: Int = -1, payType: Int = -1, webTitle: String? = null, webUrl: String? = null, webData: String? = null) {
            val postcard = ARouter.getInstance().build(WebRouterTable.PATH_WEB_ORDER_PAY)
            customScenes.let {
                postcard.withInt("customScenes", customScenes)
            }
            payType.let {
                postcard.withInt("payType", payType)
            }
            webTitle?.let {
                postcard.withString("webTitle", webTitle)
            }
            webUrl?.let {
                postcard.withString("webUrl", webUrl)
            }
            webData?.let {
                postcard.withString("webData", webData)
            }
            postcard.navigation()
        }

        fun navigateStaticPage(webTitle: String? = null, webUrl: String? = null, webData: String? = null) {
            val postcard = ARouter.getInstance().build(WebRouterTable.PATH_WEB_SHARED)
            webTitle?.let {
                postcard.withString("webTitle", webTitle)
            }
            webUrl?.let {
                postcard.withString("webUrl", webUrl)
            }
            webData?.let {
                postcard.withString("webData", webData)
            }
            postcard.navigation()

        }

        fun navigateMaskPage(webTitle: String? = null, webUrl: String? = null, webData: String? = null) {
            val postcard = ARouter.getInstance().build(WebRouterTable.PATH_WEB_MASK)
            webTitle?.let {
                postcard.withString("webTitle", webTitle)
            }
            webUrl?.let {
                postcard.withString("webUrl", webUrl)
            }
            webData?.let {
                postcard.withString("webData", webData)
            }
            postcard.navigation()

        }
    }
}