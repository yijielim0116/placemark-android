package org.setu.placemarklab2

import java.util.concurrent.atomic.AtomicLong

class PlacedMarkList {

    private val placedMarks = ArrayList<PlacedMark>()
    private val lastId = AtomicLong(0)

    fun findAll(): List<PlacedMark> = placedMarks

    fun create(mark: PlacedMark) {
        mark.id = lastId.incrementAndGet()
        placedMarks.add(mark)
    }

    fun update(mark: PlacedMark): Boolean {
        val foundIndex = placedMarks.indexOfFirst { it.id == mark.id }
        return if (foundIndex != -1) {
            placedMarks[foundIndex] = mark
            true
        } else {
            false
        }
    }

    fun delete(id: Long): Boolean {
        val found = findOne(id)
        return if (found != null) {
            placedMarks.remove(found)
            true
        } else {
            false
        }
    }

    fun findOne(id: Long): PlacedMark? = placedMarks.find { it.id == id }
}
