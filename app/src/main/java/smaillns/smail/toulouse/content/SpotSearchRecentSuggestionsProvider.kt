package smaillns.smail.toulouse.content

import android.content.SearchRecentSuggestionsProvider

class SpotSearchRecentSuggestionsProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "smaillns.smail.dzaïr.SpotSearchRecentSuggestionsProvider"
        val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
