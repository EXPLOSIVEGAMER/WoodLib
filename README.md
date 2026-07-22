[![](https://jitpack.io/v/EXPLOSIVEGAMER/WoodLib.svg)](https://jitpack.io/#EXPLOSIVEGAMER/WoodLib)

in your build.gradle

````groovy
   repositories {
        mavenCentral()
        maven {
          url "https://jitpack.io"
        }
   }
   
   dependencies {
	
         implementation 'com.github.EXPLOSIVEGAMER:woodlib-core:1.21.11-1.0'
			
   }
````

If you shade `woodlib-core` into your own plugin jar via the Shadow plugin, you should relocate the package so it doesn't collide with a different WoodLib version another plugin on the same server might bundle:

```groovy
shadowJar {
    relocate 'at.woodexplosive.woodlib', 'org.example.artifact.shaded'
}
```

`org.example.artifact.shaded` is a placeholder — replace it with your own package (e.g. `com.yourname.yourplugin.libs.woodlib`).
