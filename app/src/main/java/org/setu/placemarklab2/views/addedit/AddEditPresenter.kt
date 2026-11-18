package org.setu.placemarklab2.views.addedit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.setu.placemarklab2.AppData
import org.setu.placemarklab2.views.mappicker.MapPickerView
import org.setu.placemarklab2.PlacedMark
import timber.log.Timber

// all the decisions for the add/edit screen live here, the view just passes on what I pressed
// and shows whatever the presenter tells it to
class AddEditPresenter(private val view: AddEditView) {

    // null when I'm adding a new mark, the id of the mark when I'm editing one
    private var editingId: Long? = null

    // the photo I picked, null until I pick one
    private var imageUri: Uri? = null

    private lateinit var imageIntentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    init {
        editingId =
            view.intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            Timber.d("AddEditView opened to edit mark id=$editingId")

            loadExistingMark(editingId!!)

        } else {

            Timber.d("AddEditView opened to add a new mark")
        }

        registerImagePickerCallback()
        registerMapCallback()
    }

    // called by the view when Save is pressed, works out if it's a new mark or an edit
    fun doAddOrSave(title: String, description: String, xText: String, yText: String) {

        val cleanTitle = title.trim()
        val cleanDescription = description.trim()

        if (cleanTitle.isEmpty()) {
            Timber.d("Save blocked: title is empty")
            view.showTitleError("Title is required")
            return
        }

        val x = xText.toDoubleOrNull()

        if (x == null) {
            Timber.d("Save blocked: x is not a valid number")
            view.showXError("Enter a valid number")
            return
        }

        val y = yText.toDoubleOrNull()

        if (y == null) {
            Timber.d("Save blocked: y is not a valid number")
            view.showYError("Enter a valid number")
            return
        }

        if (editingId == null) {

            val mark = PlacedMark(
                title = cleanTitle,
                desc = cleanDescription,
                x = x,
                y = y,
                imageUri = imageUri?.toString()
            )

            Timber.d("Creating mark title=${mark.title}")

            // validate() throws if something is out of range, so show that message and stay on the form
            try {
                AppData.placedMarks.create(mark)
            } catch (e: IllegalArgumentException) {
                showValidationError(e)
                return
            }

            view.showMessage("Mark created")

        } else {

            val mark = PlacedMark(
                id = editingId!!,
                title = cleanTitle,
                desc = cleanDescription,
                x = x,
                y = y,
                imageUri = imageUri?.toString()
            )

            Timber.d("Updating mark id=${mark.id}, title=${mark.title}")

            try {
                AppData.placedMarks.update(mark)
            } catch (e: IllegalArgumentException) {
                showValidationError(e)
                return
            }

            view.showMessage("Mark updated")
        }

        view.finish()
    }

    fun doCancel() {
        Timber.d("Add/edit cancelled")
        view.finish()
    }

    fun doSelectImage() {
        imageIntentLauncher.launch(arrayOf("image/*"))
    }

    // opens the map, the view gives me whatever is typed in the boxes so the map can start there
    fun doSetLocation(title: String, xText: String, yText: String) {

        Timber.d("Pick Location pressed")

        val intent = Intent(view, MapPickerView::class.java)

        // start the map where this mark already is (or wherever I typed), instead of always Waterford
        // only passed when both boxes are real coordinates, otherwise the map uses its default
        val lat = xText.toDoubleOrNull()
        val lon = yText.toDoubleOrNull()

        if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
            intent.putExtra("latitude", lat)
            intent.putExtra("longitude", lon)
        }

        // the title shows up on the selected marker
        val cleanTitle = title.trim()
        if (cleanTitle.isNotEmpty()) {
            intent.putExtra("title", cleanTitle)
        }

        mapIntentLauncher.launch(intent)
    }

    private fun loadExistingMark(id: Long) {

        val mark =
            AppData.placedMarks.findOne(id)

        if (mark == null) {

            Timber.w("Tried to edit mark id=$id but it was not found")

            view.showMessage("Mark not found")
            view.finish()
            return
        }

        imageUri = mark.imageUri?.let { Uri.parse(it) }

        view.showMark(mark)
    }

    private fun showValidationError(e: IllegalArgumentException) {
        Timber.d("Save blocked: ${e.message}")

        view.showMessage(e.message ?: "Please check the entered values.", long = true)
    }

    // opens the phone's file picker, uri comes back null if I cancel
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                if (uri != null) {
                    // ask to keep access to the photo so it still loads after the app restarts
                    try {
                        view.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                        Timber.w("Could not keep access to $uri")
                    }

                    Timber.d("Picked image $uri")

                    imageUri = uri
                    view.showImage(uri)
                } else {
                    Timber.d("Image picker cancelled")
                }
            }
    }

    // when I confirm a spot on the map the view fills in the x and y boxes
    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data ?: return@registerForActivityResult

                    val latitude = data.getDoubleExtra("latitude", 0.0)
                    val longitude = data.getDoubleExtra("longitude", 0.0)

                    Timber.d("Map location picked lat=$latitude, lon=$longitude")

                    view.showLocation(latitude, longitude)
                } else {
                    Timber.d("Map picker cancelled")
                }
            }
    }
}
