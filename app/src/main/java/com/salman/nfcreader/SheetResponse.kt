package com.salman.nfcreader


import com.google.gson.annotations.SerializedName

data class SheetResponse(
    @SerializedName("majorDimension")
    var majorDimension: String? = null,
    @SerializedName("range")
    var range: String? = null,
    @SerializedName("values")
    var values: List<List<String>>? = null
)