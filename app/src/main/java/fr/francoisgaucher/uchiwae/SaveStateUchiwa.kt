package fr.francoisgaucher.uchiwae

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class SaveStateUchiwa : View.BaseSavedState {
    var pies: List<PieUchiwa>? = null
    var piesCopy: List<PieUchiwa>? = null
    var pieSelected: PieUchiwa? = null
    var paddingPie: Float? = null
    var uchiwaEnum: UchiwaEnum? = null
    var lastIndicePieGot: Int? = null
    var forceStopAnimation: Boolean? = null

    constructor(superState: Parcelable?) : super(superState) {

    }

    constructor(parcel: Parcel) : super(parcel) {
        paddingPie = parcel.readFloat()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        paddingPie?.let {
            out.writeFloat(it)
        }
    }

    companion object CREATOR : Parcelable.Creator<SaveStateUchiwa> {
        override fun createFromParcel(input: Parcel): SaveStateUchiwa {
            return SaveStateUchiwa(input)
        }

        override fun newArray(size: Int): Array<SaveStateUchiwa?> {
            return arrayOfNulls<SaveStateUchiwa>(size)
        }
    }
}