# RFID Try-On Display

API-driven Android dashboard for RFID try-on items.

## What this project now does

- Polls `http://103.189.89.76:8080/api/tryons/recent-fitting-room-entries-in-5min` every 10 seconds
- Falls back to `http://103.189.89.76:8080/api/tryons/todays-try-on-items` when the recent feed is empty
- Shows a large featured product area plus a bottom horizontal strip of items
- Uses Glide for image loading with memory+disk caching
- Delays content render while prefetching all current image URLs (up to 5 seconds)
- Loads a low-quality thumbnail first, then full image
- Uses placeholder and error states for slow/broken URLs
- Shows all non-image fields as text on the right side of the featured card
- Maps API fields into the UI model:
  - `epc -> tagName`
  - `sku -> sku`
  - `product -> product/descp`
  - `tryOnLocation -> location`
  - `brand/category/department/gender/color -> detail fields`
  - `itemSize -> size`
  - `duration -> duration`
  - `status -> status`
  - `tryOnsStarttime -> startTimeText/timestamp`
  - `tryOnsEndtime -> endTimeText`
- Uses placeholder artwork only when image URL is missing

## Verified assumptions

On 2026-05-25, the endpoint responded successfully over plain HTTP with no authentication header required.

Observed payload shape:

```json
{
  "success": true,
  "message": "Today's try-on items retrieved",
  "status": 200,
  "data": [
    {
      "epc": "E280...",
      "sku": "190000021031",
      "tryOnsStarttime": "2026-05-25T18:15:01.498",
      "product": "Pantaloon",
      "brand": "MEN APPAREL",
      "category": "MEN APPAREL",
      "gender": "MEN",
      "color": "Teal Blue",
      "itemSize": "M",
      "status": "IN"
    }
  ]
}
```

## Configuration

The app currently uses build config constants in `app/build.gradle.kts`:

- `TRYON_BASE_URL`
- `TRYON_AUTH_TOKEN`
- `TRYON_POLL_INTERVAL_MS`

If auth is added later, set `TRYON_AUTH_TOKEN` and the app will send it as an `Authorization` header. If the value does not already contain a space, it is sent as `Bearer <token>`.

## Image strategy

Current implementation: use `image` URL directly with Glide optimization:

- `DiskCacheStrategy.AUTOMATIC`
- memory cache enabled (`skipMemoryCache(false)`)
- `thumbnail(0.20f)` for faster first paint
- `dontAnimate()` to reduce flicker on fast updates
- decode hint `PREFER_RGB_565` to lower memory pressure

The app is already prepared to read these optional fields if they are added later:

- `imageUrl`
- `image`
- `image_url`
- `productImage`
- `mediaUrl`

## RecyclerView optimization sample

The project includes RecyclerView-ready Glide examples (MVVM compatible):

- Adapter: `app/src/main/java/com/rfid/rfidreader/ui/recyclerview/TryOnRecyclerAdapter.kt`
- Preloading helper: `app/src/main/java/com/rfid/rfidreader/ui/recyclerview/TryOnPreloadHelper.kt`
- Item model: `app/src/main/java/com/rfid/rfidreader/ui/recyclerview/RvTryOnItem.kt`
- Item layout: `app/src/main/res/layout/item_tryon_card.xml`

Attach preloader usage:

```kotlin
val adapter = TryOnRecyclerAdapter(onItemClicked = { /* ... */ })
recyclerView.adapter = adapter
attachTryOnPreloader(recyclerView, adapter, Glide.with(this))
```

This setup gives smooth scrolling, async image fetch, auto-cancel on recycle, and less flicker.

## Run

```powershell
Set-Location "D:\RFIDREADER"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

## Notes

This workspace did not contain the older Java-based Bluetooth/Excel project files referenced in the migration brief, so the UI was rebuilt directly in Compose while preserving the requested interaction pattern: a featured display area plus a bottom product strip fed entirely by the API.
