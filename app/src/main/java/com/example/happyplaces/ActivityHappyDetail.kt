package com.example.happyplaces

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.Adapter.Maps.MapActivity
import kotlinx.android.synthetic.main.activity_happy_detail.*

class ActivityHappyDetail : AppCompatActivity() {
    private var happyPlaceDeatilModel : HappyPlaceModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_detail)

        setSupportActionBar(toolbar_detail_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_detail_place.setNavigationOnClickListener {
            onBackPressed()
        }

        happyPlaceDeatilModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAIL)
        happyPlaceDeatilModel?.let {
            supportActionBar?.title = it.title
            iv_place_image.setImageURI(Uri.parse(it.image))
            txt_location_detail.text = it.location
            txt_description_detail.text = it.description

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAIL, happyPlaceDeatilModel)
                startActivity(intent)
            }
        }
    }
}