package com.example.anime_list

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.anime_list.databinding.ActivityMainBinding
import com.example.anime_list.util.SearchBridge

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use our custom Toolbar instead of the default ActionBar
        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_my_list, R.id.navigation_about)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Update subtitle dynamically per destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.subtitle = when (destination.id) {
                R.id.navigation_home     -> "Your Anime Universe"
                R.id.navigation_my_list  -> "Saved anime"
                R.id.navigation_about    -> "Creator Info"
                else                     -> null
            }
            invalidateOptionsMenu() // Refresh menu visibility based on destination
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentDestination = navController.currentDestination?.id
        val searchItem = menu?.findItem(R.id.action_search)
        
        // Hide search if we are on About page
        if (currentDestination == R.id.navigation_about) {
            searchItem?.isVisible = false
        } else {
            searchItem?.isVisible = true
        }
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.queryHint = "Search all anime..."
        searchView?.maxWidth = Int.MAX_VALUE
        
        // Fix text color for SearchView
        val searchAutoComplete = searchView?.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        searchAutoComplete?.setTextColor(android.graphics.Color.WHITE)
        searchAutoComplete?.setHintTextColor(android.graphics.Color.parseColor("#888888"))

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                SearchBridge.onQueryChanged?.invoke(query?.trim() ?: "")
                searchView.clearFocus() // Hide keyboard but keep search open
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                SearchBridge.onQueryChanged?.invoke(newText?.trim() ?: "")
                return true
            }
        })

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Keep expanded when keyboard closes
                return true
            }
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                SearchBridge.onSearchClosed?.invoke()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}