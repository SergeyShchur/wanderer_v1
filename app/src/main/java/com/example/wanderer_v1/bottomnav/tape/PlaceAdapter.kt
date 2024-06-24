package com.example.wanderer_v1.bottomnav.tape

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wanderer_v1.databinding.ItemPlaceBinding
import com.google.firebase.storage.FirebaseStorage

class PlaceAdapter(private val places: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: Place) {
            binding.place = place

            place.imageUri?.let { imageUri ->
                val storageRef = FirebaseStorage.getInstance().reference.child("Place/$imageUri")
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Glide.with(binding.imageView.context)
                            .load(uri)
                            .into(binding.imageView)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PlaceAdapter", "Failed to load image: ${exception.message}")
                    }
            } ?: Log.e("PlaceAdapter", "imageUri is null for place: ${place.title}")
        }
    }
}
