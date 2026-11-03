package org.setu.placemarklab2

data class PlacedMark(
    var id: Long = 0,
    var title: String = "",
    var desc: String = "",
    var x: Double = 0.0,
    var y: Double = 0.0,
    // the picked photo as a string, null if there isn't one
    var imageUri: String? = null
) {
    // checked when saving (not in init) so the form can be built while I'm still typing
    // isFinite() also rejects NaN and infinity
    fun validate() {
        require(title.isNotBlank()) {
            "Name must not be empty."
        }

        require(x.isFinite() && x in -90.0..90.0) {
            "Latitude must be between -90 and 90."
        }

        require(y.isFinite() && y in -180.0..180.0) {
            "Longitude must be between -180 and 180."
        }
    }
}
