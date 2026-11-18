package org.setu.placemarklab2.views.mappicker

import android.app.Activity
import org.osmdroid.util.GeoPoint
import org.setu.placemarklab2.AppData
import timber.log.Timber

// works out where the map starts, which marks to show and what happens when I tap or confirm
// the view only draws the map and the markers it is told about
class MapPickerPresenter(private val view: MapPickerView) {

    // the spot I tapped on the map, the red target sits on it
    private var selectedPoint: GeoPoint? = null

    // title of the mark being added or edited, shown on the selected marker
    private val selectedTitle: String

    init {
        // starts in Waterford unless a location gets passed in
        val lat = view.intent.getDoubleExtra("latitude", 52.245954)
        val lon = view.intent.getDoubleExtra("longitude", -7.139539)
        val start = GeoPoint(lat, lon)

        selectedTitle = view.intent.getStringExtra("title") ?: "Selected location"

        Timber.d("MapPickerView opened at lat=$lat, lon=$lon, title=$selectedTitle")

        view.showMap(start, 18.0)

        // saved marks first so the selected marker is drawn on top of them
        view.showExistingMarks(AppData.placedMarks.findAll())

        doSelectPoint(start)
    }

    // a tap or a long press on the map moves the selected point
    fun doSelectPoint(point: GeoPoint) {
        selectedPoint = point
        view.showSelectedPoint(point, selectedTitle)
    }

    // sends the chosen point back to the add/edit screen
    fun doConfirmLocation() {
        val point = selectedPoint ?: return

        Timber.d("Location confirmed lat=${point.latitude}, lon=${point.longitude}")

        val result = view.intent
            .putExtra("latitude", point.latitude)
            .putExtra("longitude", point.longitude)

        view.setResult(Activity.RESULT_OK, result)
        view.finish()
    }
}
