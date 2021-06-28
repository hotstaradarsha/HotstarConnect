package com.example.nsdkotlin


import android.net.nsd.NsdServiceInfo


class ServiceObject internal constructor( internal val service : NsdServiceInfo){


    override fun equals(other: Any?): Boolean {
        if(other is ServiceObject){
        return (service.serviceName.equals(other.service.serviceName))}
        else { return super.equals(other)}
    }

    //to make sure the set we use doesn't contain duplicates
    override fun hashCode(): Int {
        return service.serviceName.hashCode()
    }

}