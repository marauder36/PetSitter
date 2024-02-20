package com.example.firebasertdb.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.firebasertdb.R
import com.example.firebasertdb.model.User

class ListAdapter:RecyclerView.Adapter<ListAdapter.MyViewHolder> (){

    private var userList = emptyList<User>()

    class MyViewHolder(itemView:View): ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_custom_row,parent,false))
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]
        holder.itemView.findViewById<TextView>(R.id.tv_id).text=currentItem.id.toString()
        holder.itemView.findViewById<TextView>(R.id.tv_first_name).text=currentItem.firstName.toString()
        holder.itemView.findViewById<TextView>(R.id.tv_last_name).text=currentItem.lastName.toString()
        holder.itemView.findViewById<TextView>(R.id.tv_age).text=currentItem.age.toString()

        holder.itemView.findViewById<ConstraintLayout>(R.id.rv_row_layout).setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(user:List<User>){
        this.userList=user
        notifyDataSetChanged()
    }


}