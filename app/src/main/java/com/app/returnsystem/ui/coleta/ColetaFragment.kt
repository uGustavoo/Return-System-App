package com.app.returnsystem.ui.coleta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.returnsystem.databinding.FragmentColetaBinding
import com.app.returnsystem.ui.firestore.ColetaAdapter
import com.app.returnsystem.ui.firestore.ColetaData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ColetaFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentColetaBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<ColetaData>
    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColetaBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = ColetaAdapter(ArrayList()) // Defina um adaptador vazio padrão aqui

        dataList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        db.collection("status").whereEqualTo("operacao", "Coleta").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val data: ColetaData = document.toObject(ColetaData::class.java)
                        dataList.add(data)
                    }
                    recyclerView.adapter = ColetaAdapter(dataList)
                } else {
                    recyclerView.adapter = ColetaAdapter(ArrayList())
                }
            }
            .addOnFailureListener {
                recyclerView.adapter = ColetaAdapter(ArrayList())
                Toast.makeText(requireActivity(), it.toString(), Toast.LENGTH_SHORT).show()
            }

        return binding.root
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}