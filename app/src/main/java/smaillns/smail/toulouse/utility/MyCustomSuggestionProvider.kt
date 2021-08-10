package smaillns.smail.toulouse.utility

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import android.app.SearchManager
import android.content.UriMatcher





class MyCustomSuggestionProvider : ContentProvider() {


    companion object
    {
        private val STORES = "stores/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*"
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).
                addURI("com.zoftino.coupons.search", STORES, 1)
    }

    private val matrixCursorColumns = arrayOf("_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA)
    override fun onCreate(): Boolean {
        return true
    }


    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        return null!!

    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {

        Logcat.d("test", "yes")
        Toast.makeText(context, "test", Toast.LENGTH_SHORT).show()

        return null!!
    }



    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {

        return 0
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri?): String {
        return null!!
    }
}