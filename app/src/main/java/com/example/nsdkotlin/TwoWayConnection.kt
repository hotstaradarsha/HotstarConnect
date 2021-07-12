package com.example.nsdkotlin

import kotlinx.coroutines.flow.*
import org.json.JSONObject

class TwoWayConnection internal constructor(internal var connection : Connection, internal var flow : Flow<JSONObject>  ) {

    fun isConnected():Int{
        return connection.isConnected()
    }

    fun getFlow():Flow<JSONObject>{
        return flow
    }

    suspend fun sendJson(  json: JSONObject):Int{
        var ret = connection.sendJson(json)
        return ret
    }


}