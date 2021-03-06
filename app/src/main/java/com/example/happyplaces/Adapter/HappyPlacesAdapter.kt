package com.example.happyplaces.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.*
import com.example.happyplaces.MainActivity.Companion.EXTRA_PLACE_DETAIL
import kotlinx.android.synthetic.main.item_happy_places.view.*

class HappyPlacesAdapter(

    private val context: Context,
    private val list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_places,
                parent,
                false
            )
        )
    }




    fun notifyEditItem(activity : Activity, position: Int, requestCode : Int) {
        val intent = Intent(context, AddHappyPlacesActivity::class.java)
        intent.putExtra(EXTRA_PLACE_DETAIL, list[position])
        activity.startActivityForResult(intent, requestCode)

        notifyItemChanged(position)
    }


    fun removeAt(position: Int) {
        val dbHandler = DataBaseHandler(context)
        val isDelete = dbHandler.deleteHappyPlace(list[position])
        if (isDelete > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }





    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.txt__ttile.text = model.title
            holder.itemView.txt_decs.text = model.description

            holder.itemView.setOnClickListener {
                val intentDetail = Intent(
                    it.context, ActivityHappyDetail::class.java
                )
                intentDetail.putExtra(EXTRA_PLACE_DETAIL, model)
                it.context.startActivity(intentDetail)
            }
        }


    }


    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}




