package com.example.TVconnnect

import android.net.Uri
import com.example.nsdkotlin.Connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import org.json.JSONObject

class TvConnect {

    suspend fun openPage(url: String, con: Connection): Int {
        var json = JSONObject()
        json.put("action", 1)
        json.put("url", url)
        return con.sendJson(json)

    }

    suspend fun executeCommand(flow : Flow<JSONObject>){
        flow.collect {
            val ss : String = it.getString("url")
            val uri : Uri = Uri.parse(ss)

        }
    }

}