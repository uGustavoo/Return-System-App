package com.app.returnsystem.ui.modelos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.returnsystem.databinding.FragmentModelosBinding
import com.app.returnsystem.ui.firestore.ModelosAdapter
import com.app.returnsystem.ui.firestore.ModelosData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ModelosFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentModelosBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<ModelosData>
    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModelosBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        dataList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        db.collection("modelos").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val dataList = ArrayList<ModelosData>()
                    for (document in documents) {
                        val data: ModelosData = document.toObject(ModelosData::class.java)
                        dataList.add(data)
                    }
                    recyclerView.adapter = ModelosAdapter(dataList)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireActivity(), it.toString(), Toast.LENGTH_SHORT).show()
            }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}