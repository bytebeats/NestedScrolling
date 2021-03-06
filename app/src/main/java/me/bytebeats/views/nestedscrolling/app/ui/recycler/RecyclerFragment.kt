package me.bytebeats.views.nestedscrolling.app.ui.recycler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.bytebeats.views.nestedscrolling.app.adapter.ImageAdapter
import me.bytebeats.views.nestedscrolling.app.databinding.FragmentRecyclerViewBinding

class RecyclerFragment : Fragment() {

    private lateinit var recyclerViewModel: RecyclerViewModel
    private var _binding: FragmentRecyclerViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter by lazy { ImageAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recyclerViewModel =
            ViewModelProvider(this).get(RecyclerViewModel::class.java)

        _binding = FragmentRecyclerViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: RecyclerView = binding.recyclerView
        listView.layoutManager = object : LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false) {
            override fun canScrollHorizontally(): Boolean = false
            override fun canScrollVertically(): Boolean = true
        }
        listView.adapter = adapter
        recyclerViewModel.listData.observe(viewLifecycleOwner, { it ->
            adapter.add(it)
        })
        val text: TextView = binding.textDashboard
        text.setOnClickListener {
            recyclerViewModel.generate(10)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}