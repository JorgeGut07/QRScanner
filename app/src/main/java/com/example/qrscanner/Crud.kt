package com.example.qrscanner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Crud : AppCompatActivity() {

    private lateinit var tablaEmpleados: TableLayout
    private lateinit var btnAdd: Button
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        tablaEmpleados = findViewById(R.id.tablaEmpleados)
    }

    private fun cargarEmpleadosDesdeFirestore() {
        tablaEmpleados.removeViews(1, tablaEmpleados.childCount - 1) // Borra todas las filas excepto el encabezado

        db.collection("usuarios").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.getString("nombre") ?: ""
                    val apellidos = document.getString("apellidos") ?: ""
                    val clave = document.getString("clave") ?: ""
                    val id = document.id
                    agregarEmpleado(nombre, apellidos, clave, id)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar empleados", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarEmpleadosDesdeFirestore()
    }

    private fun agregarEmpleado(nombre: String, apellidos: String, clave: String, id: String) {
        val fila = TableRow(this)

        fila.addView(createTextView(nombre))
        fila.addView(createTextView(apellidos))
        fila.addView(createTextView(clave))

        val btnEditar = ImageButton(this).apply {
            setImageResource(R.drawable.edit)
            setBackgroundResource(0)
            setColorFilter(Color.BLACK)
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                // Leer el documento completo para obtener el género
                db.collection("usuarios").document(id).get()
                    .addOnSuccessListener { document ->
                        val genero = document.getString("genero") ?: ""
                        val intent = Intent(this@Crud, EditarEmpleadoActivity::class.java).apply {
                            putExtra("id", id)
                            putExtra("nombre", nombre)
                            putExtra("apellidos", apellidos)
                            putExtra("clave", clave)
                            putExtra("genero", genero) // Agregado
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@Crud, "Error al obtener el género", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        fila.addView(btnEditar)

        val btnEliminar = ImageButton(this).apply {
            setImageResource(R.drawable.baseline_delete_24)
            setBackgroundResource(0)
            setColorFilter(Color.BLACK)
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                db.collection("usuarios").document(id).delete()
                    .addOnSuccessListener {
                        tablaEmpleados.removeView(fila)
                        Toast.makeText(this@Crud, "Empleado eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@Crud, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        fila.addView(btnEliminar)

        tablaEmpleados.addView(fila)
    }


    private fun eliminarEmpleadoDeFirestore(id: String, fila: TableRow) {
        db.collection("usuarios").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("usuarios").document(id)
                        .delete()
                        .addOnSuccessListener {
                            tablaEmpleados.removeView(fila)
                            Toast.makeText(this, "Empleado eliminado de la base de datos", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "El documento no existe en la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar existencia del documento: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(8, 8, 8, 8)
        }
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
        }
    }
}
