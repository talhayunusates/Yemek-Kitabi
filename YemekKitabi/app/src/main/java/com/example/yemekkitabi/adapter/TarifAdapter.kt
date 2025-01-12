package com.example.yemekkitabi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.yemekkitabi.databinding.RecyclerRowBinding
import com.example.yemekkitabi.model.Tarif
import com.example.yemekkitabi.view.ListeFragmentDirections

class TarifAdapter(var tarifList: List<Tarif>) : RecyclerView.Adapter<TarifAdapter.TarifHolder>() {
    class TarifHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {
        val recyclerRowBinding: RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TarifHolder(recyclerRowBinding)
    }
    override fun getItemCount(): Int {
        return tarifList.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
        holder.binding.rcylerViewTextView.text = tarifList[position].isim
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski", id = tarifList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }



}