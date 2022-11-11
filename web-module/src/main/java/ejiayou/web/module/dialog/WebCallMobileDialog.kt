package ejiayou.web.module.dialog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import ejiayou.uikit.module.dialog.BaseBindDialogFragment
import ejiayou.web.module.R
import ejiayou.web.module.databinding.WebCallMobileDialogBinding

/**
 * @author:
 * @created on: 2022/7/14 14:01
 * @description: 拨打手机号
 */
class WebCallMobileDialog(var url: String) : BaseBindDialogFragment<WebCallMobileDialogBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.web_call_mobile_dialog
    }


    override fun initialize(view: View, savedInstanceState: Bundle?) {
        isCancelable = false
        binding.webIvClose.setOnClickListener {
            dismiss()
        }
        binding.webBtnCancel.setOnClickListener {
            dismiss()
        }
        binding.webBtnCall.setOnClickListener {
            dismiss()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

}