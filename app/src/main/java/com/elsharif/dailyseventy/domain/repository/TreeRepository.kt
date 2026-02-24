package com.elsharif.dailyseventy.domain.repository


import com.elsharif.dailyseventy.domain.data.tree.TreeDao
import com.elsharif.dailyseventy.domain.data.tree.TreeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreeRepository @Inject constructor(
    private val treeDao: TreeDao
) {
    fun getTreeProgress(): Flow<TreeEntity?> = treeDao.getProgress()

    suspend fun initIfEmpty() {
        val current = treeDao.getProgress().first()
        if (current == null) {
            treeDao.saveProgress(TreeEntity())
        }
    }

    suspend fun addZikrPoints(type: String, count: Int = 1) {
        when (type) {
            "subhanallah"   -> treeDao.addSubhanallah(10 * count, count)
            "alhamdulillah" -> treeDao.addAlhamdulillah(15 * count, count)
            "allahuakbar"   -> treeDao.addAllahuakbar(20 * count, count)
            "lailaha"       -> treeDao.addLaIlaha(25 * count, count)
            "hawqala"       -> treeDao.addHawqala(15 * count, count)
            "astaghfir"     -> treeDao.addAstaghfir(10 * count, count)
            "salawat"       -> treeDao.addSalawat(20 * count, count)
            "bismillah"     -> treeDao.addBismillah(5 * count, count)
            "mashallah"     -> treeDao.addMashallah(5 * count, count)
        }
    }

    suspend fun resetTree() {
        treeDao.clearAll()
        // يعيد إنشاء الصف الافتراضي
        treeDao.saveProgress(TreeEntity())
    }
}