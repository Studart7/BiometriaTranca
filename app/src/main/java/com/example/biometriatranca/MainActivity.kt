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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import java.net.Socket
import kotlin.system.measureTimeMillis

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
        btnUnlock.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

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
                    connectToServerHTTP()
                   tvStatus.text = "Tranca Desbloqueada"
                    sendSMS("21988366294", "Tranca Desbloqueada")
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

    // private fun connectToServerTCPIP() {
    //     CoroutineScope(Dispatchers.IO).launch {
    //         val url = URL("http://172.20.10.7:50000")
    //         val mensagem = "{\"mensagem\":\"Olá, servidor!\"}"

    //         try {
    //             with(url.openConnection() as HttpURLConnection) {
    //                 requestMethod = "POST"
    //                 setRequestProperty("Content-Type", "application/json")
    //                 doOutput = true

    //                 // Enviar dados
    //                 val outputStream = OutputStreamWriter(outputStream)
    //                 outputStream.write(mensagem)
    //                 outputStream.flush()

    //                 // Ler a resposta
    //                 val responseCode = responseCode
    //                 val resposta = inputStream.bufferedReader().readText()

    //                 withContext(Dispatchers.Main) {
    //                     if (responseCode == 200) {
    //                         tvStatus.text = "Resposta recebida: $resposta"
    //                     } else {
    //                         tvStatus.text = "Erro: código de resposta $responseCode"
    //                     }
    //                 }
    //             }
    //         } catch (e: Exception) {
    //             withContext(Dispatchers.Main) {
    //                 tvStatus.text = "Erro: ${e.message}"
    //             }
    //         }
    //     }
    // }

    private fun connectToServerHTTP() {
        val clienteHTTP = ClienteHTTP("http://172.20.10.7:8080")
        CoroutineScope(Dispatchers.Main).launch {
            val mensagem = "{\"mensagem\":\"Olá, servidor!\"}"
            val resposta = clienteHTTP.enviarMensagem(mensagem)
            tvStatus.text = resposta
        }
    }
}