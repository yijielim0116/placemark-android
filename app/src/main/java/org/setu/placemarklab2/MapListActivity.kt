package org.setu.placemarklab2

import android.content.Intent
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
import timber.log.Timber

class MapListActivity : AppCompatActivity() {

    private lateinit var mapView: MapView

    private lateinit var txtTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var imgPlace: ImageView
    private lateinit var editButton: Button

    // the mark whose details are in the panel, null if none is selected
    private var selectedId: Long? = null

    // the map only zooms to fit everything the first time, so coming back from
    // editing doesn't undo wherever I had scrolled to
    private var zoomedToMarks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid needs a user agent or the tile server can block the app
        // (this screen can be opened without ever opening the map picker first)
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map_list)

        Timber.d("MapListActivity created")

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
            Timber.d("Back to menu pressed")
            // the menu is still underneath this screen so just closing goes back to it
            finish()
        }

        // nothing to edit until a marker has been tapped
        editButton.isEnabled = false

        editButton.setOnClickListener {
            selectedId?.let { editMark(it) }
        }
    }

    // reloads the markers every time the screen comes back, so an edit shows up on the map
    private fun loadMarkers() {

        val placemarks = AppData.placedMarks.findAll()

        Timber.d("Showing ${placemarks.size} marks on the map")

        // take the old markers off first, but keep the copyright overlay
        mapView.overlays.removeAll { it is Marker }

        placemarks.forEach { placemark ->

            // x is the latitude and y is the longitude
            val position = GeoPoint(
                placemark.x,
                placemark.y
            )

            val marker = Marker(mapView)

            marker.position = position
            marker.title = placemark.title
            marker.snippet = placemark.desc

            marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
            )

            // Handle marker selection
            marker.setOnMarkerClickListener { _, _ ->

                Timber.d("Marker selected id=${placemark.id}, title=${placemark.title}")

                displayPlace(placemark)

                // Return true to consume the click
                true
            }

            mapView.overlays.add(marker)
        }

        if (placemarks.isEmpty()) {

            // nothing to show, so say why instead of leaving an empty map
            showEmptyMessage()

        } else {

            if (!zoomedToMarks) {
                zoomToMarks(placemarks)
                zoomedToMarks = true
            }

            refreshPanel()
        }

        mapView.invalidate()
    }

    // zooms so every marker fits on screen
    private fun zoomToMarks(placemarks: List<PlacedMark>) {

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
    private fun displayPlace(placemark: PlacedMark) {

        selectedId = placemark.id
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

    // after coming back from editing, show the new details for the mark that was selected
    // (or reset the panel if that mark isn't there any more)
    private fun refreshPanel() {

        val current = selectedId?.let { AppData.placedMarks.findOne(it) }

        if (current != null) {
            displayPlace(current)
        } else {
            selectedId = null
            editButton.isEnabled = false
            txtTitle.text = "No mark selected"
            txtDescription.text = "Tap a marker on the map to see its details."
            imgPlace.setImageDrawable(null)
        }
    }

    private fun showEmptyMessage() {

        Timber.d("No marks to show on the map")

        selectedId = null
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

    private fun editMark(id: Long) {

        Timber.d("Editing mark id=$id from the map")

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", id)

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        loadMarkers()
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
