# Daily Seventy

## Overview
**Daily Seventy** is a comprehensive Android application designed to assist Muslims in their daily religious practices. It offers prayer times, adhkar, tasbih, Qibla direction, and customizable religious reminders with a modern, user-friendly interface. The app supports offline functionality, multiple themes, and three languages: Arabic, English, and French. It also includes widgets for prayer times, Quran, and adhkar, and uses WebView for feedback forms and privacy policy.

## Features
- **Prayer Times**:
  - Displays prayer times for the entire month, supporting both online and offline modes.
  - Highlights the next prayer and shows it on the home screen.
  - Customizable Azan audio for regular prayers and a separate option for Fajr prayer.
  - Reminders for prayer times, Iqama, and pre/post-prayer notifications.
- **Adhkar**:
  - Beautifully designed daily adhkar with dynamic content on the home screen.
  - Categorized adhkar for morning, evening, post-prayer, and custom scenarios.
- **Tasbih**:
  - Customizable tasbih for post-prayer, general use, or linked to specific adhkar.
  - Interactive and visually appealing tasbih interface.
- **Qibla Finder**:
  - Easily locate the Qibla direction using OpenStreetMap (OSM) and device compass.
- **Religious Reminders**:
  - Notifications for Monday and Thursday fasting, Sunnah fasting, and Friday prayers.
  - Reminders for praying upon the Prophet (peace be upon him).
  - Customizable reminders for the three thirds of the night and religious occasions.
- **Fajr Alarm**:
  - Advanced alarm system for Fajr with customizable triggers:
    - Motion sensor (dismisses after a set number of steps).
    - Light sensor (dismisses based on ambient light detection).
  - Uses MediaPlayer for Azan and alarm sounds.
- **Location Services**:
  - Easy location detection using OpenStreetMap (OSM) for accurate prayer times and Qibla.
- **Hijri Calendar**:
  - Displays the Hijri date on the home screen using Umm al-Qura Calendar and Time4J.
- **Multilingual Support**:
  - Available in Arabic, English, and French with seamless language switching.
- **Customization**:
  - Multiple color themes with various shades for personalized appearance.
  - Customizable Azan voices for prayers and Fajr-specific Azan.
- **Widgets**:
  - Prayer Times Widget: Displays daily prayer times.
  - Quran Widget: Quick access to Quranic content.
  - Adhkar Widget: Displays daily or custom adhkar.
- **WebView Integration**:
  - Feedback form for suggestions and improvements.
  - Privacy policy displayed via WebView.
- **Offline Support**:
  - Stores prayer times, adhkar, and tasbih data for offline access.
- **Permissions**:
  - Requests permissions for location, notifications, motion/light sensors, and boot completion.

## Home Screen Layout
- Displays prayer times and the next prayer.
- Shows the Hijri date.
- Dynamic adhkar section.
- Daily Islamic Guide with categories for adhkar, prayers, tasbih, Qibla, and location settings.
- Full prayer times schedule.

## Tech Stack
### Programming Language
- **Kotlin**: Core language for app development.

### Frameworks & Libraries
- **Android Jetpack**:
  - **Jetpack Compose**: For modern, declarative UI with Sheets for modal dialogs.
  - **WorkManager**: For scheduling notifications and Azan reminders.
  - **Room**: For local storage of prayer times, adhkar, and tasbih data.
  - **Navigation**: For in-app navigation.
  - **Glance**: For creating app widgets (Prayer Times, Quran, Adhkar).
- **OpenStreetMap (OSM)**: For location services and Qibla direction.
- **Retrofit**: For fetching prayer times or other data from APIs.
- **Gson & Kotlinx Serialization**: For JSON parsing and serialization.
- **Dagger Hilt**: For dependency injection.
- **Coroutines**: For asynchronous programming and background tasks.
- **Time4J & Umm al-Qura Calendar**: For accurate Hijri date calculations.
- **MediaPlayer**: For playing Azan and Fajr alarm sounds.
- **Android Sensor API**: For motion and light sensors in Fajr alarm.
- **Coil**: For image loading in Compose.
- **Extended Icons**: For enhanced iconography in the UI.
- **Desugaring**: For Java 8+ API compatibility on older Android versions.
- **SharedPreferences**: For storing user settings (e.g., theme, language, Azan preferences).

