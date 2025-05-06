package com.example.qrscanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AsistenciasActivity : AppCompatActivity() {

    private lateinit var asistenciasText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistencias)

        asistenciasText = findViewById(R.id.asistenciasText)
        val db = FirebaseFirestore.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        db.collection("asistencias").get().addOnSuccessListener { fechas ->
            val lista = StringBuilder()

            val fechasTotales = fechas.documents.size
            var fechasProcesadas = 0

            for (fechaDoc in fechas) {
                val fechaId = fechaDoc.id
                val entradasRef = db.collection("asistencias").document(fechaId).collection("Entradas")
                val salidasRef = db.collection("asistencias").document(fechaId).collection("Salidas")

                entradasRef.get().addOnSuccessListener { entradas ->
                    salidasRef.get().addOnSuccessListener { salidas ->

                        val mapaSalidas = salidas.associateBy { it.getString("Clave") }

                        for (entrada in entradas) {
                            val clave = entrada.getString("Clave") ?: continue
                            val nombre = entrada.getString("Nombre") ?: "Sin nombre"
                            val apellido = entrada.getString("Apellido") ?: "Sin apellido"
                            val horaEntrada = entrada.getTimestamp("Hora")?.toDate()
                            val horaEntradaStr = horaEntrada?.let { sdf.format(it) } ?: "No registrada"

                            val salida = mapaSalidas[clave]
                            val horaSalida = salida?.getTimestamp("HoraSalida")?.toDate()
                            val horaSalidaStr = horaSalida?.let { sdf.format(it) } ?: "No registrada"

                            lista.append("Fecha: $fechaId\n")
                            lista.append("Nombre: $nombre $apellido\n")
                            lista.append("Clave: $clave\n")
                            lista.append("Entrada: $horaEntradaStr\n")
                            lista.append("Salida: $horaSalidaStr\n\n")
                        }

                        fechasProcesadas++
                        if (fechasProcesadas == fechasTotales) {
                            asistenciasText.text = lista.toString()
                        }
                    }
                }
            }
        }
    }
}
