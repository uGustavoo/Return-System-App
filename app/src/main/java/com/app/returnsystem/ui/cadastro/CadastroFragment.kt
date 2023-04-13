package com.app.returnsystem.ui.cadastro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.app.returnsystem.R
import com.app.returnsystem.databinding.FragmentCadastroBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class CadastroFragment : Fragment() {

    private lateinit var binding: FragmentCadastroBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentCadastroBinding.inflate(inflater, container, false)

        // Definir ouvintes de clique para botões
        binding.login.setOnClickListener { navigateLogin() }
        binding.registrar.setOnClickListener { cadastrarUsuario() }

        return binding.root
    }

    private fun navigateLogin() {
        // Navega até o fragmento de login usando o componente de navegação
        Navigation.findNavController(binding.root).navigate(R.id.action_cadastroFragment_to_loginFragment)
    }

    @SuppressLint("SimpleDateFormat")
    private fun cadastrarUsuario() {
        // Obtém a entrada do usuário
        val usuario = binding.usuario.text.toString()
        val matricula = binding.matricula.text.toString()
        val email = binding.email.text.toString()
        val senha = binding.senha.text.toString()

        // Verifica se todos os campos estão preenchidos
        if (usuario.isEmpty() || matricula.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireActivity(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria o usuário com e-mail e senha fornecidos
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { cadastro ->
                if (cadastro.isSuccessful) {
                    val dataAtualFormatada = SimpleDateFormat("yyyy-MM-dd").format(Date())

                    // Cria um mapa de dados do usuário para ser adicionado ao Firestore
                    val mapUsuarios = hashMapOf(
                        "usuario" to usuario,
                        "matricula" to matricula,
                        "email" to email,
                        "senha" to senha,
                        "cadastragem" to dataAtualFormatada
                    )

                    // Adicionar dados do usuário ao Firestore e lidar com casos de sucesso e falha
                    db.collection("usuarios")
                        .add(mapUsuarios)
                        .addOnCompleteListener {
                            Toast.makeText(requireActivity(), "Usuário registrado com sucesso", Toast.LENGTH_LONG).show()
                            navigateLogin()
                            salvarPerfilUsuario(usuario, email)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireActivity(), "Ocorreu algum erro no registro", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Trata os possíveis erros de autenticação
                    when (cadastro.exception) {
                        is FirebaseAuthWeakPasswordException ->
                            Toast.makeText(requireActivity(), "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        is FirebaseAuthInvalidCredentialsException ->
                            Toast.makeText(requireActivity(), "Digite um email válido", Toast.LENGTH_SHORT).show()
                        is FirebaseAuthUserCollisionException ->
                            Toast.makeText(requireActivity(), "Esta conta já foi cadastrada", Toast.LENGTH_SHORT).show()
                        is FirebaseNetworkException ->
                            Toast.makeText(requireActivity(), "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(requireActivity(), "Erro ao Cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
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
}