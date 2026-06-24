> 📚 **Recommended Reading Path**
>
> [Core Features](./CoreFeatures-EN.md) → [Integration Preparation](./Integration-EN.md) → **Quick Start** → [API Reference](./ApiReference-EN.md)

> 🤖 **AI-Friendly Tip**
>
> **This document is well-structured with clear steps, suitable for AI to read, understand, and execute. It can also be used as Skills, allowing AI to assist with the integration and use of AliPlayerKit.**
>
> **We will release dedicated Skills in the future to provide more complete integration support.**

---

# **AliPlayerKit Quick Start**

In just a few steps, you can easily implement video playback functionality!

With an extremely simple API design, AliPlayerKit helps you integrate video playback in a low-code way, without directly invoking the underlying player APIs or implementing complex Player UI yourself.

> **Note**: Before getting started, please make sure you have completed the environment configuration and dependency setup as described in [Integration Preparation](./Integration-EN.md).

---

## **1. Integration Process Overview**

In just a few steps, you can easily implement video playback! **AliPlayerKit provides an extremely simple API design** that helps you integrate video playback in a low-code way.

The diagram below shows the **component-layer integration** process:

![QuickStart](https://alivc-demo-cms.alicdn.com/versionProduct/installPackage/AliPlayerKit/Android/QuickStart-EN.png)

---

## **2. Choose an Integration Solution**

AliPlayerKit adopts a **layered architecture design**. In addition to component-layer integration, it also offers **scene-layer integration**. You can choose flexibly according to your business needs:

| Integration Solution | Module | Description | Use Cases |
|---------|------|---------|---------|
| **Solution 1: Component-Layer Integration** | `playerkit` | Use the `playerkit` core module, which provides configurable Player UI components | Custom Player UI is required, or flexible control of playback behavior |
| **Solution 2: Scene-Layer Integration** | `playerkit-scenes` | Built on top of the component layer, provides complete business scene solutions | Quickly implement standard playback scenarios such as long video, short video, and live streaming |

> **Note**: The scene layer depends on the component layer. If you choose scene-layer integration, you must first complete component-layer integration.

---

## **3. Global Initialization**

Call `AliPlayerKit.init()` in `Application.onCreate()` for global initialization. It only needs to be called once during the entire application lifecycle, used to initialize the player core and global configuration.

* **Java**:

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize AliPlayerKit global settings
        AliPlayerKit.init(this);
    }
}
```

* **Kotlin**:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AliPlayerKit global settings
        AliPlayerKit.init(this)
    }
}
```

> **Note**: `AliPlayerKit` also provides other global configuration interfaces such as `setPlayerViewType()`, `setDisableScreenshot()`, etc. See [API Reference](./ApiReference-EN.md) for details.

---

## **4. Solution 1: Component-Layer Integration**

The component layer (the `playerkit` module) provides out-of-the-box, configurable Player UI components. On every page that needs video playback, follow the steps below to implement playback.

