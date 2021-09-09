package me.bytebeats.views.nestedscrolling.app.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import me.bytebeats.views.nestedscrolling.app.adapter.TextAdapter
import me.bytebeats.views.nestedscrolling.app.databinding.FragmentListViewBinding

class ListViewFragment : Fragment() {

    private lateinit var listViewModel: ListViewModel
    private var _binding: FragmentListViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter by lazy { TextAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listViewModel =
            ViewModelProvider(this).get(ListViewModel::class.java)

        _binding = FragmentListViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: ListView = binding.listView
        listView.adapter = adapter
        listViewModel.listData.observe(viewLifecycleOwner, { it ->
            adapter.add(it)
        })
        val text: TextView = binding.textHome
        text.setOnClickListener {
            listViewModel.generate(10)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}