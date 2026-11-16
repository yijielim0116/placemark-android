package org.setu.placemarklab2

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import timber.log.Timber

// the file is where the marks get saved, so they are still there when the app is opened again
class PlacedMarkList(private val file: File) {

    private val placedMarks = ArrayList<PlacedMark>()
    private val lastId = AtomicLong(0)

    // pretty printing so the json file is easy to read if I open it
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // load whatever was saved last time as soon as the list is made
    init {
        load()
    }

    // reads the json file back into the list, nothing to do if it hasn't been saved yet
    private fun load() {
        if (!file.exists()) {
            Timber.d("load: no file yet, starting empty")
            return
        }

        val type = object : TypeToken<ArrayList<PlacedMark>>() {}.type

        val loaded: ArrayList<PlacedMark> =
            gson.fromJson(file.readText(), type) ?: arrayListOf()

        placedMarks.clear()
        placedMarks.addAll(loaded)

        // carry on numbering after the biggest saved id so new marks don't reuse one
        lastId.set(placedMarks.maxOfOrNull { it.id } ?: 0L)

        Timber.d("load: ${placedMarks.size} marks from ${file.name}")
    }

    // writes the whole list to the json file
    private fun save() {
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(placedMarks))

        Timber.d("save: ${placedMarks.size} marks to ${file.name}")
    }

    fun findAll(): List<PlacedMark> {
        Timber.d("findAll: ${placedMarks.size} marks")
        return placedMarks
    }

    fun create(mark: PlacedMark) {
        mark.validate()
        mark.id = lastId.incrementAndGet()
        placedMarks.add(mark)
        save()
        Timber.d("create: id=${mark.id}, title=${mark.title}")
    }

    fun update(mark: PlacedMark): Boolean {
        // check before touching the stored one
        mark.validate()
        val foundIndex = placedMarks.indexOfFirst { it.id == mark.id }
        return if (foundIndex != -1) {
            placedMarks[foundIndex] = mark
            save()
            Timber.d("update: id=${mark.id}, title=${mark.title}")
            true
        } else {
            Timber.w("update: no mark with id=${mark.id}")
            false
        }
    }

    fun delete(id: Long): Boolean {
        val found = findOne(id)
        return if (found != null) {
            placedMarks.remove(found)
            save()
            Timber.d("delete: id=$id")
            true
        } else {
            Timber.w("delete: no mark with id=$id")
            false
        }
    }

    fun findOne(id: Long): PlacedMark? {
        val found = placedMarks.find { it.id == id }
        Timber.d("findOne: id=$id found=${found != null}")
        return found
    }
}
