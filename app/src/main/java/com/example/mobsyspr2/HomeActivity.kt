package com.example.mobsyspr2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobsyspr2.databinding.ActivityHomeBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.gson.Gson
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityHomeBinding
    private lateinit var itemsAdapter: ItemsAdapter
    private val itemsList = mutableListOf<Item>()
    private var isProcessing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d("Firebase", "Firebase-App wurde initialisiert.")
        }

        binding.recyclerViewItems.layoutManager = LinearLayoutManager(this)

        itemsAdapter = ItemsAdapter(itemsList) { success ->
            if (success) {
                Log.d("HomeActivity", "Item wurde erfolgreich gelöscht.")
            } else {
                Log.e("HomeActivity", "Fehler beim Löschen des Items.")
            }
        }
        binding.recyclerViewItems.adapter = itemsAdapter



        database = FirebaseDatabase.getInstance().getReference("Einkaufsliste")

        fetchItems()

        binding.btnHinzufuegen.setOnClickListener {
            val produkt = binding.etProdukt.text.toString()
            val menge = binding.etMenge.text.toString().toIntOrNull() ?: 0
            val notiz = binding.etNotiz.text.toString()
            if (produkt.isNotEmpty() && menge > 0) {
                addItemToFirebase(produkt, menge, notiz)
            } else {
                toast("Bitte fülle alle Felder korrekt aus.")
            }
        }
    }
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun fetchItems() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    AdminBoard.incrementCounters(isRead = true)
                    itemsList.clear()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java)
                        item?.let { itemsList.add(it) }
                    }
                    itemsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Fehler beim Laden der Daten: ${error.message}")
            }
        })
    }

    private fun addItemToFirebase(produkt: String, menge: Int, notiz: String) {
        val newItemId = database.push().key ?: return
        val newItem = mapOf(
            "id" to newItemId,
            "id_kategorie" to 1,
            "menge" to menge,
            "notizen" to notiz,
            "produkt" to produkt
        )

        database.child(newItemId)
            .setValue(newItem)
            .addOnSuccessListener {
                AdminBoard.incrementCounters(isRead = false)
                toast("Eintrag erfolgreich hinzugefügt")
            }
            .addOnFailureListener { e ->
                toast("Fehler beim Hinzufügen: ${e.message}")
            }
    }



    private fun backupDatabaseToFile(context: Context) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Einkaufsliste")
        databaseRef.get().addOnSuccessListener { snapshot ->
            val backupData = mutableMapOf<String, Any>()
            for (itemSnapshot in snapshot.children) {
                val itemData = itemSnapshot.value
                if (itemData != null) {
                    backupData[itemSnapshot.key ?: ""] = itemData
                }
            }

            val jsonBackup = Gson().toJson(backupData)
            val fileName = "firebase_backup_${System.currentTimeMillis()}.json"
            val file = File(context.filesDir, fileName)

            try {
                file.writeText(jsonBackup)
                Log.d("Backup", "Backup erfolgreich gespeichert: ${file.absolutePath}")
                toast("Backup erfolgreich unter ${file.absolutePath} gespeichert")
            } catch (e: Exception) {
                Log.e("Backup", "Fehler beim Speichern des Backups: ${e.message}")
                toast("Fehler beim Speichern des Backups")
            }
        }.addOnFailureListener { e ->
            Log.e("Backup", "Fehler beim Erstellen des Backups: ${e.message}")
            toast("Fehler beim Erstellen des Backups")
        }
    }

    override fun onStop() {
        super.onStop()
        backupDatabaseToFile(this)
    }
}
