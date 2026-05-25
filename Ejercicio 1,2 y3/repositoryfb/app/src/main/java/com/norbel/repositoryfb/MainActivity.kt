package com.norbel.repositoryfb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var etNombre           : EditText
    private lateinit var spinCarrera         : Spinner
    private lateinit var spinCurso           : Spinner
    private lateinit var btnCrear            : Button
    private lateinit var btnActualizar       : Button
    private lateinit var btnEliminar         : Button
    private lateinit var recyclerEstudiantes : RecyclerView
    private lateinit var adapter             : EstudianteAdapter
    private lateinit var estudiantesRef      : DatabaseReference

    // ID del registro actualmente seleccionado (null = ninguno)
    private var idSeleccionado: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        estudiantesRef = FirebaseDatabase.getInstance().getReference("Estudiantes")

        etNombre           = findViewById(R.id.etNombre)
        spinCarrera        = findViewById(R.id.spinCarrera)
        spinCurso          = findViewById(R.id.spinCurso)
        btnCrear           = findViewById(R.id.btnCrear)
        btnActualizar      = findViewById(R.id.btnActualizar)
        btnEliminar        = findViewById(R.id.btnEliminar)
        recyclerEstudiantes = findViewById(R.id.recyclerEstudiantes)

        // Adapter con callback: al tocar un ítem se carga en el formulario
        adapter = EstudianteAdapter(mutableListOf()) { estudiante ->
            cargarEnFormulario(estudiante)
        }
        recyclerEstudiantes.layoutManager = LinearLayoutManager(this)
        recyclerEstudiantes.adapter       = adapter

        btnCrear.setOnClickListener      { crear() }
        btnActualizar.setOnClickListener  { actualizar() }
        btnEliminar.setOnClickListener    { eliminar() }

        escucharEstudiantes()
    }

    // CREATE
    private fun crear() {
        val nombre  = etNombre.text.toString().trim()
        val carrera = spinCarrera.selectedItem.toString()
        val curso   = spinCurso.selectedItem.toString()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe el nombre", Toast.LENGTH_SHORT).show()
            return
        }

        val id = estudiantesRef.push().key ?: return
        val estudiante = Estudiante(id, nombre, carrera, curso)

        estudiantesRef.child(id).setValue(estudiante)
            .addOnSuccessListener {
                Toast.makeText(this, "Creado ✓", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al crear: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    //READ
    //se activa automáticamente; actualiza el RecyclerView en tiempo real
    private fun escucharEstudiantes() {
        estudiantesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Estudiante>()
                for (child in snapshot.children) {
                    child.getValue(Estudiante::class.java)?.let { lista.add(it) }
                }
                adapter.actualizar(lista)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,
                    "Error al leer: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    //UPDATE
    private fun actualizar() {
        val id = idSeleccionado
        if (id == null) {
            Toast.makeText(this, "Selecciona un estudiante de la lista",
                Toast.LENGTH_SHORT).show()
            return
        }

        val nombre  = etNombre.text.toString().trim()
        val carrera = spinCarrera.selectedItem.toString()
        val curso   = spinCurso.selectedItem.toString()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe el nombre", Toast.LENGTH_SHORT).show()
            return
        }

        // updateChildren solo actualiza los campos indicados sin borrar el resto
        val cambios = mapOf<String, Any>(
            "nombre"  to nombre,
            "carrera" to carrera,
            "curso"   to curso
        )

        estudiantesRef.child(id).updateChildren(cambios)
            .addOnSuccessListener {
                Toast.makeText(this, "Actualizado ✓", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
    }

    //DELETE
    private fun eliminar() {
        val id = idSeleccionado
        if (id == null) {
            Toast.makeText(this, "Selecciona un estudiante de la lista",
                Toast.LENGTH_SHORT).show()
            return
        }

        estudiantesRef.child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Eliminado ✓", Toast.LENGTH_SHORT).show()
                limpiarFormulario()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
    }

    // Carga un registro en el formulario para editarlo / eliminarlo
    private fun cargarEnFormulario(e: Estudiante) {
        idSeleccionado = e.estudianteid
        etNombre.setText(e.nombre)

        // Posicionar Spinners en la opción correcta
        val carreras = resources.getStringArray(R.array.carreras)
        spinCarrera.setSelection(carreras.indexOf(e.carrera).coerceAtLeast(0))

        val cursos = resources.getStringArray(R.array.cursos)
        spinCurso.setSelection(cursos.indexOf(e.curso).coerceAtLeast(0))

        Toast.makeText(this, "Seleccionado: ${e.nombre}", Toast.LENGTH_SHORT).show()
    }

    private fun limpiarFormulario() {
        idSeleccionado = null
        etNombre.text.clear()
        spinCarrera.setSelection(0)
        spinCurso.setSelection(0)
    }
}
