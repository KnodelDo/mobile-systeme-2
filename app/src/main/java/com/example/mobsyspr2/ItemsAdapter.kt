package com.example.mobsyspr2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class ItemsAdapter(
    private val itemsList: MutableList<Item>,
    private val onItemDeleted: (Boolean) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]


        holder.tvProdukt.text = item.produkt ?: "Unbekannt"
        holder.tvMenge.text = item.menge.toString()
        holder.tvNotiz.text = item.notizen ?: "Keine Notizen"


        holder.btnDelete.setOnClickListener {
            val itemId = item.id
            if (itemId != null && position in itemsList.indices) {
                deleteItemFromFirebase(itemId) { success ->
                    if (success) {
                        if (position in itemsList.indices) {
                            itemsList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, itemsList.size)
                        } else {
                            Log.e("ItemsAdapter", "Ungültige Position nach Löschen: $position")
                        }
                    } else {
                        Log.e("ItemsAdapter", "Fehler beim Löschen des Items.")
                    }
                }
            } else {
                Log.e("ItemsAdapter", "Ungültige Position: $position oder itemId ist null")
            }
        }


    }

    override fun getItemCount(): Int = itemsList.size

    private fun deleteItemFromFirebase(itemId: String, callback: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("Einkaufsliste")
            .child(itemId)
            .removeValue()
            .addOnSuccessListener {
                AdminBoard.incrementWriteCount()
                Log.d("ItemsAdapter", "Item mit ID $itemId erfolgreich gelöscht.")
                callback(true) // Erfolgreich gelöscht
            }
            .addOnFailureListener { e ->
                Log.e("ItemsAdapter", "Fehler beim Löschen des Items: ${e.message}")
                callback(false) // Fehler beim Löschen
            }
    }
    // ViewHolder für die RecyclerView
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProdukt: TextView = itemView.findViewById(R.id.tvProdukt)
        val tvMenge: TextView = itemView.findViewById(R.id.tvMenge)
        val tvNotiz: TextView = itemView.findViewById(R.id.tvNotiz)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }
}
