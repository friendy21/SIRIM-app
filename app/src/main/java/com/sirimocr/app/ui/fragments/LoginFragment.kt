package com.sirimocr.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import com.sirimocr.app.databinding.FragmentLoginBinding
import com.sirimocr.app.ui.viewmodels.LoginViewModel
import com.sirimocr.app.ui.viewmodels.LoginViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as SirimOcrApplication }

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(app.authService)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text?.toString().orEmpty()
            val password = binding.passwordInput.text?.toString().orEmpty()
            viewModel.signIn(email, password)
        }
        binding.registerButton.setOnClickListener {
            val email = binding.emailInput.text?.toString().orEmpty()
            val password = binding.passwordInput.text?.toString().orEmpty()
            viewModel.register(email, password)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.loginButton.isEnabled = !state.loading
                binding.registerButton.isEnabled = !state.loading
                if (state.error != null) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
                if (state.authenticated && findNavController().currentDestination?.id == R.id.loginFragment) {
                    findNavController().navigate(R.id.dashboardFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
