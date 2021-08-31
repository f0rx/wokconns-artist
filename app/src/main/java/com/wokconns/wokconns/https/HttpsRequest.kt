package com.wokconns.wokconns.https

import android.content.Context
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.wokconns.wokconns.interfacess.Const
import com.wokconns.wokconns.interfacess.Helper
import com.wokconns.wokconns.jsonparser.JSONParser
import com.wokconns.wokconns.preferences.SharedPrefs
import com.wokconns.wokconns.utils.ProjectUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class HttpsRequest {
    private var match: String
    private var params: HashMap<String, String?>? = null
    private var fileparams: HashMap<String, File?>? = null
    private var ctx: Context
    private var jObject: JSONObject? = null
    private var sharedPreference: SharedPrefs? = null

    constructor(match: String, params: HashMap<String, String?>?, ctx: Context) {
        this.match = match
        this.params = params
        this.ctx = ctx
        sharedPreference = SharedPrefs.getInstance(ctx)
    }

    constructor(
        match: String,
        params: HashMap<String, String?>?,
        fileparams: HashMap<String, File?>?,
        ctx: Context
    ) {
        this.match = match
        this.params = params
        this.fileparams = fileparams
        this.ctx = ctx
        sharedPreference = SharedPrefs.getInstance(ctx)
    }

    constructor(match: String, ctx: Context) {
        this.match = match
        this.ctx = ctx
        sharedPreference = SharedPrefs.getInstance(ctx)
    }

    constructor(match: String, jObject: JSONObject?, ctx: Context) {
        this.match = match
        this.jObject = jObject
        this.ctx = ctx
        sharedPreference = SharedPrefs.getInstance(ctx)
    }

    fun stringPostJson(TAG: String?, h: Helper) {
        if (params != null) if (!params!!.containsKey(Const.ROLE)) params!![Const.ROLE] = "1"
        AndroidNetworking.post(Const.BASE_URL + match)
            .addJSONObjectBody(jObject)
            .setTag("test")
            .addHeaders(Const.LANGUAGE, "en")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e(TAG, " response body --->$response")
                    Log.e(TAG, " response body --->" + jObject.toString())
                    val jsonParser = JSONParser(ctx, response)
                    var status: Any? = ""
                    if (jsonParser.jObj.has("status")) status = jsonParser.jObj.opt("status")
                    var message: Any = ""
                    if (jsonParser.jObj.has("message")) message =
                        jsonParser.jObj.optString("message")
                    if (status is String && status.contains("err")
                        || status == "error"
                    ) {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else if (status == ERROR_INT || status == "0") {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else {
                        h.backResponse(jsonParser.RESULT, jsonParser.MESSAGE, response)
                    }
                }

                override fun onError(anError: ANError) {
                    ProjectUtils.pauseProgressDialog()
                    try {
                        if (anError.errorBody != null) {
                            val jsonObject = JSONObject(anError.errorBody)
                            val status = jsonObject.opt("status")
                            val message: Any = jsonObject.optString("message")
                            if (status is String && status.contains("err")
                                || status == "error"
                            ) h.backResponse(false, message.toString(), jsonObject)
                        }
                        val msg =
                            if (anError.errorDetail != null) anError.errorDetail else anError.cause!!.localizedMessage
                        h.backResponse(false, msg, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    Log.e(
                        TAG,
                        " error body --->" + anError.errorBody + " error msg --->" + anError.message
                    )
                }
            })
    }

    fun stringPost(TAG: String?, h: Helper) {
        if (params != null) if (!params!!.containsKey(Const.ROLE)) params!![Const.ROLE] = "1"
        AndroidNetworking.post(Const.BASE_URL + match)
            .addBodyParameter(params)
            .setTag("test")
            .addHeaders(Const.LANGUAGE, "en")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e(TAG, " response body --->$response")
                    Log.e(TAG, " param --->" + params.toString())
                    val jsonParser = JSONParser(ctx, response)
                    var status: Any? = ""
                    if (jsonParser.jObj.has("status")) status = jsonParser.jObj.opt("status")
                    var message: Any = ""
                    if (jsonParser.jObj.has("message")) message =
                        jsonParser.jObj.optString("message")
                    if (status is String && status.contains("err")
                        || status == "error"
                    ) {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else if (status == ERROR_INT || status == "0") {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else {
                        h.backResponse(jsonParser.RESULT, jsonParser.MESSAGE, response)
                    }
                }

                override fun onError(anError: ANError) {
                    ProjectUtils.pauseProgressDialog()
                    try {
                        if (anError.errorBody != null) {
                            val jsonObject = JSONObject(anError.errorBody)
                            val status = jsonObject.opt("status")
                            val message: Any = jsonObject.optString("message")
                            if (status is String && status.contains("err")
                                || status == "error"
                            ) h.backResponse(false, message.toString(), jsonObject)
                        }
                        val msg =
                            if (anError.errorDetail != null) anError.errorDetail else anError.cause!!.localizedMessage
                        h.backResponse(false, msg, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    Log.e(
                        TAG,
                        " error body --->" + anError.errorBody + " error msg --->" + anError.message
                    )
                }
            })
    }

    fun stringGet(TAG: String?, h: Helper) {
        if (params != null) if (!params!!.containsKey(Const.ROLE)) params!![Const.ROLE] = "1"
        AndroidNetworking.get(Const.BASE_URL + match)
            .setTag("test")
            .addHeaders(Const.LANGUAGE, "en")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e(TAG, " response body --->$response")
                    val jsonParser = JSONParser(ctx, response)
                    if (jsonParser.RESULT) {
                        h.backResponse(true, jsonParser.MESSAGE, response)
                    } else {
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    }
                }

                override fun onError(anError: ANError) {
                    ProjectUtils.pauseProgressDialog()
                    Log.e(
                        TAG,
                        " error body --->" + anError.errorBody + " error msg --->" + anError.message
                    )
                }
            })
    }

    fun imagePost(TAG: String?, h: Helper) {
        if (params != null) if (!params!!.containsKey(Const.ROLE)) params!![Const.ROLE] = "1"
        AndroidNetworking.upload(Const.BASE_URL + match)
            .addMultipartFile(fileparams)
            .addMultipartParameter(params)
            .addHeaders(Const.LANGUAGE, "en")
            .setTag("uploadTest")
            .setPriority(Priority.IMMEDIATE)
            .build()
            .setUploadProgressListener { bytesUploaded: Long, totalBytes: Long ->
                Log.e(
                    "Byte",
                    "$bytesUploaded  !!! $totalBytes"
                )
            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e(TAG, " response body --->$response")
                    Log.e(TAG, " param --->" + params.toString())
                    val jsonParser = JSONParser(ctx, response)
                    var status: Any? = ""
                    if (jsonParser.jObj.has("status")) status = jsonParser.jObj.opt("status")
                    var message: Any = ""
                    if (jsonParser.jObj.has("message")) message =
                        jsonParser.jObj.optString("message")
                    if (status is String && status.contains("err")
                        || status == "error"
                    ) {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else if (status == ERROR_INT || status == "0") {
                        ProjectUtils.showLong(ctx, message.toString())
                        h.backResponse(false, jsonParser.MESSAGE, null)
                    } else {
                        h.backResponse(jsonParser.RESULT, jsonParser.MESSAGE, response)
                    }
                }

                override fun onError(anError: ANError) {
                    ProjectUtils.pauseProgressDialog()
                    try {
                        if (anError.errorBody != null) {
                            val jsonObject = JSONObject(anError.errorBody)
                            val status = jsonObject.opt("status")
                            val message: Any = jsonObject.optString("message")
                            if (status is String && status.contains("err")
                                || status == "error"
                            ) h.backResponse(false, message.toString(), jsonObject)
                        }
                        val msg =
                            if (anError.errorDetail != null) anError.errorDetail else anError.cause!!.localizedMessage
                        h.backResponse(false, msg, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    Log.e(
                        TAG,
                        " error body --->" + anError.errorBody + " error msg --->" + anError.message
                    )
                }
            })
    }

    companion object {
        const val ERROR_INT = 0
    }
}