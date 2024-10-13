package com.example.biometriatranca

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.biometric.BiometricPrompt
import com.example.biometriatranca.R
import com.example.biometriatranca.analyzeMessageDeliveryTimes
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvAnalysisResults: TextView
    private lateinit var btnUnlock: Button
    private lateinit var etPhoneNumber: EditText
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor
    private var sendTime: Long = 0
    private val deliveryTimes = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvAnalysisResults = findViewById(R.id.tvAnalysisResults)
        btnUnlock = findViewById(R.id.btnUnlock)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)

        // Executor para a biometria
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    tvStatus.text = "Autenticação falhou: $errString"
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    tvStatus.text = "Tranca Desbloqueada"
                    val phoneNumber = etPhoneNumber.text.toString()
                    if (phoneNumber.isNotEmpty()) {
                        sendSMS(phoneNumber, "Tranca Desbloqueada")
                    } else {
                        Toast.makeText(this@MainActivity, "Please enter a phone number", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    tvStatus.text = "Autenticação falhou. Tente novamente."
                }
            })

        // Informações da autenticação
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticar com Biometria")
            .setSubtitle("Use sua impressão digital para desbloquear")
            .setNegativeButtonText("Cancelar")
            .build()

        // Evento do botão
        btnUnlock.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        // Solicitar permissão para SMS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestSMSPermission()
        }

        // Register receiver for delivery reports
        registerReceiver(smsDeliveryReceiver, IntentFilter("SMS_DELIVERED"))
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            val sentIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
            val deliveredIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_DELIVERED"), 0)
            sendTime = System.currentTimeMillis()
            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
        } else {
            tvStatus.text = "Permissão para SMS não concedida"
        }
    }

    private fun requestSMSPermission() {
        val permission = Manifest.permission.SEND_SMS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        }
    }

    private val smsDeliveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "SMS_DELIVERED") {
                val deliveryTime = System.currentTimeMillis()
                val timeTaken = deliveryTime - sendTime
                deliveryTimes.add(timeTaken)
                val analysisResults = analyzeMessageDeliveryTimes(deliveryTimes)
                tvAnalysisResults.text = analysisResults
                Toast.makeText(context, "SMS delivered in $timeTaken ms", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsDeliveryReceiver)
    }
}