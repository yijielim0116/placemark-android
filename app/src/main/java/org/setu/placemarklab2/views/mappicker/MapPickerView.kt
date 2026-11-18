package org.setu.placemarklab2.views.mappicker

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
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.R

// draws the map and the markers the presenter tells it about, and passes taps on to the presenter
class MapPickerView : AppCompatActivity() {

    private lateinit var presenter: MapPickerPresenter
    private lateinit var map: MapView

    // the red target for the spot I tapped
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // osmdroid needs a user agent or the tile server can block the app
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_map_picker)

        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // a tap or a long press moves the selected point
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                presenter.doSelectPoint(point)
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean {
                presenter.doSelectPoint(point)
                return true
            }
        }

        // this goes in first so it sits underneath the markers, otherwise it eats the taps
        // and tapping a marker never shows its title
        map.overlays.add(MapEventsOverlay(receiver))

        // the map has to be set up before this, because the presenter starts drawing straight away
        presenter = MapPickerPresenter(this)

        findViewById<Button>(R.id.confirmLocationButton).setOnClickListener {
            presenter.doConfirmLocation()
        }
    }

    fun showMap(center: GeoPoint, zoom: Double) {
        map.controller.setZoom(zoom)
        map.controller.setCenter(center)
    }

    // draws a marker for every mark that is already saved
    fun showExistingMarks(placemarks: List<PlacedMark>) {

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

    // puts the red target on the spot, making it the first time and moving it after that
    fun showSelectedPoint(point: GeoPoint, title: String) {

        val current = marker ?: Marker(map).apply {
            // a different icon to the saved marks (their pins) so I can tell which one is the new spot
            // tinted red because the plain icon is hard to see on the map
            icon = ContextCompat.getDrawable(this@MapPickerView, android.R.drawable.ic_menu_mylocation)
                ?.mutate()
                ?.also { it.setTint(Color.RED) }
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            this.title = title
            map.overlays.add(this)
        }
        marker = current

        current.position = point
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
