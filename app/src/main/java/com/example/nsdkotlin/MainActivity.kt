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
    fun showToast(s : String){
        runOnUiThread {
            Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         hotstarconnect = HotstarConnect("hotstar", WeakReference(this@MainActivity.applicationContext))
         serviceList  = mutableListOf()
        adapter = MyAdapter(serviceList,this,hotstarconnect)

       val discoverButton = findViewById(R.id.discoverbutton) as Button
        discoverButton.setOnClickListener(View.OnClickListener {
            GlobalScope.launch {

               val flow =  hotstarconnect.discover()
               flow.collect {
                    serviceList = it
                   if(serviceList.size>0){showToast(serviceList[0].service.serviceName as String)}
                   else { showToast("empty list")}
                   withContext(Dispatchers.Main){
                       adapter = MyAdapter(serviceList,this@MainActivity,hotstarconnect)
                       listview.setAdapter(adapter)
                   }
               }

            }
        })
  listview = findViewById(R.id.list_view) as ListView
        listview.setAdapter(adapter)

        var edittext = findViewById(R.id.edittext) as EditText

        var sendbutton = findViewById(R.id.sendbutton) as Button
        sendbutton.setOnClickListener(View.OnClickListener {
            GlobalScope.launch {
                try{
                    if(adapter.connection.isConnected()==1){
                        var json =  JSONObject()
                        var msg = edittext.text as String
                        json.put("message",msg)

                        adapter.connection.sendJson(json)
                        showToast("sent" + msg)
                    }

                    else { showToast("can't send as not connected")}
                }
                catch(e: Exception){}
            }
        })

    }

    class MyAdapter(var serviceList : MutableList<ServiceObject>, var c : Context,var hconnect : HotstarConnect) : BaseAdapter() {
      lateinit var connection : Connection
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
                            Toast.makeText(c, "Connected", Toast.LENGTH_SHORT).show()
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

}

