package com.example.anime_list.ui.mylist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.anime_list.databinding.FragmentMyListBinding
import com.example.anime_list.ui.detail.DetailActivity
import com.example.anime_list.ui.home.AnimeAdapter
import com.example.anime_list.util.MyListManager

class MyListFragment : Fragment() {

    private var _binding: FragmentMyListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadList()
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning from Detail (in case user modified the list)
        loadList()
    }

    private fun loadList() {
        val list = MyListManager.getAll(requireContext())

        if (list.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvMyList.visibility = View.GONE
            return
        }

        binding.layoutEmpty.visibility = View.GONE
        binding.rvMyList.visibility = View.VISIBLE
        binding.rvMyList.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMyList.adapter = AnimeAdapter(
            animeList = list,
            onItemClick = { anime ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("ANIME", anime)
                startActivity(intent)
            },
            onMoreClick = { _, _ -> },
            onItemLongClick = { anime, _ ->
                MyListManager.removeAnime(requireContext(), anime.malId)
                loadList()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
