package org.setu.placemarklab2

import java.util.concurrent.atomic.AtomicLong
import timber.log.Timber

class PlacedMarkList {

    private val placedMarks = ArrayList<PlacedMark>()
    private val lastId = AtomicLong(0)

    fun findAll(): List<PlacedMark> {
        Timber.d("findAll: ${placedMarks.size} marks")
        return placedMarks
    }

    fun create(mark: PlacedMark) {
        mark.id = lastId.incrementAndGet()
        placedMarks.add(mark)
        Timber.d("create: id=${mark.id}, title=${mark.title}")
    }

    fun update(mark: PlacedMark): Boolean {
        val foundIndex = placedMarks.indexOfFirst { it.id == mark.id }
        return if (foundIndex != -1) {
            placedMarks[foundIndex] = mark
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
