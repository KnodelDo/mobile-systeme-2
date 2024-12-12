package com.example.mobsyspr2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.*
import com.example.mobsyspr2.databinding.ActivityAdminBoardBinding


class AdminBoard : AppCompatActivity() {

    private lateinit var lineChartReads: LineChart
    private lateinit var lineChartWrites: LineChart
    private val readHistory = mutableListOf<Entry>()
    private val writeHistory = mutableListOf<Entry>()
    private lateinit var binding: ActivityAdminBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lineChartReads = binding.lineChartReads
        lineChartWrites = binding.lineChartWrites

        configureLineChart(lineChartReads, "Lesevorgänge")
        configureLineChart(lineChartWrites, "Schreibvorgänge")

        binding.btnZurueck.setOnClickListener { finish() }
        fetchDatabaseStats()
    }

    companion object {
        private val statsRef = FirebaseDatabase.getInstance().getReference("Stats")
        fun incrementReadCount() {
            statsRef.child("total_reads").runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentValue = currentData.getValue(Long::class.java) ?: 0L
                    currentData.value = (currentValue + 1).toInt()
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error == null && committed) {
                        val newReads = currentData?.getValue(Int::class.java) ?: 0
                        statsRef.child("total_writes").get().addOnSuccessListener { snapshot ->
                            val currentWrites = snapshot.getValue(Int::class.java) ?: 0
                            updateHistory(reads = newReads, writes = currentWrites) // Beide Werte als Int übergeben
                        }
                    } else {
                        Log.e("AdminBoard", "Fehler beim Aktualisieren der Leseanfragen: ${error?.message}")
                    }
                }
            })
        }

        fun incrementWriteCount() {
            statsRef.child("total_writes").runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentValue = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentValue + 1
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error == null && committed) {
                        val newWrites = currentData?.getValue(Int::class.java) ?: 0
                        statsRef.child("total_reads").get().addOnSuccessListener { snapshot ->
                            val currentReads = snapshot.getValue(Int::class.java) ?: 0
                            updateHistory(reads = currentReads, writes = newWrites)
                        }
                    } else {
                        Log.e("AdminBoard", "Fehler beim Aktualisieren der Schreibanfragen: ${error?.message}")
                    }
                }
            })
        }



        private var isUpdatingHistory = false

        private fun updateHistory(reads: Int, writes: Int) {
            val historyRef = statsRef.child("History")
            val indexRef = statsRef.child("HistoryIndex")

            indexRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentIndex = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentIndex + 1
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error == null && committed) {
                        val newIndex = currentData?.getValue(Int::class.java) ?: 0
                        val newEntry = mapOf("reads" to reads, "writes" to writes)

                        historyRef.child(newIndex.toString()).setValue(newEntry)
                            .addOnSuccessListener {
                                Log.d("AdminBoard", "updateHistory: Neuer Eintrag hinzugefügt: reads=$reads, writes=$writes")
                                historyRef.get().addOnSuccessListener { snapshot ->
                                    if (snapshot.childrenCount > 10) {
                                        val oldestEntryKey = snapshot.children.firstOrNull()?.key
                                        oldestEntryKey?.let { key ->
                                            historyRef.child(key).removeValue()
                                                .addOnSuccessListener {
                                                    Log.d("AdminBoard", "Ältester Eintrag erfolgreich entfernt.")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("AdminBoard", "Fehler beim Entfernen des ältesten Eintrags: ${e.message}")
                                                }
                                        }
                                    }
                                }.addOnFailureListener { e ->
                                    Log.e("AdminBoard", "Fehler beim Abrufen der Historie: ${e.message}")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("AdminBoard", "Fehler beim Hinzufügen des neuen Eintrags: ${e.message}")
                            }

                    } else {
                        Log.e("AdminBoard", "Fehler beim Aktualisieren des Index: ${error?.message}")
                    }
                }
            })
        }




    }

    private fun configureLineChart(chart: LineChart, label: String) {
        chart.description.isEnabled = true
        chart.description.text = label
        chart.setTouchEnabled(true)
        chart.setPinchZoom(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        chart.axisRight.isEnabled = false
    }

    private fun fetchDatabaseStats() {
        val statsRef = FirebaseDatabase.getInstance().getReference("Stats/History")


        statsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                readHistory.clear()
                writeHistory.clear()

                var index = 0f
                for (data in snapshot.children) {
                    val reads = data.child("reads").getValue(Int::class.java) ?: 0
                    val writes = data.child("writes").getValue(Int::class.java) ?: 0
                    readHistory.add(Entry(index, reads.toFloat()))
                    writeHistory.add(Entry(index, writes.toFloat()))
                    index++
                }

                updateLineChart(lineChartReads, readHistory, "Lesevorgänge")
                updateLineChart(lineChartWrites, writeHistory, "Schreibvorgänge")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchStats", "Fehler beim Abrufen der Historie: ${error.message}")
            }
        })
    }



    private fun updateLineChart(chart: LineChart, history: List<Entry>, label: String) {
        if (history.isNotEmpty()) {
            val dataSet = LineDataSet(history, label)
            dataSet.color = resources.getColor(android.R.color.holo_blue_light, null)
            dataSet.valueTextSize = 10f
            dataSet.lineWidth = 2f

            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.invalidate()
        }
    }
}
