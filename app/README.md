//1、URL Schemes

H5
<a href="module://ejiayou.com/path?param=1">URL Schemes path </a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="https://ejiayou.com/?param=1">URL Schemes not path</a>


AndroidManifest.xml 配置

 <intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    
    <action android:name="com.ejiayou.com.action" />

    <data
        android:host="www.ejiayou.com"
        android:scheme="https" />

    <data
        android:host="ejiayou.com"
        android:scheme="module" />

    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />

</intent-filter>

Android 取参数

//1、URL Schemes
intent.data?.let {
    Logger.d("启动参数 URL Schemes  = ${it.scheme} - host = ${it.host} - path = ${it.path} - query = ${it.getQueryParameter("param")}")
}

//2、Intent


H5

<!--
intent://[host]#Intent;package=[String];action=[String];category=[String];component=[String];scheme=[String];S.key=[String];i.key=[Int];end;

intent:
   HOST/URI-path // Optional host
   #Intent;
      package=[string];
      action=[string];
      category=[string];
      component=[string];
      scheme=[string];
   end;

    String => 'S'
    Boolean =>'B'
    Byte => 'b'
    Character => 'c'
    Double => 'd'
    Float => 'f'
    Integer => 'i'
    Long => 'l'
    Short => 's'
-->
<a href="intent://www.ejiayou.com#Intent;package=ejiayou.login;category=android.intent.category.BROWSABLE;scheme=https;S.browser_fallback_url=https://www.ejiayou.com;i.v=10000;end;">Intent打开</a>
&nbsp;&nbsp;&nbsp;&nbsp;
<a href="intent://ejiayou.com#Intent;package=ejiayou.login;category=android.intent.category.BROWSABLE;scheme=module;S.browser_fallback_url=https://www.ejiayou.com;i.v=10000;end;">Intent打开</a>


AndroidManifest.xml 配置

 <intent-filter>
    <action android:name="android.intent.action.VIEW" />

    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    
    <action android:name="com.ejiayou.com.action" />

    <data
        android:host="www.ejiayou.com"
        android:scheme="https" />

    <data
        android:host="ejiayou.com"
        android:scheme="module" />

    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />

</intent-filter>


Android 取参数

//2、Intent
intent?.let {
Logger.d("启动参数 Intent 获取参数 " +
    "${it.getStringExtra("browser_fallback_url")} - " +
    "i = ${it.getIntExtra("v", 10086)}")
}

webViewClient  实现方法


override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    println("shouldOverrideUrlLoading")
    val url = request?.url
    val scheme = url?.scheme
    val host = url?.host
    when (scheme) {
        "https" -> {
        //仅过滤某些host进行判断是否跳转，也可不过滤
        if ("ejiayou.com" == host) {
            gotoOtherAppBySchemeProtocol(url)
            return true
            }
        }
        "module" -> {
            gotoOtherAppBySchemeProtocol(url)
            return true
        }
        "intent" -> {
            gotoOtherAppByIntentProtocol(url)
            return true
        }
        else -> {
    }
}
    return super.shouldOverrideUrlLoading(view, request)
}




    private fun gotoOtherAppBySchemeProtocol(url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            //通过直接处理抛出的ActivityNotFound异常来确保程序不会崩溃
            e.printStackTrace()
        }
    }

    private fun gotoOtherAppByIntentProtocol(url: Uri) {
        val stringUrl = url.toString()
        val fallbackUrl: String = if (stringUrl.contains("S.browser_fallback_url")) {
            stringUrl.substring(stringUrl.indexOf("S.browser_fallback_url"), stringUrl.indexOf(";end"))
        } else {
            ""
        }
        Logger.d("启动参数 fallbackUrl $fallbackUrl")

        try {
            val intent = Intent.parseUri(url.toString(), Intent.URI_INTENT_SCHEME)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(intent)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: ActivityNotFoundException) {
            //通过直接处理抛出的ActivityNotFound异常来确保程序不会崩溃
            e.printStackTrace()
        }
    }