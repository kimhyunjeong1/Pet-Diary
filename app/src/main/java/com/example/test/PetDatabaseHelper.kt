package com.example.test

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PetDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pets.db"
        private const val DATABASE_VERSION = 2 // 버전 변경

        // 테이블 및 컬럼 이름
        const val TABLE_NAME = "pets"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_BREED = "breed"
        const val COLUMN_BIRTHDAY = "birthday"
        const val COLUMN_PERSONALITY = "personality"
        const val COLUMN_GENDER = "gender"
        const val COLUMN_IMAGE_URI = "image_uri" // 이미지 URI 추가
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 테이블 생성 SQL
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_BREED TEXT NOT NULL,
                $COLUMN_BIRTHDAY TEXT NOT NULL,
                $COLUMN_PERSONALITY TEXT NOT NULL,
                $COLUMN_GENDER TEXT NOT NULL,
                $COLUMN_IMAGE_URI TEXT DEFAULT '' -- 이미지 경로 추가
            )
        """
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 기존 테이블에 새로운 컬럼 추가
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_IMAGE_URI TEXT DEFAULT ''")
        }
    }

    // 데이터 삽입 함수
    fun insertPet(
        name: String,
        breed: String,
        birthday: String,
        personality: String,
        gender: String,
        imageUri: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_BREED, breed)
            put(COLUMN_BIRTHDAY, birthday)
            put(COLUMN_PERSONALITY, personality)
            put(COLUMN_GENDER, gender)
            put(COLUMN_IMAGE_URI, imageUri) // 이미지 URI 추가
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // 데이터 조회 함수
    fun getAllPets(): List<Map<String, String>> {
        val db = readableDatabase
        val pets = mutableListOf<Map<String, String>>()
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val pet = mapOf(
                COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                COLUMN_BREED to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BREED)),
                COLUMN_BIRTHDAY to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                COLUMN_PERSONALITY to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PERSONALITY)),
                COLUMN_GENDER to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                COLUMN_IMAGE_URI to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)) // 이미지 URI 추가
            )
            pets.add(pet)
        }
        cursor.close()
        return pets
    }
}
