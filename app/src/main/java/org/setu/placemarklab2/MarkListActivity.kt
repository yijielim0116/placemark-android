package org.setu.placemarklab2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class MarkListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlacedMarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_mark_list)

        Timber.d("MarkListActivity created")

        recyclerView =
            findViewById(R.id.marksRecyclerView)

        adapter = PlacedMarkAdapter(
            marks = AppData.placedMarks.findAll(),

            listener = object : OnMarkListener {

                // tap on an item opens it for editing
                override fun onMarkClick(mark: PlacedMark) {
                    editMark(mark)
                }

                // long press asks first, so a slip doesn't delete anything
                override fun onMarkLongClick(mark: PlacedMark): Boolean {
                    confirmDelete(mark)
                    return true
                }
            }
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        val returnButton =
            findViewById<Button>(R.id.returnButton)

        returnButton.setOnClickListener {

            Timber.d("Back to menu pressed")

            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {

            val marks = AppData.placedMarks.findAll()

            Timber.d("Refreshing list, ${marks.size} marks")

            adapter.updateMarks(marks)
        }
    }

    private fun editMark(mark: PlacedMark) {

        Timber.d("Editing mark id=${mark.id}, title=${mark.title}")

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", mark.id)

        startActivity(intent)
    }

    private fun confirmDelete(mark: PlacedMark) {

        Timber.d("Asking to delete mark id=${mark.id}, title=${mark.title}")

        AlertDialog.Builder(this)
            .setTitle("Delete this mark?")
            .setMessage("\"${mark.title}\" will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                deleteMark(mark)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Timber.d("Delete cancelled for mark id=${mark.id}")
            }
            .show()
    }

    private fun deleteMark(mark: PlacedMark) {

        Timber.d("Deleting mark id=${mark.id}, title=${mark.title}")

        AppData.placedMarks.delete(mark.id)

        adapter.updateMarks(
            AppData.placedMarks.findAll()
        )
    }
}
