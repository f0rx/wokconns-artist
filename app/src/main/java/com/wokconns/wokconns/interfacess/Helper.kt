package com.wokconns.wokconns.interfacess

import org.json.JSONObject

/**
 * Created by VARUN on 01/01/19.
 */
interface Helper {
    fun backResponse(flag: Boolean, msg: String?, response: JSONObject?)
}