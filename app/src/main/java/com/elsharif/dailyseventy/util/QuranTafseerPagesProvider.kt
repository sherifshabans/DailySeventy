package com.elsharif.dailyseventy.util

import android.content.Context
import com.example.core.domain.quran.DomainAyaTafseer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.reflect.Type

object QuranTafseerPagesProvider {
    fun readQuranTafseer(context: Context): List<DomainAyaTafseer> {
        val gson = GsonBuilder().create()
        val quranString = getJSONFileString("quran/tafseer/tafseer.json", context)
        val listType: Type = object : TypeToken<List<DomainAyaTafseer?>?>() {}.type
        val quran: List<DomainAyaTafseer> = gson.fromJson(quranString, listType)
        return quran
    }

    @Throws(IOException::class)
    fun getJSONFileString(filename: String, context: Context): String {
        val manager = context.assets
        val file = manager.open(filename)
        val formArray = ByteArray(file.available())
        file.read(formArray)
        file.close()
        return String(formArray)
    }
}