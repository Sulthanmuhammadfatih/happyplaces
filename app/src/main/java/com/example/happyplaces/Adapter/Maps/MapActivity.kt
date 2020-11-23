package com.example.happyplaces.Adapter.Maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.HappyPlaceModel
import com.example.happyplaces.MainActivity
import com.example.happyplaces.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import kotlinx.android.synthetic.main.activity_map.*

private var mHappyPlaceDetail : HappyPlaceModel? = null

class MapActivity : AppCompatActivity(), OnMapReadyCallback{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)


        mHappyPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAIL)

        mHappyPlaceDetail.let {
            if (it != null){
//                setSupportActionBar(toolbar_map)
//                supportActionBar?.setDisplayHomeAsUpEnabled(true)
//                supportActionBar?.title = it?.title

                toolbar_map.setNavigationOnClickListener{
                    onBackPressed()
                }

                val supportMapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                supportMapFragment.getMapAsync(this)
            }
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        val position = LatLng(mHappyPlaceDetail!!.latitude, mHappyPlaceDetail!!.longitude)

        map!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))
        val newLatlngZoom = CameraUpdateFactory.newLatLngZoom(position, 20f)
        map.animateCamera(newLatlngZoom)
    }


}