package com.example.biometriatranca

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnUnlock: Button
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnUnlock = findViewById(R.id.btnUnlock)

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
                    sendSMS("21988366294", "Tranca Desbloqueada") // Número de teste
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
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
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
}
