package com.example.qrscanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.qrscanner.ScannerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIrEscaner = findViewById<Button>(R.id.btnIrEscaner)
        val btnVerAsistencias = findViewById<Button>(R.id.btnVerAsistencias)
        val btnIrCrud = findViewById<Button>(R.id.btnIrCrud)

        btnIrEscaner.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        btnVerAsistencias.setOnClickListener {
            startActivity(Intent(this, AsistenciasActivity::class.java))
        }

        btnIrCrud.setOnClickListener {
            startActivity(Intent(this, Crud::class.java))
        }
    }
}
