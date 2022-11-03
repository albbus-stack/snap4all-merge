while getopts a: flag
do
    case "${flag}" in
        a) addedPackages=${OPTARG};;
    esac
done

if [ "$addedPackages"="" ] ; then
    addedPackages="git,make,cmake,clang,coreutils,nano,nodejs,openssh"
fi

echo "[*] Package list: $addedPackages"

sudo apt upgrade -y && sudo apt update -y


echo "[*] Generating bootstrap..."

sudo apt-get install -y git
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

git clone https://github.com/termux/termux-packages

cd termux-packages/

sudo ./scripts/run-docker.sh
sudo ./scripts/generate-bootstrap.sh -a $addedPackages
exit

cd ..


echo "[*] Installing Java and Android Sdk..."

sudo apt-get install -y default-jre android-sdk unzip rsync

echo 'export ANDROID_HOME="/usr/lib/android-sdk"
export ANDROID_SDK_ROOT="/usr/lib/android-sdk"
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools/bin
' >> ~/.bashrc
source ~/.bashrc

wget https://dl.google.com/android/repository/commandlinetools-linux-6609375_latest.zip
unzip commandlinetools-linux-6609375_latest.zip -d cmdline-tools

sudo chmod -R +w $ANDROID_HOME

rsync cmdline-tools/tools/bin/ $ANDROID_HOME/tools/bin/
rm -rf cmdline-tools/tools/bin
rsync cmdline-tools/tools/ $ANDROID_HOME/tools/
rm -rf cmdline-tools

yes | $ANDROID_HOME/tools/bin/sdkmanager --sdk_root=${ANDROID_HOME} --licenses


echo "[*] Cloning and building app..."

# This has to point to the compile branch of the snap4all repo
git clone -b compile-branch --single-branch https://github.com/albbus-stack/snap4all

rm snap4all/app/src/main/cpp/bootstrap-*
mv termux-packages/bootstrap-* snap4all/app/src/main/cpp

./snap4all/gradlew assembleDebug


echo "[*] Apks generated in /build-output"

mv snap4all/app/build/outputs/apk/debug/*.apk build-output/

exit 0
