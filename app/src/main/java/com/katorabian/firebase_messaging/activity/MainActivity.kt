package com.katorabian.firebase_messaging.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.katorabian.firebase_messaging.R
import com.katorabian.firebase_messaging.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.switchPermission.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) askNotificationPermission()
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.tvToken.text = task.result
                return@OnCompleteListener
            }
        })
        binding.btCopyToken.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("token", binding.tvToken.text)
            clipboard.setPrimaryClip(clip)
        }
    }

    override fun onResume() {
        super.onResume()
        askNotificationPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Разрешение на показ уведомлений получено", Toast.LENGTH_SHORT).show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle("Уведомление")
                .setMessage("Вы не сможете получать уведомления, пока вы не дадите разрешение на показ уведомлений")
                .show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = isNotifPermGranted()
            showHideSwitch(isGranted)
            if (isGranted) return
            else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Уведомление")
                    .setMessage("Дайте разрешение на показ уведомлений, чтобы получать уведомления")
                    .setPositiveButton("Ок") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }.setNegativeButton("Нет, спасибо") { _, _ ->
                        updatePermissionsState()
                    }
                    .setOnCancelListener {
                        updatePermissionsState()
                    }
                    .show()

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotifPermGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    private fun showHideSwitch(flag: Boolean) {
        binding.switchPermission.isChecked = flag
        binding.switchContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            height = if (flag) 0 else LinearLayout.LayoutParams.WRAP_CONTENT
        }
        binding.tvPermissionsTitle.updateLayoutParams<LinearLayout.LayoutParams> {
            height = if (flag) 0 else LinearLayout.LayoutParams.WRAP_CONTENT
        }
    }

    @SuppressLint("NewApi")
    private fun updatePermissionsState() {
        val isGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || isNotifPermGranted()
        showHideSwitch(isGranted)
    }
}