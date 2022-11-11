package ejiayou.web.export.model

/**
 * @author:
 * @created on: 2022/10/13 16:51
 * @description:
 */
data class JsBridgeData(


    //获取用户位置信息
    // 【2】
    var longitude: String? = null,// 经度
    var latitude: String? = null,// 纬度
    var adCode: String? = null,// 城市code
    var cityName: String? = null,// 城市名称

    //获取用户基础信息 包含 【2】
    var userPhone: String? = null,
    var userId: String? = null,
    var token: String? = null,

    // 获取 APP 基础信息   包含 【2】
    var deviceModel: String? = null,
    var osType: String? = null,
    var deviceOSVersion: String? = null,
    var versionBuild: String? = null,
    var version: String? = null,


    //油站id
    var stationId: String? = null,


    )
