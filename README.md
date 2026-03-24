# CardScoreTracker

A modern Android application built with **Kotlin** and **Jetpack Compose** for tracking, managing, and analyzing scores and rounds across various popular card game formats.

## Features

- **Multiple Game Modes**: Play with different rulesets seamlessly:
  - **240 Mode**: Keep playing rounds until a player reaches 240+ points. Includes a strategic "Chance" system allowing players to use up to 3 chances to limit round damage.
  - **7s Mode**: A game of exactly 7 rounds, where the first and last rounds score double!
  - **5s Mode**: A faster game spanning 5 rounds, also with double scores on the first and last rounds.
- **Auto-Save & Persistent History**: Your games are saved automatically via Room Database. Go back and review old games, round-by-round point allocations, and find out who lost.
- **Export to CSV**: Extract your game history into a CSV spreadsheet. Download the report directly or share it instantly with friends through any messaging application!
- **Smart Player Setup**: As you play, the app remembers player names to quickly auto-complete future match setups.
- **Beautiful Modern UI**: Fully designed in Jetpack Compose featuring Material Design 3 components, smooth transitions, and dynamic Dark/Light Theme support.

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel) approach for robust state management.
- **Asynchronous Processing**: Kotlin Coroutines & `StateFlow`
- **Database Components**: Room Database with KSP annotation processing.
- **Navigation**: Jetpack Navigation Compose.
- **Exporting API**: Core Android IO, Context Intents, and FileProvider.

##  Screenshots 

<p align="center">
  <img width="24%" alt="Screenshot_20260324-130307" src="https://github.com/user-attachments/assets/5bcd57f4-5f38-4a8b-9d6f-82373095ba1a" />
  <img width="24%" alt="Screenshot_20260324-130330" src="https://github.com/user-attachments/assets/3ca977a6-e985-4dd1-8218-fe59d4a6f495" />
  <img width="24%" alt="Screenshot_20260324-130439" src="https://github.com/user-attachments/assets/570fd3cb-750e-48f7-a7a2-2784282db966" />
  <img width="24%" alt="Screenshot_20260324-130335" src="https://github.com/user-attachments/assets/080e67bd-6f0f-425d-a1e4-26df6530ae0c" />
</p>

## Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/Cards-Android.git
   ```
2. **Open the project** in the latest stable version of [Android Studio](https://developer.android.com/studio).
3. **Wait for Gradle sync** to download the necessary UI and Room architecture dependencies.
4. **Run the application** on an Android Emulator or a physical device targeting API 31+.

## Project Structure

- `data/`: Contains `Room` database classes (`AppDatabase`, `GameDao`), Entity representations (`GameEntity`, `RoundEntity`, `PlayerScoreEntity`), and the `ExcelExporter` utility.
- `model/`: Data classes representing pure business domain logic (`GameMode`, `Player`).
- `ui/screens/`: Composable screens making up the View layer (`PlayerSetupScreen`, `GameScreen`, `HistoryScreen`, `HistoryDetailScreen`).
- `viewmodel/`: The primary `GameViewModel` coordinating state updates and tying backend business rules to frontend View states.
- `MainActivity.kt`: The single-activity entry point hosting application navigation.

## Contribution

Contributions, issues, and feature requests are welcome! 
Feel free to check [issues page](https://github.com/your-username/Cards-Android/issues) if you have any suggestions or find any bugs.

