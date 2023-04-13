package com.app.returnsystem.ui.firestore

data class ColetaData(
    val modelo: String? = null,
    val serial_number: String? = null,
    val sn: String? = null,
    val kit: String? = null,
    val usuario: String? = null,
    val observacoes: String? = null,
    val data: String? = null,
)

data class DevolucaoData(
    val modelo: String? = null,
    val serial_number: String? = null,
    val sn: String? = null,
    val kit: String? = null,
    val recebedor: String? = null,
    val usuario: String? = null,
    val data: String? = null,
)

data class StatusData(
    val modelo: String? = null,
    val serial_number: String? = null,
    val sn: String? = null,
    val kit: String? = null,
    val usuario: String? = null,
    val recebedor: String? = null,
    val operacao: String? = null,
    val observacoes: String? = null,
    val data: String? = null,
)

data class ModelosData(
    val modelo: String? = null,
    val part_number: String? = null,
    val cadastragem: String? = null,
)
