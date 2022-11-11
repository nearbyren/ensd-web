package ejiayou.web.module.web.jsbride

import ejiayou.web.module.web.jsbride.Callback

interface Handler {
    fun handler(map: HashMap<String, Any>?, json: String, callback: Callback)
}