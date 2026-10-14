package org.setu.placemarklab2

interface OnMarkListener {

    // normal tap on an item, used to edit the mark
    fun onMarkClick(mark: PlacedMark)

    // long press on an item, used to delete the mark
    // returns true when the long press has been handled
    fun onMarkLongClick(mark: PlacedMark): Boolean
}
