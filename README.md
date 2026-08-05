# Anime List App 🍿

A sleek, modern Android application for discovering and tracking your favorite anime, built with a **Netflix-inspired** Dark Mode UI. Powered by the [Jikan API v4](https://jikan.moe/) (unofficial MyAnimeList API).

## 🌟 Features

* **Netflix-Style UI**: Immersive dark theme (`#141414`) with Netflix red accents, smooth gradients, and grid layouts.
* **Featured Carousel**: Auto-scrolling banner showcasing the top 5 trending anime on the home screen.
* **Discover & Filter**: 
  * View top-rated anime globally.
  * Filter by genre (Action, Romance, Comedy, Adventure).
  * Sort by A-Z, Top Score, or Most Episodes.
* **Smart Search**: Real-time debounce search directly from the top app bar to find any anime instantly.
* **Detailed Info**: Deep dive into an anime's details including:
  * High-resolution posters and synopsis
  * Total episodes, global rank, and community score
  * **Community Reviews**: Read what others think.
  * **Recommendations**: Discover similar anime you might like.
* **My List**: 
  * Save anime to your personal list using a quick Context Menu or from the detail page.
  * Rate anime from 1 to 5 stars.
  * Compare your personal rating directly alongside the global API score.
* **Robust & Stable**: Built with Kotlin Coroutines, automatic rate-limit handling (HTTP 429), and lifecycle-aware architecture to prevent crashes.

## 🛠 Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **Architecture**: Single Activity with Multiple Fragments (Navigation Component)
* **Networking**: [Retrofit 2](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson)
* **Image Loading**: [Glide](https://github.com/bumptech/glide)
* **Concurrency**: Kotlin Coroutines & LifecycleScope
* **Storage**: SharedPreferences (for My List & User Ratings)
* **API**: Jikan API v4

## 📱 Screenshots

*(Add screenshots of your Home Screen, Detail Screen, and My List here!)*

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/anime-list-app.git
   ```
2. Open the project in **Android Studio**.
3. Sync the Gradle files.
4. Run the app on an emulator or physical device.

## ⚠️ Note on API Rate Limiting
This app uses the public, free Jikan API which enforces strict rate limits (3 requests/second, 60 requests/minute). The app automatically handles `HTTP 429 Too Many Requests` by displaying a "Server busy, retrying..." message and safely delaying further background requests.

## 👤 Author
Developed by **Marvel Kevin Nathanael**
