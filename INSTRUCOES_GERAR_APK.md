# 📦 Como Gerar e Distribuir o APK do Rota Escolar (`App-perua`)

Este guia explica como gerar o arquivo de instalação **`.apk`** do aplicativo **Rota Escolar** fora da Google Play Store para distribuição direta a motoristas e pais via WhatsApp, e-mail ou Google Drive.

---

## 🚀 Método 1: Download Automático pelo GitHub (Mais Fácil)

Como o repositório possui a pipeline do **GitHub Actions** configurada:

1. Acesse o seu repositório no GitHub: [https://github.com/josef10000/App-perua](https://github.com/josef10000/App-perua)
2. Clique na aba **Actions** no topo da página.
3. Clique na execução mais recente da lista de workflows (*Android CI Workflow*).
4. No final da página de detalhes do workflow, haverá uma seção chamada **Artifacts** contendo o arquivo `app-debug-apk`.
5. Clique no artefato para baixar o arquivo `.zip` contendo o arquivo `.apk` pronto para instalar!

---

## 💻 Método 2: Gerar o APK no seu Computador (via Terminal)

Caso queira gerar o arquivo `.apk` manualmente na sua máquina:

1. Abra o terminal (PowerShell ou CMD) na pasta do projeto:
   `C:\Users\JoséFrazãodaSilvaNet\.gemini\antigravity\scratch\App-perua`
2. Execute o comando de compilação do Gradle:
   ```powershell
   .\gradlew assembleDebug
   ```
3. Assim que a mensagem `BUILD SUCCESSFUL` aparecer, o arquivo `.apk` compilado estará disponível na seguinte pasta:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Pronto! Esse arquivo `app-debug.apk` é o instalador que você pode enviar para quem quiser.

---

## 📲 Como os Motoristas e Pais Instalam no Celular Android

Quando você enviar o arquivo `app-debug.apk` pelo WhatsApp ou Google Drive para os motoristas e pais:

1. O usuário toca no arquivo `.apk` recebido no WhatsApp ou gerenciador de arquivos do celular.
2. Caso seja a primeira vez que instala um aplicativo direto, o Android mostrará a seguinte mensagem:
   > *"Por motivos de segurança, o seu telefone não tem permissão para instalar apps desconhecidos desta fonte."*
3. O usuário clica em **Configurações** e ativa a opção **"Permitir desta fonte"** (ou "Instalar apps desconhecidos").
4. Retorna à tela anterior e clica em **Instalar**.
5. O aplicativo **Rota Escolar** estará instalado no celular e pronto para uso!

---

## ⚙️ Pré-requisito do Firebase no Aplicativo

Para que o app se conecte ao seu projeto no console do Firebase:
- Lembre-se de colocar o arquivo `google-services.json` que você baixou do Console do Firebase dentro da pasta `app/` do projeto antes de gerar a versão final de produção.
