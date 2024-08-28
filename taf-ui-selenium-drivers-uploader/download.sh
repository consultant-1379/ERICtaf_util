#!/bin/bash

export http_proxy="http://www-proxy.ericsson.se:8080"
export https_proxy="https://www-proxy.ericsson.se:8080"

set -e

if [ "$1" == "" ]; then
    targetDir=target/resources
else
    targetDir=$1
fi

if [ ! -d $targetDir ]; then
    mkdir -p $targetDir
fi

function fixZip() {
    filename=$1
    classifier=$2

    # Determine extension
    if [[ "$classifier" == *win* ]]
    then
        extension=.exe
    else
        extension=
    fi

    # Extract the existing driver name
    oldFileName=$(unzip -t $filename |grep "testing: "|cut -d : -f 2|cut -d ' ' -f 2)
    
    #unpack the zip file, rename it and repack it    
    unzip -o $filename $oldFileName
    mv $oldFileName $classifier$extension
    rm -f $filename
    zip $classifier $classifier$extension


}

pushd $targetDir
wget http://selenium-release.storage.googleapis.com/2.53/IEDriverServer_Win32_2.53.0.zip
fixZip IEDriverServer_Win32_2.53.0.zip ie-win-x32

wget http://selenium-release.storage.googleapis.com/2.53/IEDriverServer_x64_2.53.0.zip
fixZip IEDriverServer_x64_2.53.0.zip ie-win-x64

wget http://chromedriver.storage.googleapis.com/2.19/chromedriver_linux32.zip
fixZip chromedriver_linux32.zip chrome-linux-x32

wget http://chromedriver.storage.googleapis.com/2.19/chromedriver_linux64.zip
fixZip chromedriver_linux64.zip chrome-linux-x64

wget http://chromedriver.storage.googleapis.com/2.19/chromedriver_mac32.zip
fixZip chromedriver_mac32.zip chrome-mac-x32

wget http://chromedriver.storage.googleapis.com/2.19/chromedriver_win32.zip
fixZip chromedriver_win32.zip chrome-win-x32

#There are no 2.19 Chrome drivers for Win x64 and Mac x64
cp chrome-mac-x32.zip chrome-mac-x64.zip
fixZip chrome-mac-x64.zip chrome-mac-x64

cp chrome-win-x32.zip chrome-win-x64.zip
fixZip chrome-win-x64.zip chrome-win-x64

#PhantomJS tar/zip files contain examples so extract, move executable file to a new folder and zip that folder
curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-phantomjs/3.141.59-MARK/taf-ui-phantomjs-3.141.59-MARK-linux-x86_64.tar.bz2
tar -vxf taf-ui-phantomjs-3.141.59-MARK-linux-x86_64.tar.bz2
mv phantomjs-2.1.1-linux-x86_64/bin/phantomjs .
zip phantomjs-linux-x64.zip phantomjs
rm -rf taf-ui-phantomjs-3.141.59-MARK-linux-x86_64.tar.bz2 phantomjs-2.1.1-linux-x86_64 phantomjs

curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-phantomjs/3.141.59-MARK/taf-ui-phantomjs-3.141.59-MARK-windows.zip
unzip taf-ui-phantomjs-3.141.59-MARK-windows.zip
mv phantomjs-2.1.1-windows/bin/phantomjs.exe .
zip phantomjs-win.zip phantomjs.exe
rm -rf taf-ui-phantomjs-3.141.59-MARK-windows.zip phantomjs-2.1.1-windows phantomjs.exe

#GeckoDriver for WINDOWS x32 && x64
curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-geckodrivers/3.141.59-MARK/taf-ui-geckodrivers-3.141.59-MARK-win32.zip
fixZip taf-ui-geckodrivers-3.141.59-MARK-win32.zip geckodriver-win-x32

curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-geckodrivers/3.141.59-MARK/taf-ui-geckodrivers-3.141.59-MARK-win64.zip
fixZip taf-ui-geckodrivers-3.141.59-MARK-win64.zip geckodriver-win-x64

#GeckoDriver for LINUX x32 && x64
curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-geckodrivers/3.141.59-MARK/taf-ui-geckodrivers-3.141.59-MARK-linux32.tar.gz
tar -xvf taf-ui-geckodrivers-3.141.59-MARK-linux32.tar.gz
chmod +x geckodriver
mv geckodriver geckodriver-linux-x32
zip geckodriver-linux-x32 geckodriver-linux-x32
rm -rf taf-ui-geckodrivers-3.141.59-MARK-linux32.tar.gz

curl -LO --noproxy '*' https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/cifwk/selenium/drivers/taf-ui-geckodrivers/3.141.59-MARK/taf-ui-geckodrivers-3.141.59-MARK-linux64.tar.gz
tar -xvf taf-ui-geckodrivers-3.141.59-MARK-linux64.tar.gz
chmod +x geckodriver
mv geckodriver geckodriver-linux-x64
zip geckodriver-linux-x64 geckodriver-linux-x64
rm -rf taf-ui-geckodrivers-3.141.59-MARK-linux64.tar.gz
popd
