package org.setu.placemarklab2

import android.content.Context
import java.io.File

object AppData {

    lateinit var placedMarks: PlacedMarkList
        private set

    // makes the list once, using a json file in the app's own files folder
    fun initialize(context: Context) {
        // MainActivity can be created again (e.g. rotating), no need to load the file twice
        if (::placedMarks.isInitialized) return

        val file = File(
            context.filesDir,
            "place_list.json"
        )

        placedMarks = PlacedMarkList(file)
    }
}
