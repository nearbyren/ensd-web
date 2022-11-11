
一、编译范围
private fun initWebViewPool() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val packageName = this.packageName
        val processName = ProcessUtils.getCurrentProcessName()
        if (packageName != processName) {
            WebView.setDataDirectorySuffix(packageName)
    }
}
// 用广播提前拉起 :web进程
val intent = Intent(this, WebViewInitBoastcast::class.java)
    sendBroadcast(intent)
}

class WebViewInitBoastcast: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        LogUtils.d("WebViewInitBoastcast", "initWebViewPool")
        initWebViewPool(context)
    }

    private fun initWebViewPool(context: Context) {
        // 根据手机 CPU 核心数（或者手机内存）设置缓存池容量
        WebViewPool.getInstance().setMaxPoolSize(min(Runtime.getRuntime().availableProcessors(), 3))
        WebViewPool.getInstance().init(context)

        // 加载本地模板用的 WebView 复用池
        TemplateWebViewPool.getInstance().setMaxPoolSize(min(Runtime.getRuntime().availableProcessors(), 3))
        TemplateWebViewPool.getInstance().init(context)
    }
}


        <receiver android:name=".broadcast.WebViewInitBoastcast"
            android:process=":web"/>
        <activity
            android:name=".activity.WebActivity"
            android:exported="false"
            android:process=":web"/>
