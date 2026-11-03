package org.setu.placemarklab2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class AddEditActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    private lateinit var imagePreview: ImageView

    // the photo I picked, null until I pick one
    private var selectedImageUri: Uri? = null

    private var editingId: Long? = null

    // opens the phone's file picker, uri comes back null if I cancel
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                // ask to keep access to the photo so it still loads after the app restarts
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    Timber.w("Could not keep access to $uri")
                }

                Timber.d("Picked image $uri")

                selectedImageUri = uri
                imagePreview.setImageURI(uri)
            } else {
                Timber.d("Image picker cancelled")
            }
        }

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

        findViewById<Button>(R.id.imagePickButton).setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

        editingId =
            intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            Timber.d("AddEditActivity opened to edit mark id=$editingId")

            findViewById<TextView>(R.id.formTitle)
                .text = "Edit Mark"

            loadExistingMark(editingId!!)

        } else {

            Timber.d("AddEditActivity opened to add a new mark")
        }

        saveButton.setOnClickListener {
            saveMark()
        }

        cancelButton.setOnClickListener {
            Timber.d("Add/edit cancelled")
            finish()
        }
    }

    private fun loadExistingMark(id: Long) {

        val mark =
            AppData.placedMarks.findOne(id)

        if (mark == null) {

            Timber.w("Tried to edit mark id=$id but it was not found")

            Toast.makeText(
                this,
                "Mark not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(mark.title)
        descriptionInput.setText(mark.desc)
        xInput.setText(mark.x.toString())
        yInput.setText(mark.y.toString())

        // show the photo this mark already has, or the gallery icon if it has none
        selectedImageUri = mark.imageUri?.let { Uri.parse(it) }

        if (selectedImageUri != null) {
            imagePreview.setImageURI(selectedImageUri)
        } else {
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun saveMark() {

        val title =
            titleInput.text.toString().trim()

        val description =
            descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            Timber.d("Save blocked: title is empty")
            titleInput.error = "Title is required"
            return
        }

        val x =
            xInput.text.toString().toDoubleOrNull()

        if (x == null) {
            Timber.d("Save blocked: x is not a valid number")
            xInput.error = "Enter a valid number"
            return
        }

        val y =
            yInput.text.toString().toDoubleOrNull()

        if (y == null) {
            Timber.d("Save blocked: y is not a valid number")
            yInput.error = "Enter a valid number"
            return
        }

        if (editingId == null) {

            val mark = PlacedMark(
                title = title,
                desc = description,
                x = x,
                y = y,
                imageUri = selectedImageUri?.toString()
            )

            Timber.d("Creating mark title=${mark.title}")

            // validate() throws if something is out of range, so show that message and stay on the form
            try {
                AppData.placedMarks.create(mark)
            } catch (e: IllegalArgumentException) {
                showValidationError(e)
                return
            }

            Toast.makeText(
                this,
                "Mark created",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val mark = PlacedMark(
                id = editingId!!,
                title = title,
                desc = description,
                x = x,
                y = y,
                imageUri = selectedImageUri?.toString()
            )

            Timber.d("Updating mark id=${mark.id}, title=${mark.title}")

            try {
                AppData.placedMarks.update(mark)
            } catch (e: IllegalArgumentException) {
                showValidationError(e)
                return
            }

            Toast.makeText(
                this,
                "Mark updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    private fun showValidationError(e: IllegalArgumentException) {
        Timber.d("Save blocked: ${e.message}")

        Toast.makeText(
            this,
            e.message ?: "Please check the entered values.",
            Toast.LENGTH_LONG
        ).show()
    }
}
