package org.setu.placemarklab2.views.maplist

import android.content.Intent
import org.setu.placemarklab2.AppData
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.views.addedit.AddEditView
import timber.log.Timber

// decides what the map screen shows, which mark is selected and what the buttons do
// the view only draws the map and fills in the panel under it
class MapListPresenter(private val view: MapListView) {

    // the mark whose details are in the panel, null if none is selected
    private var selectedId: Long? = null

    // the map only zooms to fit everything the first time, so coming back from
    // editing doesn't undo wherever I had scrolled to
    private var zoomedToMarks = false

    // the view calls this every time the screen comes back, so an edit shows up on the map
    fun doLoadMarks() {

        val placemarks = AppData.placedMarks.findAll()

        Timber.d("Showing ${placemarks.size} marks on the map")

        view.showMarks(placemarks)

        if (placemarks.isEmpty()) {

            // nothing to show, so say why instead of leaving an empty map
            Timber.d("No marks to show on the map")

            selectedId = null
            view.showEmptyMessage()

        } else {

            if (!zoomedToMarks) {
                view.zoomToMarks(placemarks)
                zoomedToMarks = true
            }

            refreshPanel()
        }
    }

    // a marker on the map was tapped
    fun doSelectMark(placemark: PlacedMark) {

        Timber.d("Marker selected id=${placemark.id}, title=${placemark.title}")

        selectedId = placemark.id
        view.showPlacemark(placemark)
    }

    fun doEditSelected() {

        val id = selectedId ?: return

        Timber.d("Editing mark id=$id from the map")

        val intent =
            Intent(view, AddEditView::class.java)

        intent.putExtra("id", id)

        view.startActivity(intent)
    }

    fun doReturnToMenu() {

        Timber.d("Back to menu pressed")

        // the menu is still underneath this screen so just closing goes back to it
        view.finish()
    }

    // after coming back from editing, show the new details for the mark that was selected
    // (or reset the panel if that mark isn't there any more)
    private fun refreshPanel() {

        val current = selectedId?.let { AppData.placedMarks.findOne(it) }

        if (current != null) {
            view.showPlacemark(current)
        } else {
            selectedId = null
            view.showNoSelection()
        }
    }
}
