package com.example.TVconnnect

import android.app.Activity
import android.content.Context
import android.content.Intent
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
// i think method can't /contain a garbage and the passed value would go away as soon as it is passed??
    suspend fun executeCommand(flow : Flow<JSONObject>, context : Context){
        flow.collect {
            val ss : String = it.getString("url")
            val uri : Uri = Uri.parse(ss)
            context.startActivity(Intent(Intent.ACTION_VIEW,uri))

        }
    }

}