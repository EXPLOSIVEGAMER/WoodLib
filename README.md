[![](https://jitpack.io/v/EXPLOSIVEGAMER/WoodLib.svg)](https://jitpack.io/#EXPLOSIVEGAMER/WoodLib)

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

And many more to come


</details>



You can use this Library the following way:

### Standalone Plugin
In your build file:

z.B.
build.gradle
```
repositories {
    mavenCentral()
    maven {
      url "https://jitpack.io"
    }
}

dependencies {
     compileOnly 'com.github.EXPLOSIVEGAMER:woodlib-core:%version%'
}
```
- replace %version% with an compatible release version of github
- Add WoodLib to depend or softdepend in your plugin.yml
- place WoodLib-1.0.jar in your plugins folder

### Shaded Library
In youre build file:

z.B.
build.gradle
```
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

- replace %version% with an compatible release version of github


- Make sure to relocate the lib jar if more than 1 plugin shades this lib!
```

```
