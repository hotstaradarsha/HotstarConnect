package com.example.nsdkotlin



import HotstarConnect
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
  lateinit  var hotstarconnect :HotstarConnect
  lateinit var serviceList : MutableList<ServiceObject>
  lateinit  var adapter : MyAdapter
  lateinit var  listview : ListView
    companion object {
        @JvmStatic lateinit var con : TwoWayConnection
    }

    fun showToast(s : String){
        runOnUiThread {
            Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var edittext = findViewById(R.id.edittext) as EditText

        hotstarconnect = HotstarConnect("hotstar", WeakReference(this@MainActivity.applicationContext))
         serviceList  = mutableListOf()
        adapter = MyAdapter(serviceList,this,hotstarconnect)

        GlobalScope.launch {
           val  conn = hotstarconnect.broadcast()
            showToast("broadcast returned")
            val messageflow = conn.getFlow()

            withContext(Dispatchers.Main){
                var sendclientbutton = findViewById(R.id.clientsendbutton) as Button
                sendclientbutton.setOnClickListener(View.OnClickListener {
                    GlobalScope.launch() {
                        // try{
                        if(conn.isConnected()==1){
                            var json =  JSONObject()
                            var msg = edittext.text.toString()
                            json.put("message",msg)

                            conn.sendJson(json)
                            showToast("sent to client" + msg)
                        }

                        else { showToast("can't send as not connected")}
                        // }
                        // catch(e: Exception){showToast("error in sending")}
                    }
                })
            }

            messageflow.collect {
                  try {val msg : String = it.get("message") as String
                showToast(msg)}
                  catch (e : Exception){showToast("error in message")}

            }



        }


       val discoverButton = findViewById(R.id.discoverbutton) as Button
        discoverButton.setOnClickListener(View.OnClickListener {
            GlobalScope.launch {

               val flow =  hotstarconnect.discover()
               flow.collect {
                    serviceList = it
                   if(serviceList.size>0){showToast(serviceList[0].service.serviceName as String)}
                   else { showToast("empty list")}
                   withContext(Dispatchers.Main){
                      // try{ if(adapter.connection.isConnected()==1){con = adapter.connection}}
                      // catch (e :Exception){}
                       adapter = MyAdapter(serviceList,this@MainActivity,hotstarconnect)
                       listview.setAdapter(adapter)
                   }
               }

            }
        })
  listview = findViewById(R.id.list_view) as ListView
        listview.setAdapter(adapter)


        var sendbutton = findViewById(R.id.sendbutton) as Button
        sendbutton.setOnClickListener(View.OnClickListener {
            GlobalScope.launch {
              // try{
                    if(con.isConnected()==1){
                        var json =  JSONObject()
                        var msg = edittext.text.toString()
                        json.put("message",msg)

                        con.sendJson(json)
                        showToast("sent" + msg)

                        var flower= con.getFlow()
                        flower.collect {
                            try {val msg : String = it.get("message") as String
                                showToast(msg)}
                            catch (e : Exception){showToast("error in message")}

                        }
                    }

                    else { showToast("can't send as not connected")}
              // }
              // catch(e: Exception){showToast("error in sending")}
            }
        })




    }

    class MyAdapter(var serviceList : MutableList<ServiceObject>, var c: MainActivity,var hconnect : HotstarConnect) : BaseAdapter() {
      lateinit var connection : TwoWayConnection
        // override other abstract methods here

        override fun getCount(): Int {
           return serviceList.size
        }

        override fun getItem(position: Int): Any {
            return serviceList.get(position)
        }

        override fun getItemId(position: Int): Long {
           return  (position as Number).toLong()
        }
        override fun getView(position: Int, convertView: View?, container: ViewGroup?): View? {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = (c as Activity).getLayoutInflater().inflate(R.layout.list_layout, container, false)
            }
            (convertView?.findViewById(R.id.servicename) as TextView)
                .setText((getItem(position) as ServiceObject).service.serviceName)

            convertView?.setOnClickListener(View.OnClickListener {
                GlobalScope.launch {
                   connection = hconnect.connect(getItem(position) as ServiceObject)
                    if(connection.isConnected()==1){

                        withContext(Dispatchers.Main) {
                            MainActivity.con = connection
                            Toast.makeText(c, "Connected "+ connection.connection.port, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        withContext(Dispatchers.Main) {
                            Toast.makeText(c, "Failed to Connect", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            return convertView
        }


    }


    override fun onDestroy() {
        hotstarconnect.stopBroadcast()
        super.onDestroy()
    }
}

