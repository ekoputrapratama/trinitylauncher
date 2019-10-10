#/usr/bin/env sh
export ANDROID_HOME=/home/muhammadsayuti/Android/Sdk
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
./gradlew installFlavorDefaultDebug && adb shell am start -n com.fisma.trinity.debug/com.fisma.trinity.activity.OnBoardActivity