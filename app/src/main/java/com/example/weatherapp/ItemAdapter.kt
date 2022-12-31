package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class ItemAdapter(private val ItemList:ArrayList<ItemData>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item_time:TextView  = view.findViewById(R.id.item_time)
        val item_temp:TextView = view.findViewById(R.id.item_temp)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view:View = LayoutInflater.from(viewGroup.context).inflate(R.layout.weather_item,viewGroup,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from dataset at this position and replace the
        // contents of the view with that element
        val item: ItemData = ItemList.get(position)
        viewHolder.item_temp.setText(item.temperature + "°C")
        viewHolder.item_time.setText(item.time)
    }


    // Return the size of dataset
    override fun getItemCount():Int{
        return ItemList.size
    }

}