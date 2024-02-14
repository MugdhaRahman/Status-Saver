package com.androvine.statussaver.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.androvine.statussaver.model.ModelContact

class DirectChatContactDB(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // ------------------------------------ CREATE ------------------------------------

    // add single contact
    fun addSingleContact(contact: ModelContact) {
        val db = writableDatabase
        db.execSQL("INSERT INTO $TABLE_NAME ($KEY_NAME, $KEY_NUMBER) VALUES ('${contact.name}', '${contact.number}')")
        db.close()
    }

    // ------------------------------------ READ ------------------------------------

    // get all contacts
    fun getAllContacts(): List<ModelContact> {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val contacts = mutableListOf<ModelContact>()

        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val nameIndex = cursor.getColumnIndex(KEY_NAME)
                val numberIndex = cursor.getColumnIndex(KEY_NUMBER)

                if (nameIndex == -1 || numberIndex == -1) {
                    continue
                }

                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)

                val contact = ModelContact(name, number)
                contacts.add(contact)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return contacts
    }

    // ------------------------------------ DELETE ------------------------------------

    // delete single contact
    fun deleteSingleContact(contact: ModelContact) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME WHERE $KEY_NAME='${contact.name}' AND $KEY_NUMBER='${contact.number}'")
        db.close()
    }

    // ------------------------------------ OTHER ------------------------------------

    // check if contact exists by number
    fun checkIfContactExists(number: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $KEY_NUMBER='$number'"
        val cursor: Cursor = db.rawQuery(query, null)
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    companion object {
        // DB Name
        const val DATABASE_NAME = "contact_db"

        // DB Version
        const val DATABASE_VERSION = 1

        // Table Name Contact Table
        const val TABLE_NAME = "contact_table"

        // Table columns ID, Name, Contact
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_NUMBER = "number"

        // Create Table Query
        const val CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_NAME TEXT, $KEY_NUMBER TEXT)"
    }
}
