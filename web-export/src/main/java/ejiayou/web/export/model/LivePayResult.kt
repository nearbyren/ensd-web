package ejiayou.web.export.model

/**
 * @author:
 * @created on: 2022/9/24 14:19
 * @description: 统一支付返回结果通知处理查询
 */
data class LivePayResult(
    //支付场景  1.油站详情页   2.支付页 3.订单列表 4.订单详情
    var customScenes: Int = -1,
    //小程序
    var miniProgramJson: String? = null,
    // 1.支付宝 2.微信 3.云闪付 4.农行
    var payType: Int = -1
)

object LivePayConstants {
    //支付宝 1
    const val zfbPay = 1
    //微信支付 2
    const val wxPay = 2
    //银联支付 3
    const val yinLianPay = 3
    //农行支付 4
    const val abcPay = 4
}

object LivePayCustomScenes {

    //支付场景  1.油站详情页   2.支付页 3.订单列表 4.订单详情
    const val stationDetail = 1
    const val stationPay = 2
    const val stationOrderList = 3
    const val stationOrderDetail = 4
}
