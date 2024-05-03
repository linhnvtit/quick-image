# Quick Image
QuickImage is a lightweight Android library designed for image usage in Jetpack Compose. With QuickImage, you can efficiently use images from remote sources. This library loads images with considerably higher speed and lower resource consumption compared to others.


# Gradle
```groovy
dependencies {
    implementation 'com.github.linhnvtit.quick-image:5607bf1625'
}
```
```groovy
repositories {
    ....
    maven { url = uri("https://jitpack.io") }
}
```

# Usage
Using QuickImage instead of normal Image when you want to load image from remote source.
```kotlin
QuickImage(
  modifier = Modifier,
  url = "https://avatars.githubusercontent.com/u/47845610?v=4"
)
```

QuickImage also have components for general purposes, which is a wrapper of default Image
```kotlin
QuickImage(
    modifier = Modifier,
    painter = painterResource(id = R.drawable.ic_random),
    contentDescription = null,
    alignment = Alignment.Center,
    contentScale = ContentScale.Fit,
    alpha = DefaultAlpha,
    colorFilter = null,
)
```

```kotlin
QuickImage(
    modifier = Modifier,
    bitmap = ImageBitmap(1,1),
    contentDescription = null,
    alignment = Alignment.Center,
    contentScale = ContentScale.Fit,
    alpha = DefaultAlpha,
    colorFilter = null,
    filterQuality = DefaultFilterQuality
)
```
