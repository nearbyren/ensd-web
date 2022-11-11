package ejiayou.web

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import ejiayou.common.module.api.response.CorHttp
import timber.log.Timber

/**
 * @author:
 * @created on: 2022/7/11 16:28
 * @description:
 */
class WebApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CorHttp.getInstance().init(this)
        Timber.plant(Timber.DebugTree())
        val formatStrategy: FormatStrategy = PrettyFormatStrategy
            .newBuilder()
            .showThreadInfo(true) //（可选）是否显示线程信息。 默认值为true
            //                .methodCount(5)// （可选）要显示的方法行数。 默认2
            //                .methodOffset(7) // （可选）设置调用堆栈的函数偏移值，0的话则从打印该Log的函数开始输出堆栈信息，默认是0
            //                .logStrategy() //（可选）更改要打印的日志策略。 默认LogCat
            .tag("web") //（可选）每个日志的全局标记。
            .build()
        Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return true
            }
        })
        ARouter.openLog()
        ARouter.openDebug()
        ARouter.init(this)
    }
}