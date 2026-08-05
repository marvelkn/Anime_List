package com.example.anime_list.util

/** Simple singleton bridge so MainActivity's SearchView can talk to HomeFragment. */
object SearchBridge {
    var onQueryChanged: ((String) -> Unit)? = null
    var onSearchClosed: (() -> Unit)? = null
}
