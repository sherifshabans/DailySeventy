package com.elsharif.dailyseventy.domain.data.datasource

import android.content.Context
import com.elsharif.dailyseventy.util.QuranTafseerPagesProvider
import com.elsharif.dailyseventy.domain.data.database.quran.QuranDao
import com.example.core.data.datasource.QuranDatasource
import com.example.core.domain.quran.DomainAya
import com.example.core.domain.quran.DomainAyaTafseer
import com.example.core.domain.quran.DomainAyaWithTafseer
import com.example.core.domain.quran.DomainJozz
import com.example.core.domain.quran.DomainQuranPage
import com.example.core.domain.quran.DomainSora
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "QuranDatasourceDatabase"

class QuranDatasourceDatabaseImp  @Inject constructor(
    private val quranDao: QuranDao,
    @ApplicationContext private val context: Context
) : QuranDatasource {


    override fun getQuranPageAyaWithTafseer(page: Int): Flow<List<DomainAyaWithTafseer>> = flow {
        val ayatNotNull = CoroutineScope(Dispatchers.IO).async { quranDao.getAyatByPage(page).filterNotNull() }
        val ayatNos = ayatNotNull.await().map { it.ayaNo }
        val soras = ayatNotNull.await().map { it.sora }
        val quranTafseer = CoroutineScope(Dispatchers.IO).async { QuranTafseerPagesProvider.readQuranTafseer(context) }
        val ayatTafseer = quranTafseer.await().filter { it.aya.toInt() in ayatNos && it.number.toInt() in soras }

        val ayaWithTafseerList = ayatNotNull.await().zip(ayatTafseer) { ayat, tafseer ->
            DomainAyaWithTafseer(with(ayat) {
                DomainAya(
                    id,
                    jozz,
                    sora,
                    soraNameEn,
                    soraNameAr,
                    page,
                    lineStart,
                    lineEnd,
                    ayaNo,
                    ayaText,
                    ayaTextEmlaey
                )
            }, with(tafseer) { DomainAyaTafseer(number, aya, text) })
        }
        emit(ayaWithTafseerList)
    }


    /*
        override fun getQuranPages(): Flow<List<DomainQuranPage>> = flow {
            val pages = (1..604).map {
                withContext(Dispatchers.IO) { quranDao.getPageData(it) }
            }
            emit(pages)
        }
    */

   /* override fun getAllAjzaa(): Flow<List<DomainJozz>> = flow {
        (1..30).toList()
            .map { withContext(Dispatchers.IO) { quranDao.getJozzByNumber(it) } }
            .map { DomainJozz(it.jozzNumber, it.startPage, it.endPage) }
            .emit()
    }*/


    /*override fun getQuranPageAyaWithTafseer(page: Int): Flow<List<DomainAyaWithTafseer>> {
        TODO("Not yet implemented")
    }*/

    override fun getSoraByPageNumber(page: Int): Flow<DomainSora> = flow {
        val sora = withContext(Dispatchers.IO) {
            quranDao.getSoraByPageNumber(page)
        }
        emit(DomainSora(sora.soraNumber, sora.startPage, sora.endPage, sora.arabicName, sora.englishName, sora.ayaCount))
    }
}
