# Impersonate

[![Curseforge](https://curse.nikky.moe/api/img/359522?logo)](https://www.curseforge.com/projects/359522) [![](https://jitpack.io/v/Ladysnake/Impersonate.svg)](https://jitpack.io/#Ladysnake/Impersonate)

A library handling player impersonation.

## Adding PAL to your project

You can add the library by inserting the following in your `build.gradle` :

```gradle
repositories {
	maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "io.github.ladysnake:Impersonate:${pal_version}"
    include "io.github.ladysnake:Impersonate:${pal_version}"
}
```

You can then add the library version to your `gradle.properties`file:

```properties
# PlayerAbilityLib
pal_version = 1.x.y
```

You can find the current version of PAL in the [releases](https://github.com/Ladysnake/PlayerAbilityLib/releases) tab of the repository on Github.

## Using PAL

You can find a couple examples in the [Test Mod](https://github.com/Ladysnake/Impersonate/tree/master/src/testmod/java/io/github/ladysnake/impersonatest).

```java
```
