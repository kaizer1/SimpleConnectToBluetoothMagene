package com.losfrees.hsba.simpleconnecttobluetoothmagene

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater;
import android.widget.ListAdapter


class CustomAdapterLO(private val dataSet: ArrayList<String>) :
        RecyclerView.Adapter<CustomAdapterLO.ViewHolder>(), ListAdapter {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName : TextView = view.findViewById(R.id.text_view_name)
        val textViewAddress : TextView = view.findViewById(R.id.text_view_address)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.text_row_item_ll, viewGroup, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        val dataStringElement = dataSet[position]

        viewHolder.textViewName.text = dataSet[position]
        viewHolder.textViewAddress.text = dataStringElement
    }


    override fun getItemCount() = dataSet.size

    override fun registerDataSetObserver(p0: DataSetObserver?) {
        //TODO("Not yet implemented")
    }

    override fun unregisterDataSetObserver(p0: DataSetObserver?) {
       // TODO("Not yet implemented")
    }

    override fun getCount(): Int {
     //    TODO("Not yet implemented")

        return dataSet.size
    }

    override fun getItem(p0: Int): Any {
       // TODO("Not yet implemented")

         return p0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
      //  TODO("Not yet implemented")

        var view = p1

        if(view == null){
        view = LayoutInflater.from(p2!!.context).inflate(R.layout.text_row_item_ll, p2, false)

        }

        val stringM = dataSet[p0]

                val findA = stringM.indexOf("%%", 0)
                val substrin1 = stringM.substring(0,findA)
                val subStrin2 = stringM.substring(findA +2, stringM.length)


        view!!.findViewById<TextView>(R.id.text_view_name).text = substrin1
        view.findViewById<TextView>(R.id.text_view_address).text = subStrin2

        return view!!

    }

    override fun getViewTypeCount(): Int {
       // TODO("Not yet implemented")
      return dataSet.size
    }

    override fun isEmpty(): Boolean {
     //   TODO("Not yet implemented")
      return false
    }

    override fun areAllItemsEnabled(): Boolean {
       // TODO("Not yet implemented")
        return true
    }

    override fun isEnabled(p0: Int): Boolean {
      //  TODO("Not yet implemented")
        return true
    }

}