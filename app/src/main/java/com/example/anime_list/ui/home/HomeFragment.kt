package com.example.anime_list.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.anime_list.R
import com.example.anime_list.data.model.Anime
import com.example.anime_list.data.remote.ApiClient
import com.example.anime_list.databinding.FragmentHomeBinding
import com.example.anime_list.ui.detail.DetailActivity
import com.example.anime_list.util.MyListManager
import com.example.anime_list.util.SearchBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var animeAdapter: AnimeAdapter
    private var selectedAnime: Anime? = null

    // Pagination state
    private val fullAnimeList = mutableListOf<Anime>()
    private var currentPage = 1
    private var hasNextPage = true
    private var isLoading = false

    // Search / sort state
    private var isSearchActive = false
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvAnime.layoutManager = gridLayoutManager

        // Infinite scroll: detect when NestedScrollView reaches the bottom
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                val maxScroll = v.getChildAt(0).measuredHeight - v.measuredHeight
                // Trigger load when within 300px of bottom
                if (scrollY >= maxScroll - 300 && !isLoading && hasNextPage && !isSearchActive) {
                    loadNextPage()
                }
            }
        )

        // Search bridge
        SearchBridge.onQueryChanged = { query -> handleSearch(query) }
        SearchBridge.onSearchClosed = {
            isSearchActive = false
            binding.tvSectionTitle.text = "Top Anime"
            animeAdapter.updateList(fullAnimeList)
        }

        setupChips()

        // First load
        animeAdapter = AnimeAdapter(
            animeList = fullAnimeList.toList(),
            onItemClick = { openDetail(it) },
            onMoreClick = { anime, v -> showPopupMenu(anime, v) },
            onItemLongClick = { anime, v ->
                selectedAnime = anime
                registerForContextMenu(v)
                v.showContextMenu()
            }
        )
        binding.rvAnime.adapter = animeAdapter

        if (fullAnimeList.isEmpty()) {
            loadNextPage()
        } else {
            setupFeatured(fullAnimeList.take(5))
            updateSectionTitle()
        }
    }

    // ─── Pagination ────────────────────────────────────────────────────────────

    private fun loadNextPage() {
        if (isLoading || !hasNextPage) return
        isLoading = true
        binding.pbLoadingMore.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Jikan rate-limits: add small delay after page 1 to avoid 429
                if (currentPage > 1) delay(400)

                val response = ApiClient.instance.getTopAnime(page = currentPage)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val newItems = body?.data ?: emptyList()
                        hasNextPage = body?.pagination?.hasNextPage ?: false

                        if (currentPage == 1) {
                            // First load: setup featured and show list
                            fullAnimeList.clear()
                            fullAnimeList.addAll(newItems)
                            setupFeatured(fullAnimeList.take(5))
                            animeAdapter.updateList(fullAnimeList.toList())
                        } else {
                            // Append new items
                            fullAnimeList.addAll(newItems)
                            animeAdapter.updateList(fullAnimeList.toList())
                        }

                        currentPage++
                        updateSectionTitle()
                    } else if (response.code() == 429) {
                        // Rate limited — retry after a pause
                        delay(1500)
                        isLoading = false
                        loadNextPage()
                        return@withContext
                    }

                    binding.pbLoadingMore.visibility = View.GONE
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Fetch page $currentPage error", e)
                withContext(Dispatchers.Main) {
                    binding.pbLoadingMore.visibility = View.GONE
                    isLoading = false
                    if (currentPage == 1) {
                        Toast.makeText(requireContext(), "Failed to load anime", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateSectionTitle() {
        binding.tvSectionTitle.text = "Top Anime"
    }

    // ─── Search ────────────────────────────────────────────────────────────────

    private fun handleSearch(query: String) {
        searchJob?.cancel()
        isSearchActive = query.isNotBlank()

        if (query.isBlank()) {
            binding.tvSectionTitle.text = "Top Anime"
            animeAdapter.updateList(fullAnimeList.toList())
            return
        }

        // Local filter first (instant)
        val local = fullAnimeList.filter { it.title.contains(query, ignoreCase = true) }
        binding.tvSectionTitle.text = "Results for \"$query\""
        animeAdapter.updateList(local)

        // Then broader API search (debounced)
        searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            delay(700)
            try {
                val response = ApiClient.instance.searchAnime(query)
                if (response.isSuccessful) {
                    val results = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        if (results.isNotEmpty()) {
                            animeAdapter.updateList(results)
                            binding.tvSectionTitle.text = "Results for \"$query\""
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Search error", e)
            }
        }
    }

    // ─── Sort chips ────────────────────────────────────────────────────────────

    private fun setupChips() {
        binding.chipAll.setOnClickListener {
            if (isSearchActive) return@setOnClickListener
            animeAdapter.updateList(fullAnimeList.toList())
            updateSectionTitle()
        }
        binding.chipScore.setOnClickListener {
            if (isSearchActive) return@setOnClickListener
            binding.tvSectionTitle.text = "⭐ By Top Score"
            animeAdapter.updateList(fullAnimeList.sortedByDescending { it.score ?: 0.0 })
        }
        binding.chipTitle.setOnClickListener {
            if (isSearchActive) return@setOnClickListener
            binding.tvSectionTitle.text = "🔤 A – Z"
            animeAdapter.updateList(fullAnimeList.sortedBy { it.title })
        }
        binding.chipEpisodes.setOnClickListener {
            if (isSearchActive) return@setOnClickListener
            binding.tvSectionTitle.text = "📺 Most Episodes"
            animeAdapter.updateList(fullAnimeList.sortedByDescending { it.episodes ?: 0 })
        }
        
        // Genre Chips (trigger API search)
        binding.chipAction.setOnClickListener { searchByGenre("1", "Action") }
        binding.chipRomance.setOnClickListener { searchByGenre("22", "Romance") }
        binding.chipComedy.setOnClickListener { searchByGenre("4", "Comedy") }
        binding.chipAdventure.setOnClickListener { searchByGenre("2", "Adventure") }
    }

    private fun searchByGenre(genreId: String, genreName: String) {
        searchJob?.cancel()
        isLoading = true
        binding.pbLoadingMore.visibility = View.VISIBLE
        binding.tvSectionTitle.text = "Loading $genreName Anime..."

        searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // We use searchAnime with an empty query but specifying the genre
                val response = ApiClient.instance.searchAnime(query = "", genres = genreId)
                if (response.isSuccessful) {
                    val results = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        if (results.isNotEmpty()) {
                            animeAdapter.updateList(results)
                            binding.tvSectionTitle.text = "🍿 Top $genreName Anime"
                        } else {
                            binding.tvSectionTitle.text = "No $genreName Anime Found"
                        }
                        binding.pbLoadingMore.visibility = View.GONE
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvSectionTitle.text = "Error Loading $genreName"
                    binding.pbLoadingMore.visibility = View.GONE
                    isLoading = false
                }
            }
        }
    }

    // ─── Featured banner ───────────────────────────────────────────────────────

    private var carouselJob: Job? = null

    private fun setupFeatured(animes: List<Anime>) {
        if (animes.isEmpty()) return
        
        val adapter = FeaturedPagerAdapter(
            featuredAnime = animes,
            onPlayClick = { openDetail(it) },
            onInfoClick = { openDetail(it) }
        )
        binding.vpFeatured.adapter = adapter
        
        // Setup dots
        setupDots(animes.size)
        binding.vpFeatured.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position, animes.size)
            }
        })

        // Auto-scroll carousel every 4 seconds
        carouselJob?.cancel()
        carouselJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(4000)
                if (animes.size > 1) {
                    val nextItem = (binding.vpFeatured.currentItem + 1) % animes.size
                    binding.vpFeatured.setCurrentItem(nextItem, true)
                }
            }
        }
    }

    private fun setupDots(count: Int) {
        binding.layoutDots.removeAllViews()
        for (i in 0 until count) {
            val dot = android.widget.ImageView(requireContext())
            dot.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive))
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            binding.layoutDots.addView(dot, params)
        }
        updateDots(0, count)
    }

    private fun updateDots(position: Int, count: Int) {
        for (i in 0 until count) {
            val dot = binding.layoutDots.getChildAt(i) as? android.widget.ImageView
            if (i == position) {
                dot?.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.dot_active))
            } else {
                dot?.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.dot_inactive))
            }
        }
    }

    // ─── Navigation ────────────────────────────────────────────────────────────

    private fun openDetail(anime: Anime) {
        startActivity(Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra("ANIME", anime)
        })
    }

    // ─── Popup menu ────────────────────────────────────────────────────────────

    private fun showPopupMenu(anime: Anime, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "ℹ  More Info")
        popup.menu.add(0, 2, 0, "⭐  Add to My List")
        popup.menu.add(0, 3, 0, "🚫  Not Interested")
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                1 -> { openDetail(anime); true }
                2 -> {
                    MyListManager.addAnime(requireContext(), anime)
                    Toast.makeText(requireContext(), "\"${anime.title}\" added ⭐", Toast.LENGTH_SHORT).show()
                    true
                }
                3 -> { Toast.makeText(requireContext(), "Got it!", Toast.LENGTH_SHORT).show(); true }
                else -> false
            }
        }
        popup.show()
    }

    // ─── Context menu ──────────────────────────────────────────────────────────

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(selectedAnime?.title ?: "Options")
        menu.add(0, R.id.ctx_add_list, 0, "⭐  Add to My List")
        menu.add(0, R.id.ctx_share, 1, "🔗  Share")
        menu.add(0, R.id.ctx_detail, 2, "ℹ  View Detail")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ctx_add_list -> {
                selectedAnime?.let {
                    MyListManager.addAnime(requireContext(), it)
                    Toast.makeText(requireContext(), "\"${it.title}\" added ⭐", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.ctx_share -> {
                Toast.makeText(requireContext(), "Sharing \"${selectedAnime?.title}\"", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.ctx_detail -> { selectedAnime?.let { openDetail(it) }; true }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        carouselJob?.cancel()
        SearchBridge.onQueryChanged = null
        SearchBridge.onSearchClosed = null
        isLoading = false
        isSearchActive = false
        _binding = null
    }
}
