package ejiayou.web.export.model


/**
 * @author:
 * @created on: 2022/10/13 16:46
 * @description:
 */
data class JsBridgeDto(

//    http://office.ejiayou.com/ejy-web-docs/#/native/bridge?id=h5-call-native

    //jsAppInfo       获取 APP 基础信息   包含【2】
    //jsUserInfo      获取用户基础信息    包含【2】
    //jsLocationInfo 获取用户位置信息     包含【2】
    //【1】
    var data: JsBridgeData? = null,

    // 【2】
    var msg: String? = null,
    var code: Int = 0,

    //jsJumpNative  跳转指定原生页面 包含【1】
    var pageName: String? = null,// // 跳转页面名称，loginPage: 登录页  .....


    //jsJumpMiniProgram 跳转小程序
    //【3】
    var appId: String? = null,// 小程序username
    var path: String? = null,// 小程序页面的路径
    var miniprogramType: Int = 0,// 小程序的版本, 0； 正式，1：开发，2：体验


    //jsOpenWeb 跳转新的 Web 页面
    var url: String? = null,


    //jsWebConfiguration  Web 页面配置
    var backColor: String = "#FFFFFF", // 导航栏背景色
    var textColor: String = "#FFFFFF",   // 导航栏标题颜色
    var showNavigator: Boolean = false,   // 当前web页是否显示导航栏


    //jsShare 微信分享 url（小程序，微信好友，朋友圈） 包含【3】
    var shareType: String? = null,// 分享的类型，0: 好友分享， 1：朋友圈分享，3：微信小程序分享。多个类型使用##连接， 包含小程序时只有小程序分享
    var shareUrl: String? = null, // 分享的链接
    var logoUrl: String? = null,// 分享展示logo
    var shareTitle: String? = null,
    var shareContent: String? = null,

    //jsAliPay 支付宝支付 云闪付支付 农行支付
    var tn: String? = null


)
