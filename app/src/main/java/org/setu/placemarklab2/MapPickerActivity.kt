package org.setu.placemarklab2

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import timber.log.Timber

class MapPickerActivity : AppCompatActivity() {

    private lateinit var map: MapView

    // the spot I tapped on the map, the marker sits on it
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null
    private var selectedTitle = "Selected location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid needs a user agent or the tile server can block the app
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map_picker)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // starts in Waterford unless a location gets passed in
        val lat = intent.getDoubleExtra("latitude", 52.245954)
        val lon = intent.getDoubleExtra("longitude", -7.139539)
        val start = GeoPoint(lat, lon)

        // title of the mark being added or edited, shown on the selected marker
        selectedTitle = intent.getStringExtra("title") ?: "Selected location"

        Timber.d("MapPickerActivity opened at lat=$lat, lon=$lon, title=$selectedTitle")

        map.controller.setZoom(18.0)
        map.controller.setCenter(start)

        // a tap or a long press moves the selected point
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                setSelectedPoint(point)
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean {
                setSelectedPoint(point)
                return true
            }
        }

        // this goes in first so it sits underneath the markers, otherwise it eats the taps
        // and tapping a marker never shows its title
        map.overlays.add(MapEventsOverlay(receiver))

        // saved marks next so the selected marker is drawn on top of them
        showExistingMarkers()
        setSelectedPoint(start)

        findViewById<Button>(R.id.confirmLocationButton).setOnClickListener {
            val point = selectedPoint ?: return@setOnClickListener

            Timber.d("Location confirmed lat=${point.latitude}, lon=${point.longitude}")

            // send the point back to AddEditActivity
            val result = intent
                .putExtra("latitude", point.latitude)
                .putExtra("longitude", point.longitude)

            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    private fun setSelectedPoint(point: GeoPoint) {
        selectedPoint = point

        // only make the marker the first time, after that I just move it
        val current = marker ?: Marker(map).apply {
            // a different icon to the saved marks (their pins) so I can tell which one is the new spot
            // tinted red because the plain icon is hard to see on the map
            icon = ContextCompat.getDrawable(this@MapPickerActivity, android.R.drawable.ic_menu_mylocation)
                ?.mutate()
                ?.also { it.setTint(Color.RED) }
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = selectedTitle
            map.overlays.add(this)
        }
        marker = current

        current.position = point
        map.invalidate()
    }

    // draws a marker for every mark that is already saved
    private fun showExistingMarkers() {
        val placemarks = AppData.placedMarks.findAll()

        placemarks.forEach { placemark ->
            // x is the latitude and y is the longitude
            val point = GeoPoint(placemark.x, placemark.y)

            val existing = Marker(map).apply {
                position = point
                title = placemark.title
                snippet = placemark.desc
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            map.overlays.add(existing)
        }

        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        map.onPause()
        super.onPause()
    }
}
