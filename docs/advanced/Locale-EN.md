# **Locale**

**Locale** is the internationalization module of AliPlayerKit. Through a unified language manager it provides automatic system-language tracking, full language replacement, and runtime language switching, allowing the player UI text to flexibly adapt to multilingual scenarios.

---

## **1. Default Behavior (Zero Configuration)**

AliPlayerKit works **out of the box** — no code is required to support multiple languages:

- Built-in Chinese and English (via `values/strings.xml` and `values-en/strings.xml`)
- Player UI text **automatically follows the system language**, with no initialization or API call needed
- When the user switches the system language, the player text switches accordingly
- If the system language is not among the built-in languages, it falls back to the default language (Chinese)

| Built-in Language | Resource Directory |
|-------------------|--------------------|
| Chinese (default) | `res/values/strings.xml` |
| English | `res/values-en/strings.xml` |

> For most apps that only need Chinese and English, **no API call is required — simply integrate AliPlayerKit**.

---

## **2. Custom Scenarios**

Use the following APIs only when the default "follow system language" behavior is not sufficient.

### **2.1 Force a Specific Language (setLanguage)**

When you need to expose a language-switch entry inside the app and stop following the system language, use `setLanguage()` to force a specific language:

```java
// Force switch to English (no longer follows the system language)
PlayerLocale.setLanguage("en");

// Force switch back to Chinese
PlayerLocale.setLanguage("zh");
```

> 💡 After calling `setLanguage()`, the current Activity automatically recreates itself so the new language takes effect.

### **2.2 Add a New Language (values-xx/strings.xml)**

When you need to add a language not built into AliPlayerKit (such as Japanese or Korean), use the standard Android resource mechanism. After adding it, automatic system-language tracking still works.

**Step by Step**:

1. **Create the corresponding resource directory in your app module**

   ```
   your-app/src/main/res/
   ├── values/strings.xml          ← Chinese (default)
   ├── values-en/strings.xml       ← English
   ├── values-ja/strings.xml       ← New: Japanese
   └── values-ko/strings.xml       ← New: Korean
   ```

   > Reference example: `playerkit-examples/example-locale/src/main/res/values-ja/strings.xml`

2. **Copy the default `values/strings.xml` and translate every entry**

   ```xml
   <!-- res/values-ko/strings.xml -->
   <?xml version="1.0" encoding="utf-8"?>
   <resources>
       <string name="setting_item_speed">재생 속도</string>
       <string name="setting_item_quality">화질</string>
       <string name="setting_item_loop">반복 재생</string>
       <string name="setting_item_mute">음소거</string>
       <string name="player_brightness">밝기</string>
       <string name="player_volume">볼륨</string>
       <!-- ... translate all entries ... -->
   </resources>
   ```

3. **Make the new language take effect**

   - **Auto-follow the system**: once the user switches the system language to Korean, the player text switches automatically — no API call required
   - **Force a language**: if you need to switch manually inside the app, call `PlayerLocale.setLanguage("ko")`

> 💡 Recommended: translate every entry in `values/strings.xml` into the new language file to avoid mixed display caused by partial fallback to the default language.

### **2.3 Listening for Language Changes**

Use a listener to respond to language switches — useful for refreshing custom UI or recording logs:

```java
// Create a listener
PlayerLocale.OnLanguageChangedListener listener = (oldLanguage, newLanguage) -> {
    Log.i(TAG, "Language changed: " + oldLanguage + " → " + newLanguage);
    // Refresh custom UI
    refreshMyCustomUI();
};

// Register the listener
PlayerLocale.addOnLanguageChangedListener(listener);

// Remove it when no longer needed (e.g. Activity onDestroy)
PlayerLocale.removeOnLanguageChangedListener(listener);
```

---

## **3. API Reference**

### **3.1 PlayerLocale Methods**

| Method | Description |
|--------|-------------|
| `get(resId)` | Get text (read from strings.xml resources) |
| `get(resId, formatArgs)` | Get formatted text (supports `%s`, `%d` placeholders) |
| `setLanguage(languageCode)` | Set the app language and trigger Activity recreation |
| `getLanguage()` | Get the current language code |
| `addOnLanguageChangedListener(listener)` | Add a language-change listener |
| `removeOnLanguageChangedListener(listener)` | Remove a language-change listener |

---

## **4. Design Notes**

### **4.1 Text Lookup Mechanism**

`PlayerLocale.get()` retrieves the text for the current language via the standard Android resource mechanism:

- If a `values-xx/strings.xml` matching the current language exists, the translation in that file is returned
- If no resource directory exists for the current language, it falls back to the default (Chinese `values/strings.xml`)

### **4.2 Thread Safety**

| Component | Thread-safety mechanism |
|-----------|--------------------------|
| Listener list | `CopyOnWriteArrayList`, optimized for read-heavy, write-light workloads |

### **4.3 Language Switching Mechanism**

`setLanguage()` is implemented internally with `AppCompatDelegate.setApplicationLocales()`:

- Android 13+: native per-app language support, no special handling needed
- Android 12 and below: a Context with the correct locale is created via `createConfigurationContext`
- The current Activity is recreated after the call, so the UI refreshes automatically

### **4.4 Version Compatibility**

| Android Version | Behavior |
|-----------------|----------|
| Android 13+ (API 33+) | Uses native per-app language support; the Application Context locale stays in sync automatically |
| Android 12 and below | Compatible implementation via AppCompatDelegate + createConfigurationContext |

---

## **5. Best Practices**

### **5.1 Choose the Right Approach**

| Scenario | Recommended Approach | Description |
|----------|----------------------|-------------|
| Chinese & English only | No action needed | Built-in Chinese and English; auto-follows the system language |
| Add a full new language | `values-xx/strings.xml` | Translate everything; uses the native Android mechanism and auto-follows the system language |
| Force a specific language | `PlayerLocale.setLanguage()` | Stops following the system; the app controls the language itself |

### **5.2 Notes**

| Item | Description |
|------|-------------|
| Translate fully | When adding a new language, translate every entry to avoid mixed-language display |
| Listener lifecycle | Remove listeners when the Activity/Fragment is destroyed to avoid memory leaks |
| Language code format | Use the lowercase two-letter ISO 639-1 format (e.g. `"zh"`, `"en"`, `"ja"`) |

---

## **6. Example Reference**

The project ships with a complete example at `playerkit-examples/example-locale`.

### **6.1 Example Features**

| Feature | Description |
|---------|-------------|
| Language switching | Demonstrates real-time switching between Chinese, English, and Japanese (Japanese added via `values-ja/strings.xml`) |
| Change listener | Demonstrates registering and using the language-change listener |

### **6.2 Run the Example**

Pick the "Locale" example in the demo app to see it in action.
