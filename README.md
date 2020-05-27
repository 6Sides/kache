# Quick start

## Add guice module to injector

```kotlin
val injector = Guice.createInjector(SimpleCacheModule())
```

## Define a fetcher

```kotlin
// Return 10 times the input
val fetcher = CacheBuilder.build(Strategy.READ_THROUGH) { key: Int ->
    CacheableResult.of(key*10, 5) // Result expires after 5 seconds
}
```

## Query the fetcher - Results are cached automatically!
```kotlin
println(fetcher[1]?.result) // Computes value
println(fetcher[2]?.result) // Computes value
println(fetcher[1]?.result) // Uses cached value
```

### Console output

```
10
20
10
```
