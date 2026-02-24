package com.elsharif.dailyseventy.domain.data.tree


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elsharif.dailyseventy.domain.data.tree.TreeDao
import com.elsharif.dailyseventy.domain.data.tree.TreeEntity

@Database(
    entities = [TreeEntity::class],
    version = 2,           // ← غيّرها من 1 إلى 2
    exportSchema = false
)
abstract class TreeDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao

    companion object {
        @Volatile
        private var INSTANCE: TreeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN laIlahaCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN hawqalaCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN astaghfirCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN salawatCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN bismillahCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tree_progress ADD COLUMN mashallahCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): TreeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TreeDatabase::class.java,
                    "tree_db.db"
                )
                    .addMigrations(MIGRATION_1_2)   // ← أضف السطر ده
                    .build().also { INSTANCE = it }
            }
        }
    }
}