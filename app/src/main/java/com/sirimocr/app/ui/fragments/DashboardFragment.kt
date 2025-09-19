package com.sirimocr.app.ui.fragments

import android.Manifest

import android.content.Context

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.sirimocr.app.R
import com.sirimocr.app.SirimOcrApplication
import com.sirimocr.app.databinding.FragmentDashboardBinding
import com.sirimocr.app.ocr.MLKitOcrProcessor
import com.sirimocr.app.utils.ImageUtils
import com.sirimocr.app.ui.viewmodels.DashboardViewModel
import com.sirimocr.app.ui.viewmodels.DashboardViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.sirimocr.app.ui.viewmodels.DashboardViewModel
import com.sirimocr.app.ui.viewmodels.DashboardViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val app by lazy { requireActivity().application as SirimOcrApplication }
    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(app.repository, app.authService)
    }

    private var cameraExecutor: ExecutorService? = null
    private val ocrProcessor = MLKitOcrProcessor()
    private var imageCapture: ImageCapture? = null
    private lateinit var imageUtils: ImageUtils
    private var analysisJob: Job? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else Toast.makeText(
                requireContext(),
                getString(R.string.message_camera_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.captureButton.setOnClickListener { capturePhoto() }
        binding.captureButton.setOnClickListener { viewModel.saveCurrentResult() }
        observeViewModel()
        ensureCameraPermission()
    }

    override fun onDestroyView() {
        analysisJob?.cancel()
        cameraExecutor?.shutdown()
        imageCapture = null
        _binding = null
        super.onDestroyView()
    }

    private fun ensureCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> startCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.previewView.surfaceProvider)
        }
        val executor = cameraExecutor ?: return
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        analysis.setAnalyzer(executor) { imageProxy ->
            analysisJob?.cancel()
            analysisJob = viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val result = ocrProcessor.process(imageProxy)
                    viewModel.updateOcrResult(result)
                } finally {
                    imageProxy.close()
                }
            }
        }
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(binding.previewView.display?.rotation ?: 0)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                analysis
            )
        } catch (t: Throwable) {
            Toast.makeText(
                requireContext(),
                getString(R.string.message_camera_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ocrResult.collectLatest { result ->
                if (result?.success == true && result.sirimData != null) {
                    binding.ocrSummaryCard.visibility = View.VISIBLE
                    binding.ocrDetails.text = buildString {
                        append("Serial: ${result.sirimData.sirimSerialNo}\n")
                        result.sirimData.batchNo?.let { append("Batch: $it\n") }
                        result.sirimData.brandTrademark?.let { append("Brand: $it\n") }
                    }
                    binding.confidenceProgress.setProgressCompat((result.confidenceScore * 100).toInt(), true)
                    binding.ocrStatus.text = "Confidence ${(result.confidenceScore * 100).toInt()}%"
                } else {
                    binding.ocrSummaryCard.visibility = View.GONE
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saveError.collectLatest { error ->
                error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.saving.collectLatest { saving ->
                binding.captureButton.isEnabled = !saving
                binding.captureButton.alpha = if (saving) 0.5f else 1f
                binding.captureProgress.visibility = if (saving) View.VISIBLE else View.GONE
            }
        }
    }

    private fun capturePhoto() {
        val currentResult = viewModel.ocrResult.value
        if (currentResult?.success != true || currentResult.sirimData == null) {
            Toast.makeText(requireContext(), getString(R.string.message_capture_requires_result), Toast.LENGTH_LONG).show()
            return
        }
        if (currentResult.sirimData.sirimSerialNo.isNullOrBlank()) {
            Toast.makeText(requireContext(), getString(R.string.message_capture_requires_result), Toast.LENGTH_LONG).show()
            return
        }
        val imageCapture = imageCapture ?: run {
            Toast.makeText(requireContext(), getString(R.string.message_camera_unavailable), Toast.LENGTH_LONG).show()
            return
        }
        val photoFile = imageUtils.createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        binding.captureButton.isEnabled = false
        binding.captureButton.alpha = 0.5f
        binding.captureProgress.visibility = View.VISIBLE
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(), getString(R.string.message_capture_failed), Toast.LENGTH_LONG).show()
                    photoFile.delete()
                    binding.captureProgress.visibility = View.GONE
                    binding.captureButton.isEnabled = true
                    binding.captureButton.alpha = 1f
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val processedPath = withContext(Dispatchers.IO) {
                                imageUtils.prepareImage(photoFile)
                            }
                            val launched = viewModel.saveCurrentResult(processedPath)
                            if (launched) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.message_capture_saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                photoFile.delete()
                                binding.captureProgress.visibility = View.GONE
                                binding.captureButton.isEnabled = true
                                binding.captureButton.alpha = 1f
                            }
                        } catch (t: Throwable) {
                            Toast.makeText(requireContext(), getString(R.string.message_capture_failed), Toast.LENGTH_LONG)
                                .show()
                            photoFile.delete()
                            binding.captureProgress.visibility = View.GONE
                            binding.captureButton.isEnabled = true
                            binding.captureButton.alpha = 1f
                        }
                    }
                }
            }
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        imageUtils = ImageUtils(context.applicationContext)
    }
}
