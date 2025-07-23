# Just Put It In - Disc Golf Putting Tracker

Just Put It In is a modern Android app for tracking your disc golf putting practice sessions. Set your distance, number of putts, and putting style, then record your results and view your progress over time with beautiful statistics and charts.

## Features

- 📏 **Session Setup:** Choose distance (1-30m), number of putts (1-20), and putting style from a comprehensive list.
- 🎯 **Quick Results Entry:** Enter your made putts with an intuitive, scrollable selector. Save results with a single tap.
- 📊 **History & Stats:** View your past sessions by distance and time range (week, month, year, all). See hit rates, session details, and a bar chart of your performance.
- 🏆 **Style Tracking:** Track your putting style for each session (push, spin, turbo, straddle, etc.).
- ✨ **Modern UI:** Beautiful, animated Jetpack Compose interface with smooth transitions and clear navigation.
- 💾 **Local Storage:** All data is stored locally on your device using Room database.

## Screenshots

*Add screenshots here if available*

## Getting Started

### Prerequisites
- Android Studio (Giraffe or newer recommended)
- Android device or emulator (API 29+)

### Setup
1. **Clone the repository:**
   ```bash
   git clone <your-repo-url>
   cd JPII
   ```
2. **Open in Android Studio:**
   - Open the `JPII` folder in Android Studio.
3. **Build the project:**
   - Let Gradle sync and download dependencies.
   - Build the project (`Build > Make Project`).
4. **Run the app:**
   - Select a device or emulator and click Run.

## Usage
1. **Start a New Session:**
   - Tap "New session" on the home screen.
   - Select your distance, number of putts, and putting style.
   - Tap "Save" after entering your results.
2. **View History:**
   - Tap "History" to see your stats by distance and time range.
   - Review your hit rates, session details, and progress chart.

🧠 Putting Rating: How It Works
This app calculates a single score — your Putting Rating (0–100) — that reflects how good you are at putting, based on your most recent sessions from 3 to 15 meters.

⚙️ How It Works
1. 📦 Recent Sessions by Distance
   For every distance between 1 and 15 meters, the app:

Looks at your most recent putting sessions.

Includes sessions until it has at least 10 putts, but no more than 30.

Skips the distance entirely if you haven’t attempted at least 10 putts.

2. 📊 Calculates Your Accuracy
   For each distance:

Counts how many total putts you took.

Counts how many you made.

Calculates your make percentage (e.g. 18 made / 25 total = 72%).

3. ⚖️ Weights Each Distance
   Not all distances are treated equally:

Longer putts count more
→ Making a 12m putt is harder than a 2m putt.

Distances with more data count more
→ More attempts = more reliable.

4. 🧮 Calculates Your Final Rating
   The app combines all of your distance stats using the weights above to calculate a single value from 0.0 to 1.0, which is then scaled to a 0–100 score.

📈 Example
Distance	Made / Attempts	Accuracy
3m	10 / 10	100% ✅
5m	18 / 20	90% ✅
10m	15 / 30	50% ⚠️
12m	12 / 30	40% ❌

Even though you're perfect at short distances, the longer putts lower your average — because they're harder and weighted more.

👉 Your Putting Rating might be ~75, not 93.

🧠 Why This Is Smart
✅ Doesn’t overreact to a single hot or cold session

📅 Only uses recent data

📏 Skips distances where there's not enough data

🎯 Prioritizes tougher and more reliable samples



## Tech Stack
- **Kotlin**
- **Jetpack Compose** (UI)
- **Room** (local database)
- **Material 3** (theming)
- **Navigation Compose** (screen navigation)

## Contributing
Pull requests and suggestions are welcome! Please open an issue or submit a PR.

## License
*Specify your license here (e.g., MIT, Apache 2.0, etc.)*

---

*Just Put It In - Track your putts, improve your game!* 