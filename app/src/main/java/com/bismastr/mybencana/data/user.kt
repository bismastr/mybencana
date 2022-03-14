package com.bismastr.mybencana.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class user(
    var email: String,
    var uid: String,
): Parcelable
