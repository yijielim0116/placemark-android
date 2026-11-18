package org.setu.placemarklab2.views.marklist

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.R
import timber.log.Timber

// shows the list and passes taps on to the presenter, it is also the listener for the list items
class MarkListView : AppCompatActivity(), OnMarkListener {

    private lateinit var presenter: MarkListPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlacedMarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_mark_list)

        Timber.d("MarkListView created")

        presenter = MarkListPresenter(this)

        recyclerView =
            findViewById(R.id.marksRecyclerView)

        // starts empty, onResume asks the presenter for the marks
        adapter = PlacedMarkAdapter(
            marks = emptyList(),
            listener = this
        )

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        findViewById<Button>(R.id.returnButton).setOnClickListener {
            presenter.doReturnToMenu()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.doLoadMarks()
    }

    // tap on an item
    override fun onMarkClick(mark: PlacedMark) {
        presenter.doEditMark(mark)
    }

    // long press on an item
    override fun onMarkLongClick(mark: PlacedMark): Boolean {
        presenter.doAskToDelete(mark)
        return true
    }

    fun showMarks(marks: List<PlacedMark>) {
        adapter.updateMarks(marks)
    }

    fun showDeleteDialog(mark: PlacedMark) {
        AlertDialog.Builder(this)
            .setTitle("Delete this mark?")
            .setMessage("\"${mark.title}\" will be removed.")
            .setPositiveButton("Delete") { _, _ ->
                presenter.doDeleteMark(mark)
            }
            .setNegativeButton("Cancel") { _, _ ->
                presenter.doCancelDelete(mark)
            }
            .show()
    }
}
