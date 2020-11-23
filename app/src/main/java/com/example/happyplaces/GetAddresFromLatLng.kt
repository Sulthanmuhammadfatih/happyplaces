package com.example.happyplaces

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.lang.StringBuilder
import java.util.*




class GetAddresFromLatLng(context: Context, private val latitude: Double,private val mLongitude: Double)
    : AsyncTask<Void, String, String>() {

    private val geocoder: Geocoder = Geocoder(context,Locale.getDefault())
    private lateinit var mAddressListener : AddressListener

    override fun doInBackground(vararg params: Void?): String {
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, mLongitude, 1)
            if (addressList != null && addressList.isEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append(",")
                }
                sb.deleteCharAt(sb.length - 1)

                return sb.toString()
            }
        } catch (e: IOException) {
            Log.e("HappyPlaces", " Unable connect to geoCoder")
        }
        return ""

    }

    override fun onPostExecute(result: String?) {
        if (result == null) {
            mAddressListener.onError()
        } else {
            mAddressListener.onAddresFound(result)
        }
        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }

    fun getAddress(){
        execute()
    }


    interface AddressListener {
        fun onAddresFound(address: String)
        fun onError()
    }
}
