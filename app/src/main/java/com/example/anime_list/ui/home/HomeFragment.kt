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

        fetchAnime()
    }

    private fun fetchAnime() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.instance.getTopAnime()
                if (response.isSuccessful) {
                    val animeList = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        setupRecyclerView(animeList)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching anime", e)
            }
        }
    }

    private fun setupRecyclerView(animeList: List<Anime>) {
        animeAdapter = AnimeAdapter(
            animeList = animeList,
            onItemClick = { anime ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("ANIME", anime)
                startActivity(intent)
            },
            onMoreClick = { anime, view ->
                showPopupMenu(anime, view)
            },
            onItemLongClick = { anime, view ->
                selectedAnime = anime
                // Register for context menu
                registerForContextMenu(view)
                view.showContextMenu()
            }
        )
        binding.rvAnime.adapter = animeAdapter
    }

    private fun showPopupMenu(anime: Anime, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "More Info")
        popup.menu.add(0, 2, 0, "Not Interested")
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra("ANIME", anime)
                    startActivity(intent)
                    true
                }
                2 -> {
                    Toast.makeText(requireContext(), "Removed from suggestions", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle(selectedAnime?.title)
        menu.add(0, 1, 0, "Add to My List")
        menu.add(0, 2, 0, "Share")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            1 -> {
                Toast.makeText(requireContext(), "${selectedAnime?.title} added to My List", Toast.LENGTH_SHORT).show()
                true
            }
            2 -> {
                Toast.makeText(requireContext(), "Shared ${selectedAnime?.title}", Toast.LENGTH_SHORT).show()
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
