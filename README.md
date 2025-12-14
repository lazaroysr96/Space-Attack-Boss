Space Attack Boss
=================

Autor: Lazaro Yunier Salazar Rodriguez

Juego de disparos espaciales para Android.

Instalación rápida (Linux)
--------------------------

1) Clonar y entrar al proyecto:
   git clone https://github.com/lazaroysr96/Space-Attack-Boss.git
   cd Space-Attack-Boss

2) Configurar SDK Android (ejemplo):
   export ANDROID_HOME="$HOME/android-sdk"
   export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$(pwd)/gradle-5.6.4/bin"

3) Compilar APK debug:
   ./gradle-5.6.4/bin/gradle assembleDebug

4) Instalar en dispositivo conectado por ADB:
   adb install -r app/build/outputs/apk/debug/app-debug.apk

5) Lanzar el juego:
   adb shell am start -n cu.spaceattack.boss/.MainActivity
