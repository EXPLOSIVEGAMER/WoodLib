[![jitpack](https://jitpack.io/v/EXPLOSIVEGAMER/WoodLib.svg)](https://jitpack.io/#EXPLOSIVEGAMER/WoodLib)

# WoodLib
WoodLib is an Bukkit Plugin library made by me (WoodEXPLOSIVE).

## Features:

<details>
<summary>Spoiler</summary>

- AbstractItemBuilder
- ItemBuilder
- Scheduler (Bukkit + Async Millisecond Scheduler)
## Gui Lib:
- Interfaces for all Elements for easy extending
### Elements:
- GuiElement (using the AbstractItemBuilder)
- Tab
- PagedTab
### Guis:
- SimpleGui
- SimplePagedGui
- SimpleTabbedGui
- SimpleTabbedPagedGui

##Utilities:
- MathUtil

And many more to come


</details>



You can use this Library the following way:

### Standalone Plugin

for example

build.gradle
```groovy
repositories {
    mavenCentral()
    maven {
      url "https://jitpack.io"
    }
}

dependencies {
     compileOnly 'com.github.EXPLOSIVEGAMER:woodlib-core:%version%'
}
````
- replace %version% with an compatible release version of github
- Add WoodLib to depend or softdepend in your plugin.yml
- place WoodLib-1.0.jar in your plugins folder

### Shaded Library
In your build file:

for example:

build.gradle
```groovy
plugins {
  id 'com.gradleup.shadow' version '9.0.0'
}

repositories {
    mavenCentral()
    maven {
      url "https://jitpack.io"
    }
}

dependencies {
     implementation 'com.github.EXPLOSIVEGAMER:woodlib-core:%version%'
}
```

- replace %version% with a compatible release version of github


If you shade `woodlib` into your own plugin jar via the Shadow plugin, you should relocate the package so it doesn't collide with a different WoodLib version another plugin on the same server might bundle:

```groovy
shadowJar {
    relocate 'at.woodexplosive.woodlib', 'org.example.artifact.shaded'
}
```

`org.example.artifact.shaded` is a placeholder — replace it with your own package (e.g. `com.yourname.yourplugin.libs.woodlib`).

