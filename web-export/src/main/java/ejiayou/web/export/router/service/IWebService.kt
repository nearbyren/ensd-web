package ejiayou.web.export.router.service

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.template.IProvider
import ejiayou.web.export.router.WebRouterTable

/**
 * @author: lr
 * @created on: 2022/7/16 4:03 下午
 * @description: 对外暴露服务功能
 */
@Route(path = WebRouterTable.PATH_WEB_SERVICE)
interface IWebService : IProvider {

    override fun init(context: Context?)

}