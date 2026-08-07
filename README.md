# 🚌 Rota Escolar (`App-perua`)

Aplicativo Android moderno desenvolvido em Kotlin com Jetpack Compose para rastreamento em tempo real e comunicação no transporte escolar.

---

## 📌 Visão Geral

O **Rota Escolar** visa conectar motoristas de transporte escolar e pais/responsáveis em uma única plataforma intuitiva, segura e ágil. 

O aplicativo suporta alternância de perfis e funcionalidades dedicadas para cada tipo de usuário:

- **🚌 Perfil Motorista**:
  - **Rota GPS**: Visualização e transmissão da rota em tempo real.
  - **Comunicados**: Envio de avisos instantâneos aos pais.
  - **Pix**: Configuração e compartilhamento de chave Pix para recebimento de mensalidades.
  - **Arquitetura**: Visualização técnica do modelo de dados Supabase e fluxos em tempo real.

- **👨‍👩‍👧 Perfil Responsável (Pais)**:
  - **Mapa Vivo**: Acompanhamento no mapa do veículo escolar em movimento.
  - **Mural**: Recebimento de alertas e comunicados importantes do motorista.
  - **Pagamento**: Histórico e facilidade de pagamento de mensalidades via QR Code Pix.
  - **Arquitetura**: Informações de transparência e sincronização de dados.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Kotlin (JVM 17)
- **UI Framework**: Jetpack Compose com Material Design 3
- **Arquitetura**: MVVM com `StateFlow` e `Coroutines`
- **Backend / Realtime**: Supabase (PostgreSQL, Realtime Subscriptions & Auth)
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
3. Copie o arquivo `.env.example` para `.env` e configure suas credenciais do Supabase:
   ```env
   SUPABASE_URL=https://seu-projeto.supabase.co
   SUPABASE_ANON_KEY=sua-chave-anon-key
   ```
4. Sincronize os arquivos Gradle (`Sync Project with Gradle Files`).
5. Execute a aplicação (`Run 'app'`).

---

## 🔄 Integração Contínua (CI)

O repositório conta com **GitHub Actions** configurado para validar builds e testes de forma automatizada em cada push e pull request.

---

## 📄 Licença

Este projeto é de uso restrito e desenvolvimento contínuo para transporte escolar.
