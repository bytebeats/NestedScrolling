package me.bytebeats.views.nestedscrolling.app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import me.bytebeats.views.nestedscrolling.app.R
import me.bytebeats.views.nestedscrolling.app.adapter.TextAdapter
import me.bytebeats.views.nestedscrolling.app.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val adapter by lazy { TextAdapter(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val listView: ListView = binding.listView
        listView.adapter = adapter
        notificationsViewModel.listData.observe(viewLifecycleOwner, { it ->
            adapter.add(it)
        })
        val text: TextView = binding.textNotifications
        text.setOnClickListener {
            notificationsViewModel.generate(10)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}