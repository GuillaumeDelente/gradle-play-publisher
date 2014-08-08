# gradle-play-publisher

Gradle plugin to upload your release APKs to the Google Play Store. Works with the android application plugin.

## Requirements

To use the publisher plugin you have to create a service account for your existing Google Play Account. See https://developers.google.com/android-publisher/getting_started for more information.

Once you finished the setup you have a so called *service account email address* and a *p12 key file* that we will use later on.

## Usage

Add it to your buildscript dependencies:

```
buildscript {

    repositories {
        mavenCentral()
    }
    
    dependencies {
        classpath 'com.github.triplet.gradle:play-publisher:+'
    }
}
```

Apply it:

```
apply plugin: 'play'
```

## Configuration

Once you have applied this plugin to your android application project you can configure it via the ```play``` closure. Drop in your service account email address and the p12 key file you generated in the API Console here.

```
play {
    serviceAccountEmail = 'your-service-account-email'
    pk12File = file('key.p12')
}
```

## Run

The plugin adds a publish task for every flavor of your app. So in the most simple case (where there are no flavors at all) that would be ```publishRelease```. If there are flavors like *free* and *paid* you would get the tasks ```publishFreeRelease```and ```publishPaidRelease```.