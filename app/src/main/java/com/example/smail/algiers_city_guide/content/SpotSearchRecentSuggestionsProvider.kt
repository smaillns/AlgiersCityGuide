package com.example.smail.algiers_city_guide.content

import android.content.SearchRecentSuggestionsProvider

class SpotSearchRecentSuggestionsProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        val AUTHORITY = "com.example.smail.algiers_city_guide.content.SpotSearchRecentSuggestionsProvider"
        val MODE = SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES
    }
}
