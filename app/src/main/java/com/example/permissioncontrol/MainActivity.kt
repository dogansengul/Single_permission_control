package com.example.permissioncontrol

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity() {
    private var cameraResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted ->
            if(isGranted){
                Toast.makeText(this, "Permission is granted for camera.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission is not granted for camera.", Toast.LENGTH_SHORT).show()
            }
        }

    private var permissionRequestCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var sharedPreferences: SharedPreferences = getSharedPreferences("PermissionRequestCount", Context.MODE_PRIVATE)
        permissionRequestCount = sharedPreferences.getInt("PermissionRequestCount", 0)

        val counter = findViewById<TextView>(R.id.counter)
        counter.text = "${sharedPreferences.getInt("PermissionRequestCount", 0)}"

        val cameraButton = findViewById<Button>(R.id.camera_button)
        cameraButton.setOnClickListener {
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionRequestCount++
                sharedPreferences.edit().putInt("PermissionRequestCount", permissionRequestCount).apply()
                counter.text = "${sharedPreferences.getInt("PermissionRequestCount", 0)}"
            }
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showRationaleDialog()

            } else if(permissionRequestCount > 2 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                showSettingsDialog()

            } else {
                cameraResultLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private fun showRationaleDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera permission before. Please go to app settings and grant the permission.")
            .setPositiveButton("Okay") { _, _ -> cameraResultLauncher.launch(Manifest.permission.CAMERA) }
        alertDialog.show()
    }
    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Request")
            .setMessage("You have denied camera permission multiple times. Please go to app settings and grant the permission.")
            .setPositiveButton("Okay") { _, _ -> openAppSettings() }
            .show()
    }
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
