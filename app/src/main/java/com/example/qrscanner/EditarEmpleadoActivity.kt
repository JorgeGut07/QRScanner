package com.example.qrscanner

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditarEmpleadoActivity : AppCompatActivity() {

    private lateinit var editNombre: EditText
    private lateinit var editApellidos: EditText
    private lateinit var editEdad: EditText
    private lateinit var spinnerGenero: Spinner
    private lateinit var btnGuardar: Button

    private val db = FirebaseFirestore.getInstance()
    private var empleadoId: String? = null
    private var claveOriginal: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_empleado)

        editNombre = findViewById(R.id.editNombre)
        editApellidos = findViewById(R.id.editApellidos)
        editEdad = findViewById(R.id.editEdad)
        spinnerGenero = findViewById(R.id.spinnerGenero)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Configurar el Spinner con opciones "Masculino" y "Femenino"
        val generos = arrayOf("Masculino", "Femenino")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, generos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGenero.adapter = adapter

        // Obtener datos pasados por Intent
        empleadoId = intent.getStringExtra("id")

        if (empleadoId != null) {
            db.collection("usuarios").document(empleadoId!!).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        editNombre.setText(document.getString("nombre") ?: "")
                        editApellidos.setText(document.getString("apellidos") ?: "")

                        // Obtener y convertir la edad
                        val edad = document.get("edad") // Esto puede ser Long, String, o cualquier otro tipo
                        val edadValor = when (edad) {
                            is Long -> edad.toInt()  // Si es Long, convertir a Int
                            is String -> edad.toIntOrNull() ?: 0  // Si es String, convertir a Int o poner 0
                            else -> 0  // Si no es un tipo válido, poner 0
                        }
                        editEdad.setText(edadValor.toString())

                        claveOriginal = document.getString("clave") ?: ""

                        val genero = document.getString("genero") ?: ""
                        val index = generos.indexOfFirst { it.equals(genero, ignoreCase = true) }
                        if (index >= 0) {
                            spinnerGenero.setSelection(index)
                        }
                    }
                }
        }

        btnGuardar.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val apellidos = editApellidos.text.toString().trim()
            val edadStr = editEdad.text.toString().trim()
            val genero = spinnerGenero.selectedItem.toString()

            if (nombre.isEmpty() || apellidos.isEmpty() || edadStr.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val edad = edadStr.toIntOrNull()
            if (edad == null || edad <= 0) {
                Toast.makeText(this, "Edad no válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datosActualizados = mapOf(
                "nombre" to nombre,
                "apellidos" to apellidos,
                "edad" to edad,
                "genero" to genero,
                "clave" to claveOriginal // se conserva sin permitir editar
            )

            empleadoId?.let {
                db.collection("usuarios").document(it).update(datosActualizados)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Empleado actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
