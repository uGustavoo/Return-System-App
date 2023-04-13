package com.app.returnsystem.ui.devolucao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.returnsystem.databinding.FragmentColetaBinding
import com.app.returnsystem.ui.firestore.DevolucaoAdapter
import com.app.returnsystem.ui.firestore.DevolucaoData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DevolucaoFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentColetaBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataList: ArrayList<DevolucaoData>
    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColetaBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        dataList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        db.collection("status").whereEqualTo("operacao", "Devolução").get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val dataList = ArrayList<DevolucaoData>()
                    for (document in documents) {
                        val data: DevolucaoData = document.toObject(DevolucaoData::class.java)
                        dataList.add(data)
                    }
                    recyclerView.adapter = DevolucaoAdapter(dataList)
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