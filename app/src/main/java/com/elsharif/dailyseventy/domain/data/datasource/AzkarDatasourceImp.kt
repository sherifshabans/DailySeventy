package com.elsharif.dailyseventy.domain.data.datasource

import android.content.Context
import com.example.core.data.datasource.AzkarDatasource
import com.example.core.domain.azkar.DomainZekr
import com.example.core.domain.azkar.DomainZekrType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class AzkarDatasourceImp @Inject constructor(
    private val context: Context
) : AzkarDatasource {

    override fun getAzkarOfCategory(zekrCategory: String): Flow<List<DomainZekr>> = flow {
        try {
            val jsonString = context.assets.open("Azkar.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val azkarList = mutableListOf<DomainZekr>()

            if (jsonObject.has(zekrCategory)) {
                val categoryArray = jsonObject.getJSONArray(zekrCategory)
                for (i in 0 until categoryArray.length()) {
                    val azkarObject = categoryArray.getJSONObject(i)
                    val zekr = DomainZekr(
                        zekr = azkarObject.optString("content", ""),
                        category = azkarObject.optString("category", ""),
                        description = azkarObject.optString("description", ""),
                        reference = azkarObject.optString("reference", ""),
                        count = azkarObject.optString("count", "1")
                    )
                    azkarList.add(zekr)
                }
            }
            emit(azkarList)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getAzkarNames(): Flow<List<DomainZekrType>> = flow {
        try {
            val jsonString = context.assets.open("Azkar.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val categories = mutableListOf<DomainZekrType>()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val categoryName = keys.next()
                categories.add(DomainZekrType(categoryName))
            }
            emit(categories)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
} 