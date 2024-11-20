# Biometria Tranca📱 
## Projeto de Aplicativo Android com Biometria e Comunicação HTTP
Descrição
O projeto BiometriaTranca é um aplicativo Android que utiliza autenticação biométrica para desbloquear uma tranca e enviar notificações via SMS. Além disso, o aplicativo se comunica com um servidor HTTP para enviar mensagens e receber respostas. Este projeto é desenvolvido em Kotlin e utiliza várias bibliotecas e frameworks modernos para garantir uma experiência de usuário segura e eficiente.

Estrutura do Projeto
A estrutura do projeto é organizada da seguinte forma:

```
.
├── .gitignore
├── .idea/
├── app/
│   ├── build/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/biometriatranca/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ClienteHTTP.kt
│   │   │   │   ├── ClienteTCP.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   ├── ic_launcher_round.xml
│   │   │   ├── AndroidManifest.xml
│   │   ├── androidTest/
│   │   │   ├── java/com/example/biometriatranca/
│   │   │   │   ├── ExampleInstrumentedTest.kt
│   │   ├── test/
│   │   │   ├── java/com/example/biometriatranca/
│   │   │   │   ├── ExampleUnitTest.kt
├── build.gradle.kts
├── gradle/
│   ├── wrapper/
│   ├── libs.versions.toml
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
```

### Funcionalidades
#### Autenticação Biométrica 🔒

O aplicativo utiliza a biblioteca androidx.biometric para autenticação biométrica. A autenticação é configurada no arquivo MainActivity.kt:

```kotlin
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
```

#### Comunicação HTTP 📡
A classe ClienteHTTP é responsável por enviar mensagens para um servidor HTTP:

```kotlin
class ClienteHTTP(private val urlString: String) {
    suspend fun enviarMensagem(mensagem: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(urlString)
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    OutputStreamWriter(outputStream).apply {
                        write(mensagem)
                        flush()
                    }
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
```

#### Envio de SMS 📧
O aplicativo também pode enviar SMS após a autenticação bem-sucedida:

```kotlin
private fun sendSMS(phoneNumber: String, message: String) {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    } else {
        tvStatus.text = "Permissão para SMS não concedida"
    }
}
```

#### Configuração do Servidor Python
O servidor Python é configurado para receber requisições HTTP:

```python
# Dentro de servidor.py

from http.server import BaseHTTPRequestHandler, HTTPServer

class ServidorHTTP(BaseHTTPRequestHandler):
    def do_GET(self):
        # Log da requisição GET
        print(f'Recebida requisição GET de {self.client_address}')
        print(f'Headers: {self.headers}')

        # Enviar resposta
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        resposta = 'Resposta ao GET Estou conectado PORRA!'
        self.wfile.write(resposta.encode())

    def do_POST(self):
        # Log da requisição POST
        print(f'Recebida requisição POST de {self.client_address}')
        print(f'Headers: {self.headers}')

        # Ler o comprimento do conteúdo
        content_length = int(self.headers.get('Content-Length', 0))
        if content_length > 0:
            # Ler os dados POST
            post_data = self.rfile.read(content_length)
            print(f'Dados recebidos: {post_data.decode()}')
        else:
            print('Nenhum dado recebido')

        # Enviar resposta
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        resposta = 'Resposta ao POST: Olá, mundo!'
        self.wfile.write(resposta.encode())

if __name__ == '__main__':
    servidor_ip = '0.0.0.0'  # Escuta em todas as interfaces
    porta = 8080
    servidor = HTTPServer((servidor_ip, porta), ServidorHTTP)
    print(f'Servidor HTTP rodando em http://{servidor_ip}:{porta}')
    servidor.serve_forever()
```
#### Dependências
As dependências do projeto são gerenciadas pelo Gradle e estão definidas nos arquivos build.gradle.kts e libs.versions.toml:

```kotlin
dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

#### Testes
O projeto inclui testes unitários e instrumentados:

Testes Unitários: Localizados em ExampleUnitTest.kt
Testes Instrumentados: Localizados em ExampleInstrumentedTest.kt
Configuração do Ambiente
Para configurar o ambiente de desenvolvimento, siga os passos abaixo:

Clone o repositório:

```git
git clone https://github.com/seu-usuario/BiometriaTranca.git
```

Entre na pasta

```terminal
cd BiometriaTranca
```

Execute o aplicativo:

Abra o projeto no Android Studio e clique em "Run".

Contribuição
Contribuições são bem-vindas! Sinta-se à vontade para abrir issues e pull requests.

### Licença
Este projeto está licenciado sob a Licença Apache 2.0. Veja o arquivo LICENSE para mais detalhes.

Esperamos que este README forneça uma visão clara e abrangente do projeto BiometriaTranca. Se tiver alguma dúvida, não hesite em entrar em contato! 🚀



