package com.sirimocr.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import com.sirimocr.app.databinding.FragmentHistoryBinding
import com.sirimocr.app.ui.adapters.RecordsAdapter
import com.sirimocr.app.ui.viewmodels.HistoryViewModel
import com.sirimocr.app.ui.viewmodels.HistoryViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as SirimOcrApplication }
    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(app.repository, app.authService, app.exportUtils)
    }

    private val adapter = RecordsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recordsList.layoutManager = LinearLayoutManager(requireContext())
        binding.recordsList.adapter = adapter
        binding.exportButton.setOnClickListener { showExportDialog() }
        observeViewModel()
        viewModel.refreshUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeViewModel() {
        viewModel.records.observe(viewLifecycleOwner, Observer { records ->
            adapter.submitList(records)
            binding.emptyState.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        })
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exportResult.collectLatest { uri ->
                uri?.let {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.message_export_success, it.path),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.clearExportState()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exportError.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), getString(R.string.message_export_failed), Toast.LENGTH_LONG)
                        .show()
                    viewModel.clearExportState()
                }
            }
        }
    }

    private fun showExportDialog() {
        val records = adapter.currentList
        if (records.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.message_no_records_export), Toast.LENGTH_LONG)
                .show()
            return
        }
        val options = arrayOf(
            getString(R.string.export_csv),
            getString(R.string.export_excel),
            getString(R.string.export_pdf)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.title_export))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.exportCsv(records)
                    1 -> viewModel.exportExcel(records)
                    2 -> viewModel.exportPdf(records)
                }
            }
            .show()
    }
}