> **Prerequisite**: Before integrating the component layer, complete the component-layer integration steps in [Integration Preparation](./Integration-EN.md), and complete [Global Initialization](#3-global-initialization).

### **Step 1: Add the Player View**

Add `AliPlayerView` in the layout XML:

```xml
<!-- Player component view -->
<com.aliyun.playerkit.AliPlayerView
    android:id="@+id/player_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

`AliPlayerView` can be placed directly into your layout, and its width and height can be set as needed. If you need an outer container (e.g., for fullscreen switching scenarios), you can add a FrameLayout or other container to wrap it yourself.

### **Step 2: Bind the Controller and Play**

> **Important Configuration**: If your application supports fullscreen playback switching, you need to add the `android:configChanges` attribute in the `AndroidManifest.xml` of the corresponding Activity, to avoid losing the playback state due to Activity recreation when the screen rotates:
>
> ```xml
> <application>
>     <activity
>         android:name=".VideoPlayerActivity"
>         android:configChanges="orientation|screenSize"
>         ...>
>     </activity>
> </application>
> ```
>
> **Note**: Without this configuration, fullscreen switching may cause abnormal player state, screen flickering, or playback interruption.

In the Activity/Fragment, create the controller, configure data, and bind the View:

> **Important**: `configure()` must be called before `attach()`; otherwise, the playback configuration cannot be obtained when the view is bound.

* **Java**:

```java
public class VideoPlayerActivity extends AppCompatActivity {

    private AliPlayerView playerView;
    private AliPlayerController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // 1. Create the playback controller
        controller = new AliPlayerController(this);

        // 2. Configure playback data (VidAuth approach is recommended)
        controller.configure(new AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(
                        "Your Video ID",    // Video ID
                        "Your PlayAuth"     // Playback credential
                ))
                .sceneType(SceneType.VOD)
                .coverUrl("https://example.com/cover.jpg")  // Replace with the actual cover image URL
                .videoTitle("Sample Video")
                .autoPlay(true)
                .build());

        // 3. Bind the view
        playerView = findViewById(R.id.player_view);
        playerView.attach(controller);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 4. Destroy the player and release resources
        controller.destroy();
    }
}
```

* **Kotlin**:

```kotlin
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var playerView: AliPlayerView
    private lateinit var controller: AliPlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        // 1. Create the playback controller
        controller = AliPlayerController(this)

        // 2. Configure playback data (VidAuth approach is recommended)
        controller.configure(AliPlayerModel.Builder()
                .videoSource(VideoSourceFactory.createVidAuthSource(
                        "Your Video ID",    // Video ID
                        "Your PlayAuth"     // Playback credential
                ))
                .sceneType(SceneType.VOD)
                .coverUrl("https://example.com/cover.jpg")  // Replace with the actual cover image URL
                .videoTitle("Sample Video")
                .autoPlay(true)
                .build())

        // 3. Bind the view
        playerView = findViewById(R.id.player_view)
        playerView.attach(controller)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 4. Destroy the player and release resources
        controller.destroy()
    }
}
```

> **Tip**: If you are temporarily unable to obtain a **Video ID** and **PlayAuth**, you can use the example data provided in `SceneConstants.java` for testing, in order to quickly run through the example.

### **Step 3 (Optional): Handle the Back Key**

If your application supports fullscreen playback, you need to handle the back key event in the Activity:

* **Java**:

```java
@Override
public void onBackPressed() {
    // If currently in fullscreen, exit fullscreen
    if (playerView.onBackPressed()) {
        return;  // Already handled, do not perform default behavior
    }
    super.onBackPressed();  // Not handled, perform default behavior
}
```

* **Kotlin**:

```kotlin
@Deprecated("Deprecated in Java")
override fun onBackPressed() {
    // If currently in fullscreen, exit fullscreen
    if (playerView.onBackPressed()) {
        return  // Already handled, do not perform default behavior
    }
    super.onBackPressed()  // Not handled, perform default behavior
}
```

> **Note**: For the complete API description of each component, please refer to [API Reference](./ApiReference-EN.md).

**Related Reference**: For the core interface invocation flow, please refer to [Core Features - Call Sequence](./CoreFeatures-EN.md#25-call-sequence).

---

## **5. Solution 2: Scene-Layer Integration**

The scene layer provides complete playback solutions for specific business scenarios such as long video, short video, and live streaming. The scene layer is built on top of the component layer and is ready to use after integration without additional development.

> **Prerequisite**: Before integrating the scene layer, complete the scene-layer integration steps in [Integration Preparation](./Integration-EN.md), and complete [Global Initialization](#3-global-initialization).

### **Step 1: Choose a Scene Module**

Scene modules are located under the `playerkit-scenes` directory and currently include:

| Scene Module | Description |
|---------|------|
| `scene-common` | Scene common module (required, the base dependency for other scene modules) |
| `scene-longvideo` | **Medium/Long Video Scene**: a complete solution for medium/long video playback |
| `scene-shortvideo` | **Short Video Scene**: a complete solution for short video swipe playback |
| `scene-live` | **Live Streaming Scene**: a complete solution for live streaming playback |
| `scene-playlist` | **List Playback Scene**: a complete solution for video list playback |

### **Step 2: Launch the Scene Page**

After scene module integration is complete, you can launch the scene page in either of the following two ways:

#### **Approach 1: Intent Navigation**

* **Java**:

```java
// Navigate to the medium/long video scene
Intent intent = new Intent(this, LongVideoActivity.class);
startActivity(intent);
```

* **Kotlin**:

```kotlin
// Navigate to the medium/long video scene
val intent = Intent(this, LongVideoActivity::class.java)
startActivity(intent)
```

#### **Approach 2: Schema Navigation**

Scene modules support launching the corresponding playback scene via Schema. The Schema for each scene is defined in the `AndroidManifest.xml` of the corresponding module. You can check the `<data>` tag of `<intent-filter>` to obtain the specific Schema URI.

For example, the configuration in `AndroidManifest.xml`:

```xml
<data
    android:host="scenes"
    android:path="/longvideo"
    android:scheme="playerkit" />
```

The corresponding Schema URI can be assembled as:

```
playerkit://scenes/longvideo
```

* **Java**:

```java
// Navigate to the target scene (replace with the actual Schema)
Intent intent = new Intent(Intent.ACTION_VIEW);
intent.setData(Uri.parse("Corresponding Schema URI"));
startActivity(intent);
```

* **Kotlin**:

```kotlin
// Navigate to the target scene (replace with the actual Schema)
val intent = Intent(Intent.ACTION_VIEW).apply {
    data = Uri.parse("Corresponding Schema URI")
}
startActivity(intent)
```

---

## **6. Examples and Extensions**

Through the steps above, you have completed the basic integration of AliPlayerKit and successfully implemented video playback.

For more examples, please refer to the following directories:

- `playerkit-examples`: provides examples of common API usage
- `playerkit-scenes`: provides solutions for typical playback scenarios

If you encounter issues during integration or use, please contact official technical support for help.

---

For detailed descriptions of each component and interface, please refer to **[API Reference](./ApiReference-EN.md)**.
