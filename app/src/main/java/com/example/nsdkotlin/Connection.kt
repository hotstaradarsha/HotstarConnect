package com.example.nsdkotlin

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import java.net.InetAddress

class Connection internal constructor( internal val service : NsdServiceInfo, internal var connected : Int =0){
//allowed to check , out of the module , if the connection is valid or not

fun isConnected():Int{
    return connected
}


}