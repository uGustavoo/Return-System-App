package com.app.returnsystem.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.returnsystem.MainActivity
import com.app.returnsystem.R
import com.app.returnsystem.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupCadastroButton()
        setupEntrarButton()

        return binding.root
    }

    // Este método configura o comportamento do botão "Cadastro"
    private fun setupCadastroButton() {
        binding.cadastro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment)
        }
    }

    // Este método configura o comportamento do botão "Entrar"
    private fun setupEntrarButton() {
        binding.entrar.setOnClickListener {
            val email = binding.email.text.toString()
            val senha = binding.senha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                exibirToast("preencha todos os campos")
            } else {
                logar(email, senha)
                limparCampos()
            }
        }
    }

    // Este método loga o usuário verificando se suas credenciais são válidas
    private fun logar(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnSuccessListener {
                db.collection("usuarios")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            exibirToast("Credenciais inválidas")
                        } else {
                            val documentSnapshot = querySnapshot.documents[0]
                            val usuario = documentSnapshot.get("usuario").toString()

                            exibirToast("Olá, $usuario!")

                            salvarPerfilUsuario(usuario, email)

                            startActivity(Intent(requireActivity(), MainActivity::class.java))
                        }
                    }
            }.addOnFailureListener {
                exibirToast("Erro ao fazer o login do usuário")
            }
    }

    // Este método salva as informações do perfil do usuário em SharedPreferences
    private fun salvarPerfilUsuario(usuario: String, email: String) {
        val sharedPref = requireActivity().getSharedPreferences("perfil_usuario", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("nome_usuario", usuario)
        editor.putString("email_usuario", email)
        editor.apply()
    }

    private fun limparCampos() {
        // Limpa os campos de entrada do usuário usando
        binding.email.setText("")
        binding.senha.setText("")
    }

    private fun exibirToast(mensagem: String) {
        Toast.makeText(requireActivity(), mensagem, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()

        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if (usuarioAtual != null){
            startActivity(Intent(requireActivity(), MainActivity::class.java))
        }
    }
}