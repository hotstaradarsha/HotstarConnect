import android.app.Service
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.widget.Toast
import com.example.nsdkotlin.Connection
import com.example.nsdkotlin.ServiceObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.ServerSocket
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


//add internet permissions with this library


//weak reference of the context
class HotstarConnect( var serviceName :String,var context : WeakReference<Context>){

    //serviceName is the string denoting  a part of the name of the service you want to discover or broadcast

    //NsdManager needed from the Android Library to be able to manage communication
    private lateinit var nsdManager: NsdManager
    private var serviceList : MutableSet<ServiceObject> = mutableSetOf()
    private val SERVICE_TYPE : String = "_http._tcp."
    lateinit private var serverSocket : ServerSocket
    lateinit var discoveryListener : NsdManager.DiscoveryListener



    init{
        //initialising the NsdManager
       try{
 nsdManager = context.get()?.getSystemService(Context.NSD_SERVICE) as NsdManager
    }catch(e :Exception){
        // try to throw some error serviceObject
            }
         }



    suspend  fun showToast(s : String){
        withContext(Dispatchers.Main) {
            Toast.makeText(context.get(), s, Toast.LENGTH_SHORT).show()
        }
    }

    //discover function for getting the list of services


//should be suspend or not , this is the question
fun discover(): Flow<MutableList<ServiceObject>> = callbackFlow{


    // Instantiate a new DiscoveryListener
     discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {



        }

        override fun onServiceFound(service: NsdServiceInfo) {

            //create ServiceObject and add to the serviceList
            //can create duplicates in case of thread unsafety
            if(service.serviceName.contains(serviceName)) {
                val serviceobject = ServiceObject(service)
                serviceList.add(serviceobject)


                offer(serviceList.toMutableList())
            }

            //  if (service.serviceName.contains(serviceName)){nsdManager.resolveService(service, resolveListener)}
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            //remove from the list
            //takes care of thread safety between servicefound and service lost
            //as even if after losing service it's found again and the found thread is executed first , it would
            if(service.serviceName.contains(serviceName)) {
            val serviceobject = ServiceObject(service)
            serviceList.remove(serviceobject)

                offer(serviceList.toMutableList())
            //var++;
        }  }

        override fun onDiscoveryStopped(serviceType: String) {
            //Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            //Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            // Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)

        }

    }


    nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

// i think it should be there
   awaitClose{nsdManager.stopServiceDiscovery(discoveryListener)}
}


  suspend fun connect(serviceobject: ServiceObject) : Connection =  suspendCoroutine{
      nsdManager.resolveService(serviceobject.service, object : NsdManager.ResolveListener {

          override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
              // Called when the resolve fails. Use the error code to debug.
             val connection = Connection(serviceInfo)
              it.resume(connection)
          }

          override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
              //Log.e(TAG, "Resolve Succeeded. $serviceInfo")
             val connection = Connection(serviceInfo,1)
              it.resume(connection)

          }
      })



  }


    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            //mServiceName = NsdServiceInfo.serviceName
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            //send error json probably // so that they can try again
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            //send error json probably // so that developer can try again
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
        }
    }

    private fun  registerService(port: Int) {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            this.serviceName = this@HotstarConnect.serviceName + android.os.Build.MODEL
            this.serviceType = SERVICE_TYPE
            setPort(port)
        }

         nsdManager.apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }


    //should we have such a while loop here , also should a flow be suspend function
fun broadcast():Flow<JSONObject> =  flow{
    //setting up a free port for communication
    var mLocalPort: Int  =0
     serverSocket = ServerSocket(0).also { socket ->
            // Store the chosen port.
            mLocalPort = socket.localPort
            showToast(""+ mLocalPort)
            registerService(mLocalPort)
        }
    // register the service with that port
        //keep listening to the port and send the flow
 while(true) {
     try{
     val socket = serverSocket.accept()
     showToast("socket accepted")
     val isr =  InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
     val reader =  BufferedReader(isr)
    // val dis = Scanner(socket.getInputStream())
     val str = reader.readLine()
         reader.close()   // close the bufferedreader to avoid memory leak
     val json: JSONObject = JSONObject(str)
     showToast(str + " received")
     emit(json)}
     catch (e : Exception){}

    }

    }


    //call this in ondestroy of your activity
    fun stopBroadcast(){
        // close the socket for the broadcaster
        try{ if(!serverSocket.isClosed()){serverSocket.close()} }
        catch(e : Exception){}
        //close the registration
        try{nsdManager.unregisterService(registrationListener)}
        catch (e : Exception){}
        //close discovering
        try{nsdManager.stopServiceDiscovery(discoveryListener)}
        catch(e : Exception){}
    }




}




