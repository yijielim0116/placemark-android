package org.setu.placemarklab2.views.maplist

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.R
import timber.log.Timber

// draws the map and the panel under it, and passes taps and button presses on to the presenter
class MapListView : AppCompatActivity() {

    private lateinit var presenter: MapListPresenter

    private lateinit var mapView: MapView

    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var imgPlace: ImageView
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid needs a user agent or the tile server can block the app
        // (this screen can be opened without ever opening the map picker first)
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map_list)

        Timber.d("MapListView created")

        presenter = MapListPresenter(this)

        // Initialise views
        mapView = findViewById(R.id.mapList)
        txtTitle = findViewById(R.id.mapTitleText)
        txtDescription = findViewById(R.id.mapDescText)
        imgPlace = findViewById(R.id.mapImageView)
        editButton = findViewById(R.id.mapEditButton)

        // Configure OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Show map attribution
        mapView.overlays.add(CopyrightOverlay(this))

        findViewById<Button>(R.id.mapBackButton).setOnClickListener {
            presenter.doReturnToMenu()
        }

        // nothing to edit until a marker has been tapped
        editButton.isEnabled = false

        editButton.setOnClickListener {
            presenter.doEditSelected()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        presenter.doLoadMarks()
    }

    // draws a marker for every mark, taking the old ones off first but keeping the copyright overlay
    fun showMarks(placemarks: List<PlacedMark>) {

        mapView.overlays.removeAll { it is Marker }

        placemarks.forEach { placemark ->

            // x is the latitude and y is the longitude
            val marker = Marker(mapView)

            marker.position = GeoPoint(
                placemark.x,
                placemark.y
            )
            marker.title = placemark.title
            marker.snippet = placemark.desc

            marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )

            // Handle marker selection
            marker.setOnMarkerClickListener { _, _ ->

                presenter.doSelectMark(placemark)

                // Return true to consume the click
                true
            }

            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    // zooms so every marker fits on screen
    fun zoomToMarks(placemarks: List<PlacedMark>) {

        val box = BoundingBox.fromGeoPoints(
            placemarks.map { GeoPoint(it.x, it.y) }
        )

        // one mark (or marks on the same spot) has no size to fit, so just centre on it
        if (box.latitudeSpan == 0.0 && box.longitudeSpan == 0.0) {

            mapView.controller.setZoom(15.0)
            mapView.controller.setCenter(box.centerWithDateLine)
            return
        }

        // the map has no size until it has been laid out, so wait for that before zooming
        if (mapView.isLayoutOccurred) {
            mapView.zoomToBoundingBox(box, false, 100)
        } else {
            mapView.addOnFirstLayoutListener { _, _, _, _, _ ->
                mapView.zoomToBoundingBox(box, false, 100)
            }
        }
    }

    // fills the panel under the map with the mark I tapped
    fun showPlacemark(placemark: PlacedMark) {

        editButton.isEnabled = true

        txtTitle.text = placemark.title

        txtDescription.text = placemark.desc

        if (placemark.imageUri != null) {
            imgPlace.setImageURI(
                Uri.parse(placemark.imageUri)
            )
        } else {
            imgPlace.setImageDrawable(null)
        }
    }

    // resets the panel when no mark is selected
    fun showNoSelection() {

        editButton.isEnabled = false

        txtTitle.text = "No mark selected"
        txtDescription.text = "Tap a marker on the map to see its details."
        imgPlace.setImageDrawable(null)
    }

    fun showEmptyMessage() {

        editButton.isEnabled = false

        txtTitle.text = "No marks yet"
        txtDescription.text = "Add a mark from the main menu and it will show up here."
        imgPlace.setImageDrawable(null)

        Toast.makeText(
            this,
            "There are no marks to show yet",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        mapView.onDetach()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()

        mapView.tileProvider.clearTileCache()
        mapView.invalidate()
    }
}
