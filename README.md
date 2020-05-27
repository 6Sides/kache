# Quick start

## Add guice module to injector

```kotlin
val injector = Guice.createInjector(SimpleCacheModule())
```

## Define a fetcher

```kotlin
class ExampleFetcher @Inject constructor(
    cache: CacheStore,
    serializer: Serializer
) : ReadThroughCachedFetcher<Int, Int?>(cache, serializer) {

    companion object {
        private val data = mapOf(1 to 2, 2 to 5)
    }

    override fun calculateResult(key: Int): CacheableResult<Int?> {
        Thread.sleep(1000) // Simulate delay from computation
        return CacheableResult.of(data[key], 3) // Cache the result for 3 seconds
    }
}
```

## Use fetchers - results are automatically cached
```kotlin
println(fetcher[2]?.result) // Computes value
println(fetcher[3]?.result) // Computes value
println(fetcher[2]?.result) // Uses cached value
```

### Console output

```
5
null
5
```
