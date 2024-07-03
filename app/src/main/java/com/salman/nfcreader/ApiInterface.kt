package com.salman.nfcreader
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {


    //https://sheets.googleapis.com/v4/spreadsheets/1dxijc2lbN3EQe9ehVi7ExKBopCtHpH-xqYkbXQVxhjs/values/User1?alt=json&key=AIzaSyDfMekXftUEmTLroc_aKXkMgAeFZxTEf1U
    @GET("{sheet_id}/values/{sheet_name}")
    fun Getdata(@Path("sheet_id") sheetId:String,
        @Path("sheet_name") sheet_name:String,
        @Query("alt") alt:String="json",
        @Query("key") key:String
        ): Call<SheetResponse>

}
