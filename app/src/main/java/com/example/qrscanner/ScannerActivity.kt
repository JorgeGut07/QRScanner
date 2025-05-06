package com.example.qrscanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.text.SimpleDateFormat
import java.util.*

class ScannerActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var txtResult: TextView
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        barcodeView = findViewById(R.id.barcode_scanner)
        txtResult = findViewById(R.id.txtResult)

        barcodeView.decodeContinuous(callback)
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            barcodeView.pause()
            val qrData = result.text
            txtResult.text = "Escaneado: $qrData"
            procesarQR(qrData)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    private fun procesarQR(qrText: String) {
        try {
            val claveEscaneada = qrText.removePrefix("clave:").trim()
            val fechaActual = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val horaActual = Timestamp.now()
            val docRef = db.collection("asistencias").document(fechaActual)

            docRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    docRef.set(hashMapOf<String, Any>())
                }

                db.collection("usuarios").get()
                    .addOnSuccessListener { result ->
                        var usuarioEncontrado: DocumentSnapshot? = null

                        for (doc in result.documents) {
                            val claveBD = doc.getString("clave") ?: continue
                            if (claveBD.equals(claveEscaneada, ignoreCase = true)) {
                                usuarioEncontrado = doc
                                break
                            }
                        }

                        if (usuarioEncontrado != null) {
                            val nombre = usuarioEncontrado.getString("nombre") ?: ""
                            val apellido = usuarioEncontrado.getString("apellido") ?: ""

                            docRef.collection("Entradas")
                                .whereEqualTo("Clave", claveEscaneada)
                                .get()
                                .addOnSuccessListener { entradaDocs ->
                                    if (entradaDocs.isEmpty) {
                                        // ENTRADA
                                        val entrada = hashMapOf(
                                            "Clave" to claveEscaneada,
                                            "Nombre" to nombre,
                                            "Apellido" to apellido,
                                            "Hora" to horaActual
                                        )
                                        docRef.collection("Entradas").add(entrada)
                                        txtResult.text = "Entrada registrada: $nombre $apellido"
                                    } else {
                                        // Verificar si ya tiene salida
                                        docRef.collection("Salidas")
                                            .whereEqualTo("Clave", claveEscaneada)
                                            .get()
                                            .addOnSuccessListener { salidaDocs ->
                                                if (salidaDocs.isEmpty) {
                                                    val salida = hashMapOf(
                                                        "Clave" to claveEscaneada,
                                                        "Nombre" to nombre,
                                                        "Apellido" to apellido,
                                                        "HoraSalida" to horaActual
                                                    )
                                                    docRef.collection("Salidas").add(salida)
                                                    txtResult.text = "Salida registrada: $nombre $apellido"
                                                } else {
                                                    txtResult.text = "Ya se ha registrado la salida de $nombre $apellido"
                                                }
                                            }
                                    }
                                }
                        } else {
                            txtResult.text = "Usuario no encontrado con clave: $claveEscaneada"
                        }
                    }
            }
        } catch (e: Exception) {
            txtResult.text = "Error al procesar QR: ${e.message}"
        } finally {
            Handler(Looper.getMainLooper()).postDelayed({
                barcodeView.resume()
            }, 2000)
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}