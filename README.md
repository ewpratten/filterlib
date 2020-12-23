# FilterLib ![Build library](https://github.com/Ewpratten/filterlib/workflows/Build%20library/badge.svg) [![Documentation](https://img.shields.io/badge/-documentation-blue)](https://ewpratten.retrylife.ca/filterlib)

FilterLib is a small utility library for Java that is designed to provide a simple way to work with lists of data. The primary function of FilterLib is to allow objects in a list to be assigned a "score", where various rules can be applied to mask out items based on boolean logic.

## Examples

```java
// Set up a filter
Filter<Integer> filter = new Filter<Integer>(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

// Give everything above 5 a positive score
filter.score((value) -> (value > 5) ? 1.0 : 0.0);

// Check scores
for (int value : filter.getAboveThreshold(0)) {
    assert value > 5;
}
```

## Using

Using this library with your program is quite simple. Here is a basic example for Gradle:


**Step 1.** Add the RetryLife maven server to your `build.gradle` file:

```groovy
repositories {
    maven { url 'https://maven.retrylife.ca' }
}
```

**Step 1.** Add this library as a dependency:

```groovy
dependencies {
    implementation 'ca.retrylife:filterlib:v1.+'
}
```

See [maven.retrylife.ca](https://maven.retrylife.ca/#ca.retrylife/filterlib) for up-to-date examples in all major buildsystems.
