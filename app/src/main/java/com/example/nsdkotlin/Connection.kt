package com.example.nsdkotlin

import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.Socket
import java.nio.charset.StandardCharsets

class Connection internal constructor( internal val service : NsdServiceInfo, internal var connected : Int =0) {
//allowed to check , out of the module , if the connection is valid or not

    fun isConnected(): Int {
        return connected
    }


    //want to send the Ack back to the phone , 1 if successfull else 0 when failure occurs
    //for now just send success
    //should i do it myself?? if i do this then the dev would anyway have to call it inside another couroutine??
    // i think they would start a couroutine and call these functions there?
  suspend  fun sendJson(jsonobject: JSONObject): Int {
        var ret  =0
        withContext(Dispatchers.IO){
       try {
            var message = jsonobject.toString()
            var clientsocket = Socket(service.host, service.port)
           var outstream =   OutputStreamWriter(
                clientsocket.getOutputStream(), StandardCharsets.UTF_8
            )
           outstream.use { out -> out.write(jsonobject.toString()) }
           //closing the stram and the socket
           outstream.close()
           clientsocket.close()
            /*val out = PrintWriter(
                BufferedWriter(
                    OutputStreamWriter(clientsocket.getOutputStream())
                ),
                true
            )
            out.println(message) */
      } catch (e: Exception) {
          ret = 1
        }

    }

        if(ret==0){return 1}
  else{return 0} }

}