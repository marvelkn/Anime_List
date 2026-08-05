package com.example.anime_list.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.anime_list.R
import com.example.anime_list.data.model.Anime
import com.example.anime_list.data.remote.ApiClient
import com.example.anime_list.databinding.FragmentHomeBinding
import com.example.anime_list.ui.detail.DetailActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var animeAdapter: AnimeAdapter
    private var selectedAnime: Anime? = null
    private var fullAnimeList: List<Anime> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvAnime.layoutManager = GridLayoutManager(requireContext(), 3)

        setupChips()
        fetchAnime()
    }

    private fun setupChips() {
        binding.chipAll.setOnClickListener {
            binding.tvSectionTitle.text = "Top Anime"
            applySort("all")
        }
        binding.chipScore.setOnClickListener {
            binding.tvSectionTitle.text = "⭐ By Top Score"
            applySort("score")
        }
        binding.chipTitle.setOnClickListener {
            binding.tvSectionTitle.text = "🔤 Sorted A-Z"
            applySort("title")
        }
        binding.chipEpisodes.setOnClickListener {
            binding.tvSectionTitle.text = "📺 Most Episodes"
            applySort("episodes")
        }
    }

    private fun applySort(sortBy: String) {
        val sorted = when (sortBy) {
            "score"    -> fullAnimeList.sortedByDescending { it.score ?: 0.0 }
            "title"    -> fullAnimeList.sortedBy { it.title }
            "episodes" -> fullAnimeList.sortedByDescending { it.episodes ?: 0 }
            else       -> fullAnimeList
        }
        animeAdapter.updateList(sorted)
    }

    private fun fetchAnime() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.instance.getTopAnime()
                if (response.isSuccessful) {
                    val animeList = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        fullAnimeList = animeList
                        setupFeatured(animeList.firstOrNull())
                        setupRecyclerView(animeList)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching anime", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to load anime", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFeatured(anime: Anime?) {
        if (anime == null) return
        binding.tvFeaturedTitle.text = anime.title
        binding.tvFeaturedScore.text = "⭐ Score: ${anime.score ?: "N/A"}  |  ${anime.episodes ?: "?"} eps"

        Glide.with(this)
            .load(anime.images.jpg.largeImageUrl)
            .into(binding.ivFeatured)

        binding.btnFeaturedPlay.setOnClickListener {
            openDetail(anime)
        }
        binding.btnFeaturedInfo.setOnClickListener {
            openDetail(anime)
        }
    }

    private fun setupRecyclerView(animeList: List<Anime>) {
        animeAdapter = AnimeAdapter(
            animeList = animeList,
            onItemClick = { anime -> openDetail(anime) },
            onMoreClick = { anime, view -> showPopupMenu(anime, view) },
            onItemLongClick = { anime, view ->
                selectedAnime = anime
                registerForContextMenu(view)
                view.showContextMenu()
            }
        )
        binding.rvAnime.adapter = animeAdapter
    }

    private fun openDetail(anime: Anime) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("ANIME", anime)
        startActivity(intent)
    }

    private fun showPopupMenu(anime: Anime, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "ℹ  More Info")
        popup.menu.add(0, 2, 0, "➕  Add to My List")
        popup.menu.add(0, 3, 0, "🚫  Not Interested")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> { openDetail(anime); true }
                2 -> {
                    Toast.makeText(requireContext(), "\"${anime.title}\" added to My List", Toast.LENGTH_SHORT).show()
                    true
                }
                3 -> {
                    Toast.makeText(requireContext(), "Won't show \"${anime.title}\" again", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(selectedAnime?.title ?: "Options")
        menu.add(0, R.id.ctx_add_list, 0, "➕  Add to My List")
        menu.add(0, R.id.ctx_share, 1, "🔗  Share")
        menu.add(0, R.id.ctx_detail, 2, "ℹ  View Detail")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.ctx_add_list -> {
                Toast.makeText(requireContext(), "\"${selectedAnime?.title}\" added to My List", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.ctx_share -> {
                Toast.makeText(requireContext(), "Sharing \"${selectedAnime?.title}\"", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.ctx_detail -> {
                selectedAnime?.let { openDetail(it) }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
