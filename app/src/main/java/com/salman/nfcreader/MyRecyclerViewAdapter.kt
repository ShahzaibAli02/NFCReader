package com.salman.nfcreader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {

    private var data: List<List<String>> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val biding=LayoutInflater.from(context).inflate(R.layout.layout_sheet_list_item,parent,false)
        return ViewHolder(biding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }


    fun setData(newData: List<List<String>>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: View) : RecyclerView.ViewHolder(binding) {
        fun bind(item: List<String>) {
            binding.findViewById<TextView>(R.id.tagId).text=   item.getOrElse(0) { index -> "" };
            binding.findViewById<TextView>(R.id.phoneID).text=   item.getOrElse(1) { index -> "" };
            binding.findViewById<TextView>(R.id.noteID).text=   item.getOrElse(2) { index -> "" };
            binding.findViewById<TextView>(R.id.noteID2).text=   item.getOrElse(3) { index -> "" };
        }
    }

}
