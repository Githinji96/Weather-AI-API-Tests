# Farm Image Assets

Place drone, aerial, or satellite farm images here for the forestry upload tests.

## Expected files (configured in config.properties)

| Property                  | Default filename       | Format       |
|---------------------------|------------------------|--------------|
| `forestry.image.primary`  | `sample-farm.jpg`      | JPEG         |
| `forestry.image.fallback` | `sample-farm.png`      | PNG          |

## Constraints (WeatherAI API)
- Accepted formats: JPEG, PNG, WEBP
- Max file size: 20 MB

## Changing the image path

Update `src/main/resources/config.properties`:
```properties
forestry.image.primary=images/my-custom-farm.jpg
forestry.image.fallback=images/my-custom-farm.png
```

No code changes needed — `ApiConfig` reads these values at runtime.
