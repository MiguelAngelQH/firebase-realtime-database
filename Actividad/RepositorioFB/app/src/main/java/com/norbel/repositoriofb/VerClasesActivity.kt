package com.norbel.repositoriofb

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class VerClasesActivity : AppCompatActivity() {

    private lateinit var listaClases: ListView
    private lateinit var clasesRef: DatabaseReference

    private val lista = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_clases)

        listaClases = findViewById(R.id.listaClases)

        clasesRef = FirebaseDatabase
            .getInstance()
            .getReference("Clases")
            .child("Lecciones")

        obtenerClases()
    }

    private fun obtenerClases() {

        clasesRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                lista.clear()

                for (dato in snapshot.children) {

                    val clase = dato.getValue(Clase::class.java)

                    if (clase != null) {

                        val texto = """
                            Sección: ${clase.seccion}
                            Área: ${clase.area}
                            Tema: ${clase.tema}
                        """.trimIndent()

                        lista.add(texto)
                    }
                }

                val adapter = ArrayAdapter(
                    this@VerClasesActivity,
                    android.R.layout.simple_list_item_1,
                    lista
                )

                listaClases.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}