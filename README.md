# Impersonate

[![Curseforge](https://curse.nikky.moe/api/img/359522?logo)](https://www.curseforge.com/projects/359522) [![](https://jitpack.io/v/Ladysnake/Impersonate.svg)](https://jitpack.io/#Ladysnake/Impersonate)

A library handling player impersonation.

## Adding Impersonate to your project

You can add the library by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { 
        name = "Jitpack"
        url = 'https://jitpack.io' }
    }
    maven {
        name = "NerdHubMC"
        url = "https://maven.abusedmaster.xyz"
    }
}

dependencies {
    modImplementation "io.github.ladysnake:Impersonate:${impersonate_version}"
    include "io.github.ladysnake:Impersonate:${impersonate_version}"
}
```

You can then add the library version to your `gradle.properties`file:

```properties
# Impersonate
impersonate_version = 1.x.y
```

You can find the current version of Impersonate in the [releases](https://github.com/Ladysnake/Impersonate/releases) tab of the repository on Github.

## Using Impersonate

You can find a couple examples in the [Test Mod](https://github.com/Ladysnake/Impersonate/tree/master/src/testmod/java/io/github/ladysnake/impersonatest).

```java
// TODO
```
