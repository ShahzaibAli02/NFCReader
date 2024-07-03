package com.salman.nfcreader.model

data class Beep(val fileName:String,val label:String)
{
    override fun toString(): String {
        return label
    }
}


 fun getBeepsList():List<Beep>
{

    return  listOf(
        Beep("beep-01.mp3","Beep 1"),
        Beep("beep-02.mp3","Beep 2"),
        Beep("beep-03.mp3","Beep 3"),
        Beep("beep-04.mp3","Beep 4"),
        Beep("beep-05.mp3","Beep 5"),
        Beep("beep-06.mp3","Beep 6")

    )

}
