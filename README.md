🎵 Offline Music Player

An elegant offline music player app built with Kotlin and Jetpack Compose, featuring smooth playback controls, modern UI, and background playback powered by Media3 ExoPlayer.



🚀 Features

* 🎶 **Offline Playback** – Automatically lists all songs available on your device.
* 🏠 **Home Screen** – Displays a full list of available tracks with song details.
* ▶️ **Mini Player (Bottom Bar)** – Persistent player control at the bottom of the screen for quick access.
* 🔊 **Media Notifications** – Full media-style notification with playback controls.
* 📱 **MediaSession Integration** – Seamless interaction with system media controls (lock screen, Bluetooth devices, etc).
* 🎧 **Media3 ExoPlayer** – Efficient, feature-rich playback with modern APIs.
* 🧭 **MVVM Architecture** – Clean and maintainable code using ViewModel and LiveData/StateFlow.
* 💅 **Modern UI** – Built with Jetpack Compose for a responsive, beautiful design.

---

🛠️ Tech Stack

| Layer                             | Technology                                         |
| --------------------------------- | -------------------------------------------------- |
| Language                          | **Kotlin**                                         |
| UI                                | **Jetpack Compose**                                |
| Media Playback                    | **AndroidX Media3 ExoPlayer**                      |
| Media Control                     | **MediaSession**, **MediaStyle Notification**      |
| Architecture                      | **MVVM**, **ViewModel**, **Coroutines**            |
| Dependency Injection *(optional)* | **Hilt / Koin**                                    |
| Permissions                       | **Runtime Storage Access (READ_EXTERNAL_STORAGE)** |

---
📸 Screenshots

| Home Screen   | Player Bar    | Notification  |
| ------------- | ------------- | ------------- |
| *(add image)* | *(add image)* | *(add image)* |


⚙️ Setup & Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/yourusername/offline-music-player.git
   ```
2. Open the project in **Android Studio** (latest stable version recommended).
3. Sync Gradle and run the app on a physical device or emulator.


🧩 Future Enhancements

* 🎨 Album art and metadata display
* 🕹️ Playlist and favorites support
* 🌙 Dark mode toggle
* 🔁 Shuffle and repeat modes
* 🔍 Search functionality
