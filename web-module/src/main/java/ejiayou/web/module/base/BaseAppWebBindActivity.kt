package ejiayou.web.module.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import ejiayou.common.module.dialog.LoadingDialog
import ejiayou.common.module.utils.SystemUIUtils
import ejiayou.web.module.mvvm.BaseBindWebActivity

/**
 * @author: lr
 * @created on: 2022/7/10 11:13 上午
 * @description:
 */

abstract class BaseAppWebBindActivity<B : ViewDataBinding> : BaseBindWebActivity<B>() {

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUIUtils.transparentStatusBar(this)
    }

    override fun showLoadingView(isShow: Boolean) {
        try {
            if (isShow) {
                if (!isFinishing && !loadingDialog.isAdded) {
                    loadingDialog.show(this)
                }
            } else {
                if (!isFinishing) {

                    loadingDialog.dismissAllowingStateLoss()
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun showEmptyView(isShow: Boolean) {

    }

    override fun showContentView(isShow: Boolean) {

    }

    override fun showNetworkView(isShow: Boolean) {

    }

}