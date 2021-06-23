import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.example.nsdkotlin.Connection
import com.example.nsdkotlin.ServiceObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception

//weak reference of the context
class HotstarConnect( var serviceName :String,var context : Context){

    //serviceName is the string denoting  a part of the name of the service you want to discover or broadcast

    //NsdManager needed from the Android Library to be able to manage communication
    private lateinit var nsdManager: NsdManager
    private lateinit var connection : Connection
    private val serviceList : MutableList<ServiceObject> = mutableListOf()
    private val SERVICE_TYPE : String = "_http._tcp."



    init{
        //initialising the NsdManager
        try{
 nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager}
    catch(e :Exception){
        // try to throw some error serviceObject
    }}



    //discover function for getting the list of services



fun discover(): Flow<ServiceObject> = flow{
    //write the logic
    nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)


}


  fun connect(serviceobject: ServiceObject) : Connection {
      // is it proper code ?? it's  not even s suspend fnction , so how can we make sure that this
      // would initialise connection before returning , maybe launch in a different coroutine and then join()
      nsdManager.resolveService(serviceobject.service, resolveListener)
      return connection
  }

    // Instantiate a new DiscoveryListener
    val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {



        }

        override fun onServiceFound(service: NsdServiceInfo) {

            //create ServiceObject and add to the serviceList
            //can create duplicates in case of thread unsafety
            val serviceobject = ServiceObject(service)
            serviceList.add(serviceobject)
            //var++;


          //  if (service.serviceName.contains(serviceName)){nsdManager.resolveService(service, resolveListener)}
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
           //remove from the list
            //takes care of thread safety between servicefound and service lost
            //as even if after losing service it's found again and the found thread is executed first , it would
            val serviceobject = ServiceObject(service)
            serviceList.remove(serviceobject)
          //var++;
        }

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

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
           connection = Connection(serviceInfo)
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            //Log.e(TAG, "Resolve Succeeded. $serviceInfo")
               connection = Connection(serviceInfo,1)

            // val port: Int = serviceInfo.port
            // val host: InetAddress = serviceInfo.host
        }
    }



}