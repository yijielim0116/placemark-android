package org.setu.placemark.models

interface PlacemarkStore {
    fun findAll(): List<PlacemarkModel>
    fun create(placemark: PlacemarkModel)
    fun update(placemark: PlacemarkModel): Boolean
    fun delete(id: Long): Boolean
    fun findOne(id: Long): PlacemarkModel?
}
