package org.setu.placemarklab2.views.marklist

import android.content.Intent
import org.setu.placemarklab2.AppData
import org.setu.placemarklab2.MainActivity
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.views.addedit.AddEditView
import timber.log.Timber

// decides what happens when I tap, long press or delete in the list
// the view only shows the list and the dialog
class MarkListPresenter(private val view: MarkListView) {

    // reloads the marks from the model and gives them to the view, the view calls this
    // every time it comes back so an edit or a delete shows up
    fun doLoadMarks() {

        val marks = AppData.placedMarks.findAll()

        Timber.d("Refreshing list, ${marks.size} marks")

        view.showMarks(marks)
    }

    // tap on an item opens it for editing
    fun doEditMark(mark: PlacedMark) {

        Timber.d("Editing mark id=${mark.id}, title=${mark.title}")

        val intent =
            Intent(view, AddEditView::class.java)

        intent.putExtra("id", mark.id)

        view.startActivity(intent)
    }

    // long press asks first, so a slip doesn't delete anything
    fun doAskToDelete(mark: PlacedMark) {

        Timber.d("Asking to delete mark id=${mark.id}, title=${mark.title}")

        view.showDeleteDialog(mark)
    }

    fun doCancelDelete(mark: PlacedMark) {
        Timber.d("Delete cancelled for mark id=${mark.id}")
    }

    fun doDeleteMark(mark: PlacedMark) {

        Timber.d("Deleting mark id=${mark.id}, title=${mark.title}")

        AppData.placedMarks.delete(mark.id)

        view.showMarks(
            AppData.placedMarks.findAll()
        )
    }

    fun doReturnToMenu() {

        Timber.d("Back to menu pressed")

        view.startActivity(
            Intent(view, MainActivity::class.java)
        )
    }
}
