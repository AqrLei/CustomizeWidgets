package com.example.storesample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storesample.databinding.FragmentMainBinding
import com.example.storesample.ui.Action
import com.example.storesample.ui.ActionListAdapter

private val actionList = arrayOf(
    Action(R.string.demo_mediastore, R.id.action_mainFragment_to_mediaStoreFragment),
    Action(R.string.demo_saf, R.id.action_mainFragment_to_safFragment)
)
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = ActionListAdapter(actionList)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}