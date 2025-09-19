package com.sirimocr.app.ui.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import com.sirimocr.app.databinding.ActivityMainBinding
import com.sirimocr.app.ui.viewmodels.MainViewModel
import com.sirimocr.app.ui.viewmodels.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    private val app by lazy { application as SirimOcrApplication }

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(app.authService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setupAuthListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::authListener.isInitialized) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener)
        }
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController
        binding.bottomNavigation.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility = when (destination.id) {
                R.id.loginFragment, R.id.splashFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun setupAuthListener() {
        authListener = FirebaseAuth.AuthStateListener { auth ->
            val destination = if (auth.currentUser == null) R.id.loginFragment else R.id.dashboardFragment
            if (navController.currentDestination?.id != destination) {
                navController.navigate(destination)
            }
            viewModel.refreshAuthState()
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }
}
