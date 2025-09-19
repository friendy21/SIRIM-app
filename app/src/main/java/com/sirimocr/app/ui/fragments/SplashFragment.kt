package com.sirimocr.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as SirimOcrApplication
        lifecycleScope.launch {
            delay(500)
            val destination = if (app.authService.currentUser() != null) {
                R.id.dashboardFragment
            } else {
                R.id.loginFragment
            }
            if (findNavController().currentDestination?.id == R.id.splashFragment) {
                findNavController().navigate(destination)
            }
        }
    }
}