### Modularization
- The app follows a modular architecture with:
  - **Use Cases**: Encapsulate business logic (e.g., `azan`, `dailyazkar`).
  - **Data Sources**: Handle data retrieval (e.g., `repository`, `sensordomain`).
  - **Repositories**: Manage data flow between sources and use cases.
  - **Modules**: Separate features (e.g., `prayertimes`, `qibla`, `tasbeeh`) for scalability.

### Tools
- **Android Studio**: IDE for development and testing.
- **Gradle**: Build automation with Kotlin DSL.
- **Git**: Version control system.

## Prerequisites
To build and run the app, ensure you have:
- Android Studio (version 2023.3.1 or later)
- JDK 17 or higher
- Android SDK (API level 33 or higher)
- Gradle 8.0 or higher
- Internet connection for fetching prayer times (optional for offline mode)
- Device with location services and sensors for full functionality

## Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/sherifshabans/DailySeventy.git
   ```
2. **Open in Android Studio**:
   - Open Android Studio and select `Open an existing project`.
   - Navigate to the cloned repository folder and open it.
3. **Sync Project**:
   - Click `Sync Project with Gradle Files` to download dependencies.
4. **Configure API Keys**:
   - Add your OpenStreetMap API key or prayer times API key (e.g., Aladhan API) in `local.properties`.
   - Configure WebView URLs for feedback form and privacy policy.
5. **Run the App**:
   - Connect an Android device or use an emulator.
   - Click `Run` in Android Studio to build and deploy the app.

## Project Structure
```
core/                          # Core module containing core features
├── prayertimes/               # Prayer times functionality
├── quran/                     # Quran-related functionality
└── tasbih/                    # Tasbih-related functionality
app/
├── manifests/                 # Manifest files
├── kotlin-java/
│   ├── com.elsharif.dailyseventy/
│   │   ├── di/               # Dependency Injection configuration
│   │   ├── domain/           # Business logic layer
│   │   │   ├── azan/
│   │   │   ├── dailyazkar/
│   │   │   ├── data/
│   │   │   ├── friday/
│   │   │   ├── islamicReminder/
│   │   │   ├── repository/
│   │   │   ├── sensordomain/
│   │   │   ├── thirdnight/
│   │   │   └── zekr/
│   │   ├── data/             # Data layer
│   │   ├── AppPreferences.kt # App settings
│   │   ├── presentation/     # Presentation layer
│   │   │   ├── azkarcategories/
│   │   │   ├── colorselction/
│   │   │   ├── comingsoon/
│   │   │   ├── components/
│   │   │   ├── friday/
│   │   │   ├── hijriCalendar/
│   │   │   ├── home/
│   │   │   ├── islamicReminders/
│   │   │   ├── language/
│   │   │   ├── permissins/
│   │   │   ├── prayertimes/
│   │   │   ├── privacypolicy/
│   │   │   ├── problems/
│   │   │   ├── qibla/
│   │   │   ├── sensor/
│   │   │   ├── settings/
│   │   │   ├── tasbeeh/
│   │   │   ├── thirdofthenight/
│   │   │   ├── widgets/
│   │   │   ├── zekr/
│   │   │   ├── ui/
│   │   │   └── util/
│   │   ├── DilaYApp.kt       # App entry point
│   │   └── MainActivity      # Main activity
│   ├── com (androidTest)/    # Android test cases
│   └── com (test)/           # Unit test cases
```

## Dependencies
Key dependencies in the `build.gradle.kts` file:
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.threeten:threetenbp:1.6.8") // Time4J dependency
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")
}
```

## Permissions
The app requests the following permissions:
- `ACCESS_FINE_LOCATION`: For prayer times and Qibla direction.
- `INTERNET`: For API calls and WebView content.
- `RECEIVE_BOOT_COMPLETED`: For WorkManager to reschedule notifications.
- `USE_FULL_SCREEN_INTENT`: For Fajr alarm notifications.
- `ACTIVITY_RECOGNITION`: For motion sensor in Fajr alarm.
- `SCHEDULE_EXACT_ALARM`: For precise prayer time notifications.

## API Integration
- **OpenStreetMap (OSM)**: For location detection and Qibla direction.
- **Prayer Times API** (optional): Configure your API endpoint (e.g., Aladhan API) in the app.
- **WebView URLs**: Feedback form and privacy policy loaded via WebView.

## Local Database
The app uses Room to store:
- Prayer times for offline access.
- Adhkar and tasbih data.
- User preferences (e.g., theme, language, Azan settings).

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Make your changes and commit (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
