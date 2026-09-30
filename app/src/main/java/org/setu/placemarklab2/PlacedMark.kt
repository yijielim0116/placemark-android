package org.setu.placemarklab2

data class PlacedMark(
    var id: Long = 0,
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0
)
