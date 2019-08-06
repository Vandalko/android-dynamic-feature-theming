#!/bin/bash

./gradlew :app:bundleDebug
java -jar bundletool/bundletool-all-0.10.2.jar build-apks --overwrite --bundle=app/build/outputs/bundle/debug/app.aab --output=app/build/outputs/bundle/debug/app.apks
java -jar bundletool/bundletool-all-0.10.2.jar install-apks --apks=app/build/outputs/bundle/debug/app.apks