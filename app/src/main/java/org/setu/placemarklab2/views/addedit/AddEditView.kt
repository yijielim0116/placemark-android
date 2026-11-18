package org.setu.placemarklab2.views.addedit

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.setu.placemarklab2.PlacedMark
import org.setu.placemarklab2.R

// the screen only sets up the controls and passes what I press on to the presenter
// the presenter decides what to do and tells this class what to show
class AddEditView : AppCompatActivity() {

    private lateinit var presenter: AddEditPresenter

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    private lateinit var imagePreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        titleInput =
            findViewById(R.id.titleInput)

        descriptionInput =
            findViewById(R.id.descriptionInput)

        xInput =
            findViewById(R.id.xInput)

        yInput =
            findViewById(R.id.yInput)

        imagePreview =
            findViewById(R.id.imageViewAdd)

        // the controls have to exist before this, because the presenter can ask the view
        // to show an existing mark straight away when it is created
        presenter = AddEditPresenter(this)

        findViewById<Button>(R.id.imagePickButton).setOnClickListener {
            presenter.doSelectImage()
        }

        findViewById<Button>(R.id.buttonLoc).setOnClickListener {
            presenter.doSetLocation(
                titleInput.text.toString(),
                xInput.text.toString(),
                yInput.text.toString()
            )
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            presenter.doAddOrSave(
                titleInput.text.toString(),
                descriptionInput.text.toString(),
                xInput.text.toString(),
                yInput.text.toString()
            )
        }

        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            presenter.doCancel()
        }
    }

    // fills the form with the mark that is being edited
    fun showMark(mark: PlacedMark) {

        findViewById<TextView>(R.id.formTitle)
            .text = "Edit Mark"

        titleInput.setText(mark.title)
        descriptionInput.setText(mark.desc)
        xInput.setText(mark.x.toString())
        yInput.setText(mark.y.toString())

        showImage(mark.imageUri?.let { Uri.parse(it) })
    }

    // shows the photo, or the gallery icon if there isn't one
    fun showImage(uri: Uri?) {
        if (uri != null) {
            imagePreview.setImageURI(uri)
        } else {
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    // puts the spot picked on the map into the x and y boxes
    fun showLocation(latitude: Double, longitude: Double) {
        xInput.setText(latitude.toString())
        yInput.setText(longitude.toString())
    }

    fun showTitleError(message: String) {
        titleInput.error = message
    }

    fun showXError(message: String) {
        xInput.error = message
    }

    fun showYError(message: String) {
        yInput.error = message
    }

    fun showMessage(message: String, long: Boolean = false) {
        Toast.makeText(
            this,
            message,
            if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }
}
