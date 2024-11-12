package com.example.biometriatranca

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ClienteHTTP(private val urlString: String) {

    suspend fun enviarMensagem(mensagem: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    // Enviar dados
                    OutputStreamWriter(outputStream).apply {
                        write(mensagem)
                        flush()
                    }
                    // Ler a resposta
                    val responseCode = responseCode
                    val resposta = inputStream.bufferedReader().readText()
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        resposta
                    } else {
                        "Erro: código de resposta $responseCode"
                    }
                }
            } catch (e: Exception) {
                "Erro: ${e.message}"
            }
        }
    }
}