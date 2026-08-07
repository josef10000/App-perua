# 🚌 Rota Escolar (`App-perua`)

Aplicativo Android moderno desenvolvido em Kotlin com Jetpack Compose para rastreamento em tempo real e comunicação no transporte escolar, integrado **100% com os serviços Firebase**.

---

## 📌 Visão Geral

O **Rota Escolar** conecta motoristas de transporte escolar e pais/responsáveis em uma única plataforma intuitiva, segura e ágil.

O aplicativo suporta alternância de perfis e funcionalidades dedicadas para cada tipo de usuário:

- **🚌 Perfil Motorista**:
  - **Rota GPS**: Transmissão em tempo real das coordenadas GPS da perua escolar via Firebase Realtime Database.
  - **Comunicados**: Envio de avisos e notificações instantâneas via Cloud Firestore & Firebase Cloud Messaging (FCM).
  - **Pix**: Configuração e compartilhamento de chave Pix para recebimento de mensalidades.
  - **Arquitetura Firebase**: Visualização técnica das coleções do Firestore, regras de segurança e streaming do Realtime DB.

- **👨‍👩‍👧 Perfil Responsável (Pais)**:
  - **Mapa Vivo**: Acompanhamento no mapa da perua escolar em movimento com baixíssima latência.
  - **Mural**: Recebimento de alertas e comunicados importantes do motorista.
  - **Pagamento**: Histórico e facilidade de pagamento de mensalidades via QR Code Pix.
  - **Arquitetura Firebase**: Informações de transparência, sincronização de dados e segurança das regras do Firebase.

---

## 🛠️ Stack Tecnológica & Serviços Firebase

- **Linguagem**: Kotlin (JVM 17)
- **UI Framework**: Jetpack Compose com Material Design 3
- **Arquitetura Android**: MVVM com `StateFlow` e `Coroutines`
- **Backend Services (100% Firebase)**:
  - 🔐 **Firebase Authentication**: Autenticação segura de motoristas e pais.
  - ⚡ **Firebase Realtime Database**: Rastreamento da localização GPS em tempo real com baixíssima latência.
  - 📦 **Cloud Firestore**: Armazenamento de alunos, rotas, comunicados e pagamentos.
  - 🔔 **Firebase Cloud Messaging (FCM)**: Push Notifications e alertas de aproximação do transporte.
- **Compilação**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Android Studio Ladybug (ou versão mais recente)
- JDK 17 configurado
- Dispositivo Android (versão 8.0 / API 26 ou superior) ou Emulador Android

### Passos
1. Clone o repositório:
   ```bash
   git clone https://github.com/josef10000/App-perua.git
   ```
2. Abra o projeto no Android Studio.
3. Baixe o arquivo `google-services.json` do seu Console do Firebase e adicione na pasta `app/`:
   ```bash
   cp app/google-services.json.example app/google-services.json
   ```
4. Sincronize os arquivos Gradle (`Sync Project with Gradle Files`).
5. Execute a aplicação (`Run 'app'`).

---

## 🔄 Integração Contínua (CI)

O repositório conta com **GitHub Actions** configurado para validar builds e testes de forma automatizada em cada push e pull request.

---

## 📄 Licença

Este projeto é de uso restrito e desenvolvimento contínuo para transporte escolar.
