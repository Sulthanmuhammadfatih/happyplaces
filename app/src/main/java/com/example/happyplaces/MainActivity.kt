package com.example.happyplaces

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.Adapter.HappyPlacesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    companion object {
         val EXTRA_PLACE_DETAIL = "extra_place_detais"
        private const val ADD_PLACES_ACTIVITY_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab_add_happy.setOnClickListener {
            val intent =  Intent(this@MainActivity, AddHappyPlacesActivity::class.java)
            startActivityForResult(intent, ADD_PLACES_ACTIVITY_REQUEST_CODE)

        }

        getHappyPlacesListFromLocalDB()

    }
    private fun getHappyPlacesListFromLocalDB() {
        val dbHandler = DataBaseHandler(this)
        val getHappyPlaceList = dbHandler.getHappyPlaceList()


        if (getHappyPlaceList.size > 0) {
            rv_happy_place_list.visibility = View.VISIBLE
            no_records.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlaceList)
        } else {
            rv_happy_place_list.visibility = View.VISIBLE
            no_records.visibility = View.VISIBLE
        }
    }

    private fun setupHappyPlacesRecyclerView(happyPLaceList: ArrayList<HappyPlaceModel>) {

        rv_happy_place_list.layoutManager = LinearLayoutManager(this)
        rv_happy_place_list.setHasFixedSize(true)
        val placeAdapter = HappyPlacesAdapter(this, happyPLaceList)
        rv_happy_place_list.adapter = placeAdapter


        val editSwipHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_place_list.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACES_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_place_list)

        val deleteSwipeHandler = object  : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_place_list.adapter as
                        HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()
            }
        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_place_list)
    }






    private fun OnActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode, data)

        if (requestCode == ADD_PLACES_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK)
                getHappyPlacesListFromLocalDB()
        } else {
            Log.e("Activity", "Cancelled or back Pressed")


        }





    }

    



}
