package com.sirimocr.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import com.sirimocr.app.databinding.FragmentSettingsBinding
import com.sirimocr.app.ui.viewmodels.SettingsViewModel
import com.sirimocr.app.ui.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as SirimOcrApplication }
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(app.authService, app.securePreferences, app.syncManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.autoSyncSwitch.isChecked = viewModel.autoSync.value
        binding.autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoSync(isChecked)
        }
        binding.signOutButton.setOnClickListener {
            viewModel.signOut {
                findNavController().navigate(R.id.loginFragment)
            }
        }
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.autoSync.collectLatest { enabled ->
                if (binding.autoSyncSwitch.isChecked != enabled) {
                    binding.autoSyncSwitch.isChecked = enabled
                }
            }
        }
    }
}
