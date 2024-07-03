package com.salman.nfcreader
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {


    @GET("{sheet_id}/values/{sheet_name}")
    fun Getdata(@Path("sheet_id") sheetId:String,
        @Path("sheet_name") sheet_name:String,
        @Query("alt") alt:String="json",
        @Query("key") key:String
        ): Call<SheetResponse>

}
