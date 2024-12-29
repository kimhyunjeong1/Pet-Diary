package com.example.test

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "schedule.db"
        private const val DATABASE_VERSION = 1

        // 일정 테이블
        const val TABLE_NAME = "schedule"
        const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_VACCINATION = "vaccination"
        const val COLUMN_CHECKUP = "checkup"
        const val COLUMN_BATH = "bath"
        const val COLUMN_MEDICATION = "medication"
        const val COLUMN_DEWORMING = "deworming"
        const val COLUMN_FEEDING = "feeding"
        const val COLUMN_TOILET = "toilet"
        const val COLUMN_ACTIVITY = "activity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE TEXT NOT NULL UNIQUE,
                $COLUMN_VACCINATION INTEGER DEFAULT 0,
                $COLUMN_CHECKUP INTEGER DEFAULT 0,
                $COLUMN_BATH INTEGER DEFAULT 0,
                $COLUMN_MEDICATION INTEGER DEFAULT 0,
                $COLUMN_DEWORMING INTEGER DEFAULT 0,
                $COLUMN_FEEDING TEXT DEFAULT '',
                $COLUMN_TOILET TEXT DEFAULT '',
                $COLUMN_ACTIVITY TEXT DEFAULT ''
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // 일정 저장
    fun saveSchedule(
        date: String,
        vaccination: Boolean,
        checkup: Boolean,
        bath: Boolean,
        medication: Boolean,
        deworming: Boolean,
        feeding: String,
        toilet: String,
        activity: String
    ): Long {
        val db = writableDatabase

        // 입력된 날짜를 표준 형식(yyyy-MM-dd)으로 변환
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = try {
            LocalDate.parse(date, formatter).toString()
        } catch (e: Exception) {
            date // 변환 실패 시 원본 사용
        }

        val values = ContentValues().apply {
            put(COLUMN_DATE, formattedDate)
            put(COLUMN_VACCINATION, if (vaccination) 1 else 0)
            put(COLUMN_CHECKUP, if (checkup) 1 else 0)
            put(COLUMN_BATH, if (bath) 1 else 0)
            put(COLUMN_MEDICATION, if (medication) 1 else 0)
            put(COLUMN_DEWORMING, if (deworming) 1 else 0)
            put(COLUMN_FEEDING, feeding)
            put(COLUMN_TOILET, toilet)
            put(COLUMN_ACTIVITY, activity)
        }
        return db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // 특정 날짜의 일정 조회
    fun getScheduleByDate(date: String): Map<String, Any>? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME, null, "$COLUMN_DATE = ?", arrayOf(date),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            mapOf(
                COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                COLUMN_VACCINATION to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VACCINATION)) == 1),
                COLUMN_CHECKUP to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHECKUP)) == 1),
                COLUMN_BATH to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATH)) == 1),
                COLUMN_MEDICATION to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEDICATION)) == 1),
                COLUMN_DEWORMING to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEWORMING)) == 1),
                COLUMN_FEEDING to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDING)),
                COLUMN_TOILET to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOILET)),
                COLUMN_ACTIVITY to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY))
            )
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    // 일정 조회: 앞으로 일주일 내의 일정 가져오기
    fun getSchedulesWithinWeek(): List<Map<String, Any>> {
        val db = readableDatabase
        val schedules = mutableListOf<Map<String, Any>>()

        val currentDate = LocalDate.now()
        val endDate = currentDate.plusDays(7)

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_DATE BETWEEN ? AND ? ORDER BY $COLUMN_DATE ASC",
            arrayOf(currentDate.toString(), endDate.toString())
        )

        while (cursor.moveToNext()) {
            val schedule = mapOf(
                COLUMN_DATE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                COLUMN_VACCINATION to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VACCINATION)) == 1),
                COLUMN_CHECKUP to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CHECKUP)) == 1),
                COLUMN_BATH to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATH)) == 1),
                COLUMN_MEDICATION to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEDICATION)) == 1),
                COLUMN_DEWORMING to (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEWORMING)) == 1),
                COLUMN_FEEDING to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FEEDING)),
                COLUMN_TOILET to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOILET)),
                COLUMN_ACTIVITY to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIVITY))
            )
            schedules.add(schedule)
        }
        cursor.close()
        return schedules
    }

    // 특정 날짜의 일정 삭제
    fun deleteScheduleByDate(date: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_DATE = ?", arrayOf(date))
    }
}
