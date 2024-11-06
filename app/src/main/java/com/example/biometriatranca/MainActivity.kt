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
                    connectToServer()
//                    tvStatus.text = "Tranca Desbloqueada"
                    sendSMS("21988366294", "Tranca Desbloqueada")
                    connectToServer()
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

    private fun connectToServer() {
        CoroutineScope(Dispatchers.IO).launch {
            val endereco = "172.20.10.7"  // IP do PC na rede local
            val porta = 50000
            val mensagem = "Olá, servidor!"
            val tentativas = 10

            val temposResposta = mutableListOf<Double>()

            try {
                Socket(endereco, porta).use { cliente ->
                    val entrada = cliente.getInputStream().bufferedReader()
                    val saida = cliente.getOutputStream().bufferedWriter()

                    for (i in 1..tentativas) {
                        val tempoResposta = measureTimeMillis {
                            saida.write("$mensagem\n")
                            saida.flush()
                            val resposta = entrada.readLine()
                            withContext(Dispatchers.Main) {
                                tvStatus.text = "Resposta recebida: $resposta"
                            }
                        }.toDouble() / 1000

                        temposResposta.add(tempoResposta)
                    }

                    val menorTempo = temposResposta.minOrNull() ?: 0.0
                    val maiorTempo = temposResposta.maxOrNull() ?: 0.0
                    val mediaTempo = temposResposta.average()

                    withContext(Dispatchers.Main) {
                        tvStatus.text = """
                            Menor tempo de resposta: %.6f segundos
                            Maior tempo de resposta: %.6f segundos
                            Média de tempo de resposta: %.6f segundos
                        """.trimIndent().format(menorTempo, maiorTempo, mediaTempo)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
//                    tvStatus.text = "Conexão bem permitida!"
                    tvStatus.text = "Erro: ${e.message}"
                }
            }
        }
    }
}