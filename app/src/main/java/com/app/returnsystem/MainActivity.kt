@file:Suppress("DEPRECATION")

package com.app.returnsystem

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.app.returnsystem.databinding.ActivityMainBinding
import com.app.returnsystem.ui.sobre.SobreActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Int = R.id.nav_coleta
    private val db = FirebaseFirestore.getInstance()
    private var primeiraLeituraDevolucao: String = ""
    private var codigoKit: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Definir a navegação e visualizações do menu
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val bottomView: BottomNavigationView = binding.appBarMain.bottomNavigationView

        // Obter preferências compartilhadas do usuário
        val sharedPref = getSharedPreferences("perfil_usuario", Context.MODE_PRIVATE)
        val nomeUsuario = sharedPref.getString("nome_usuario", null)
        val emailUsuario = sharedPref.getString("email_usuario", null)

        // Define a visualização do cabeçalho com as informações do usuário
        navView.getHeaderView(0).apply {
            findViewById<TextView>(R.id.header_usuario).text = nomeUsuario
            findViewById<TextView>(R.id.header_email).text = emailUsuario
        }

        // Define a tela atual quando o destino muda
        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentFragment = destination.id
        }

        // Definir botão para escanear o código com base no destino atual
        binding.appBarMain.barAdicionar.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.nav_coleta -> scanCodeColeta()
                R.id.nav_devolucao -> scanCodeDevolucao()
                R.id.nav_modelos -> scanCodeModelos()
                else -> Toast.makeText(this, "Ação não permitida nesta tela", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar barra de ação e navegação com configuração
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_coleta, R.id.nav_devolucao, R.id.nav_status, R.id.nav_modelos),
            drawerLayout
        )

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_coleta, R.id.nav_devolucao),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomView.setupWithNavController(navController)

        // Configura o item para ir para a tela sobre
        navView.menu.findItem(R.id.menu_sobre).setOnMenuItemClickListener {
            startActivity(Intent(this, SobreActivity::class.java))
            true
        }

        // Configura o item para sair
        navView.menu.findItem(R.id.menu_sair).setOnMenuItemClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            true
        }
    }

    private fun scanCodeColeta() {
        IntentIntegrator(this).apply {
            setPrompt("Aponte a câmera para o serial bar")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setBarcodeImageEnabled(false)
            initiateScan()
        }
    }

    private fun scanCodeDevolucao() {
        val prompt = if (primeiraLeituraDevolucao.isEmpty()) {
            "Aponte a câmera para a box label"
        } else {
            "Aponte a câmera para a quality label"
        }

        IntentIntegrator(this).apply {
            setPrompt(prompt)
            setBeepEnabled(true)
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setBarcodeImageEnabled(false)
            initiateScan()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun salvarDevolucao(codigoBarras: String, modelo_resultado: String,
                                serial_number: String, sn: String, codigoKit: String,
                                data_hora: String, usuario: String?) {
        val recebedor = "Logística"

        db.collection("status")
            .whereEqualTo("serial_number", codigoBarras).limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    val document = query.documents[0]
                    val obs_resultado = document.get("observacoes").toString()

                    val mapstatus = hashMapOf(
                        "emissao" to SimpleDateFormat("yyyy-MM-dd").format(Date()),
                        "modelo" to modelo_resultado,
                        "serial_number" to serial_number,
                        "sn" to sn,
                        "kit" to codigoKit,
                        "operacao" to "Devolução",
                        "data" to data_hora,
                        "recebedor" to recebedor,
                        "usuario" to usuario,
                        "observacoes" to obs_resultado
                    )

                    db.collection("status")
                        .add(mapstatus)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Modelo registrado: $modelo_resultado", Toast.LENGTH_LONG).show()
                            recreate()

                            val historicoRef = Firebase.firestore.collection("historico")
                            val historicoQuery = historicoRef.whereEqualTo("serial_number", serial_number)
                            historicoQuery.get()
                                .addOnSuccessListener { querySnapshot ->
                                    for (historicoDoc in querySnapshot) {
                                        historicoRef.document(historicoDoc.id).update("recebedor", recebedor)
                                    }

                                    for (historicoDoc in querySnapshot) {
                                        historicoRef.document(historicoDoc.id).update("devolucao", data_hora)
                                    }

                                    for (historicoDoc in querySnapshot) {
                                        historicoRef.document(historicoDoc.id).update("kit", codigoKit)
                                    }

                                    historicoQuery.get()
                                        .addOnSuccessListener { querySnapshot2 ->
                                            for (historicoDoc2 in querySnapshot2) {
                                                val usuario_atual = historicoDoc2.get("usuario") as? String
                                                    ?: continue
                                                if (usuario_atual == usuario) {
                                                    // O usuário já está correto, não há necessidade de atualizar
                                                    continue
                                                }
                                                val nova_informacao = " | $usuario"
                                                val novo_usuario = usuario_atual + nova_informacao
                                                historicoRef.document(historicoDoc2.id).update("usuario", novo_usuario)
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Não foi possível registrar", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        }
                }
            }
        primeiraLeituraDevolucao = ""
    }

    private fun scanCodeKit(callback: (String) -> Unit) {
        IntentIntegrator(this).apply {
            setPrompt("Aponte a câmera para o REMOCON do KIT")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setBarcodeImageEnabled(false)
            initiateScan()
        }

        // Define uma função de retorno de chamada a ser executada quando o usuário ler o código do kit
        this.callback = callback
    }

    private var callback: ((String) -> Unit)? = null

    private fun scanCodeModelos() {
        IntentIntegrator(this).apply {
            setPrompt("Aponte a câmera para o serial bar")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setBarcodeImageEnabled(false)
            initiateScan()
        }
    }

    private fun exibirDialogModelo(codigoBarras: String) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Digite o nome do modelo"
            setPadding(50, 50, 50, 50)
        }

        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Nome do modelo")
            setMessage("Insira o nome do modelo para o serial lido:")
            setView(input)
            setPositiveButton("Salvar") { _, _ ->
                val nomeModelo = input.text.toString()
                salvarModelo(codigoBarras, nomeModelo)
            }
            setNegativeButton("Cancelar", null)
            create()
        }

        dialog.show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun salvarModelo(codigoBarras: String, nomeModelo: String) {
        val mapModelo = hashMapOf(
            "part_number" to codigoBarras.substring(15),
            "modelo" to nomeModelo,
            "cadastragem" to SimpleDateFormat("yyyy-MM-dd").format(Date())
        )

        db.collection("modelos")
            .document(codigoBarras)
            .set(mapModelo)
            .addOnSuccessListener {
                Toast.makeText(this, "Modelo salvo com sucesso", Toast.LENGTH_LONG).show()
                recreate()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível salvar o modelo", Toast.LENGTH_LONG).show()
            }
    }

    @SuppressLint("SimpleDateFormat")
    @Deprecated("Descontinuado no Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                val sharedPref = getSharedPreferences("perfil_usuario", Context.MODE_PRIVATE)
                val nomeUsuario = sharedPref.getString("nome_usuario", null)

                val codigoBarras = result.contents

                // Executa a função callback se ela existir
                callback?.let {
                    val kit = result.contents
                    it(kit)
                }

                // Verifica se o código de barras inválido ou é válido
                if (codigoBarras.length < 15) {
                    Toast.makeText(this, "Código de barras inválido", Toast.LENGTH_LONG).show()
                    return
                }

                // Verifica qual fragmento é exibido atualmente
                when (currentFragment) {
                    R.id.nav_modelos -> {
                        db.collection("modelos")
                            .whereEqualTo("part_number", codigoBarras.substring(15)).limit(1).get()
                            .addOnSuccessListener { query ->
                                if (query.isEmpty) {
                                    exibirDialogModelo(codigoBarras)
                                } else {
                                    Toast.makeText(this, "Modelo já registrado", Toast.LENGTH_LONG).show()
                                    return@addOnSuccessListener
                                }
                            }
                    }
                }

                // Verifica se o modelo existe no banco de dados
                db.collection("modelos")
                    .whereEqualTo("part_number", codigoBarras.substring(15)).limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val documentSnapshot = querySnapshot.documents[0]
                            val modelo_resultado = documentSnapshot.get("modelo").toString()

                            val serial_number = codigoBarras
                            val sn = codigoBarras.substring(10, 15)
                            val operacao_coleta = "Coleta"
                            val operacao_devolucao = "Devolução"
                            val usuario = nomeUsuario
                            val data_hora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

                            when (currentFragment) {
                                R.id.nav_coleta -> {
                                    // Verifica se o modelo existe no banco de dados
                                    db.collection("status")
                                        .whereEqualTo("serial_number", serial_number)
                                        .whereEqualTo("operacao", operacao_coleta).limit(1)
                                        .get()
                                        .addOnSuccessListener { query ->
                                            if (query.isEmpty) {
                                                val input = EditText(this).apply {
                                                    inputType = InputType.TYPE_CLASS_TEXT
                                                    hint = "Obs:"
                                                    setPadding(50, 50, 50, 50)
                                                }

                                                val dialog = AlertDialog.Builder(this).apply {
                                                    setTitle("Observações")
                                                    setMessage("Caso necessário, escreva alguma observação no campo abaixo")
                                                    setView(input)
                                                    setPositiveButton("Fazer coleta") { _, _ ->
                                                        // Atribuindo o valor do EditText para a variável obs
                                                        val obs = input.text.toString()

                                                        val mapstatus = hashMapOf(
                                                            "emissao" to SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                                            "modelo" to modelo_resultado,
                                                            "serial_number" to serial_number,
                                                            "sn" to sn,
                                                            "kit" to "----------",
                                                            "operacao" to operacao_coleta,
                                                            "data" to data_hora,
                                                            "recebedor" to "----------",
                                                            "usuario" to usuario,
                                                            "observacoes" to obs
                                                        )

                                                        val maphistorico = hashMapOf(
                                                            "emissao" to SimpleDateFormat("yyyy-MM-dd").format(Date()),
                                                            "modelo" to modelo_resultado,
                                                            "serial_number" to serial_number,
                                                            "sn" to sn,
                                                            "kit" to "----------",
                                                            "coleta" to data_hora,
                                                            "devolucao" to "----------",
                                                            "recebedor" to "----------",
                                                            "usuario" to usuario,
                                                            "observacoes" to obs
                                                        )

                                                        val firestore = Firebase.firestore

                                                        firestore.runBatch { batch ->
                                                            batch.set(firestore.collection("status").document(), mapstatus)
                                                            batch.set(firestore.collection("historico").document(), maphistorico)
                                                        }.addOnSuccessListener {
                                                            Toast.makeText(applicationContext, "Modelo registrado: $modelo_resultado", Toast.LENGTH_LONG).show()
                                                            recreate()
                                                        }.addOnFailureListener {
                                                            Toast.makeText(applicationContext, "Não foi possível registrar", Toast.LENGTH_SHORT).show()
                                                        }

                                                    }
                                                    setNegativeButton("Cancelar", null)
                                                    create()
                                                }

                                                dialog.show()

                                            } else {
                                                Toast.makeText(this, "Serial já registrado", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                }

                                R.id.nav_devolucao -> {
                                    if (primeiraLeituraDevolucao.isEmpty()) {
                                        // Verifica se o modelo já foi feito a coleta
                                        db.collection("status")
                                            .whereEqualTo("serial_number", serial_number)
                                            .whereEqualTo("operacao", operacao_coleta).limit(1)
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.isEmpty) {
                                                    Toast.makeText(this, "Serial sem registro de coleta", Toast.LENGTH_LONG).show()
                                                } else {
                                                    // Verifica se o modelo já foi feito a devolução
                                                    db.collection("status")
                                                        .whereEqualTo("serial_number", serial_number)
                                                        .whereEqualTo("operacao", operacao_devolucao).limit(1)
                                                        .get()
                                                        .addOnSuccessListener { query ->
                                                            if (query.isEmpty) {
                                                                Toast.makeText(this, "Box Label lido com sucesso", Toast.LENGTH_LONG).show()
                                                                primeiraLeituraDevolucao = serial_number
                                                                scanCodeDevolucao()
                                                            } else {
                                                                Toast.makeText(this, "Serial já registrado", Toast.LENGTH_LONG).show()
                                                                primeiraLeituraDevolucao = ""
                                                            }
                                                        }
                                                }
                                            }

                                    } else {
                                        if (primeiraLeituraDevolucao == codigoBarras) {
                                            val builder = AlertDialog.Builder(this)
                                            builder.setTitle("Leitura de kit")
                                            builder.setMessage("Você deseja fazer a leitura do kit?")
                                            builder.setPositiveButton("Sim") { dialog, which ->
                                                scanCodeKit { kit ->
                                                    codigoKit = kit
                                                    salvarDevolucao(codigoBarras, modelo_resultado, serial_number, sn, codigoKit, data_hora, usuario)
                                                }
                                            }
                                            builder.setNegativeButton("Não") { dialog, which ->
                                                codigoKit = "Não se aplica"
                                                salvarDevolucao(codigoBarras, modelo_resultado, serial_number, sn, codigoKit, data_hora, usuario)
                                            }
                                            builder.show()


                                        } else if (!primeiraLeituraDevolucao.isEmpty() && primeiraLeituraDevolucao != codigoBarras) {
                                            Toast.makeText(this, "Seriais não correspondem", Toast.LENGTH_LONG).show()
                                            primeiraLeituraDevolucao = ""
                                            scanCodeDevolucao()
                                        }
                                    }
                                }

                                else -> {
                                    Toast.makeText(this, "Ação não permitida nesta tela", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Modelo não encontrado", Toast.LENGTH_LONG).show()
                            primeiraLeituraDevolucao = ""
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("Erro ao encontrar o modelo: $exception")
                    }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_main_search, menu)

        val item = menu.findItem(R.id.menu_pesquisa)
        binding.appBarMain.searchView.setMenuItem(item)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_pesquisa){
            Toast.makeText(this, "Pesquisa", Toast.LENGTH_SHORT).show()
        }else if (item.itemId == R.id.menu_refresh){
            recreate()
        }
        return super.onOptionsItemSelected(item)
    }
}