package smaillns.smail.dzair.adapter

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.support.v4.widget.CursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import smaillns.smail.dzair.R

class SearchSuggestionAdapter(context: Context, cursor: Cursor) : CursorAdapter(context, cursor, 0) {


    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        return inflater.inflate(R.layout.search_suggestion_item, parent, false)
    }


    override fun bindView(view: View, context: Context, cursor: Cursor) {
        // reference
        val root = view as LinearLayout
        val titleTextView = root.findViewById<View>(R.id.search_suggestion_item_title) as TextView

        // content
        val index = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
        titleTextView.text = cursor.getString(index)
    }


    fun refill(context: Context, cursor: Cursor) {
        changeCursor(cursor)
    }
}
