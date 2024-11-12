package com.example.biometriatranca

import java.net.Socket
import kotlin.system.measureTimeMillis

fun main() {
    val endereco = "172.20.10.7"  // IP do PC na rede local
    val porta = 50000
    val mensagem = "Olá, servidor!"
    val tentativas = 10

    val temposResposta = mutableListOf<Double>()

    try {
        Socket(endereco, porta).use { cliente ->
            val entrada = cliente.getInputStream().bufferedReader()
            val saida = cliente.getOutputStream().bufferedWriter()

            println("Enviando mensagens para o servidor...")

            for (i in 1..tentativas) {
                val tempoResposta = measureTimeMillis {
                    saida.write("$mensagem\n")
                    saida.flush()
                    val resposta = entrada.readLine()  // Esperar a resposta (ACK) do servidor
                    println("Resposta recebida: $resposta")
                }.toDouble() / 1000  // Converter para segundos

                temposResposta.add(tempoResposta)

                println("Tentativa $i: Tempo de resposta = %.6f segundos".format(tempoResposta))
            }

            // Calculando estatísticas
            val menorTempo = temposResposta.minOrNull() ?: 0.0
            val maiorTempo = temposResposta.maxOrNull() ?: 0.0
            val mediaTempo = temposResposta.average()

            println("\nEstatísticas de tempo de resposta:")
            println("Menor tempo de resposta: %.6f segundos".format(menorTempo))
            println("Maior tempo de resposta: %.6f segundos".format(maiorTempo))
            println("Média de tempo de resposta: %.6f segundos".format(mediaTempo))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
