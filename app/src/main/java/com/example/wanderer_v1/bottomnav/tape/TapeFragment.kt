package com.example.wanderer_v1.bottomnav.tape

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wanderer_v1.databinding.FragmentTapeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TapeFragment : Fragment() {

    private lateinit var binding: FragmentTapeBinding
    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var databaseReference: DatabaseReference
    private val placeList = mutableListOf<Place>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTapeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placeAdapter = PlaceAdapter(placeList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = placeAdapter

        databaseReference = FirebaseDatabase.getInstance().reference.child("Place")
        fetchPlaces()
    }

    private fun fetchPlaces() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                placeList.clear()
                for (dataSnapshot in snapshot.children) {
                    val place = dataSnapshot.getValue(Place::class.java)
                    place?.let { placeList.add(it) }
                }
                placeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TapeFragment", "Database error: ${error.message}")
            }
        })
    }
}

