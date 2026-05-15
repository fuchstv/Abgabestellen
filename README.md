# Abgabestellen Berlin

An Android application built with Jetpack Compose that helps users find and manage drop-off points (Abgabestellen) in Berlin. It provides an interactive map, a list view of locations, and integrates with Firebase as a backend for storing and managing drop-off point data and user suggestions.

## Features

* **Interactive Map View:** Browse drop-off points across Berlin using Google Maps integration.
* **List View:** Scroll through a detailed list of all available drop-off points.
* **Detailed Information:** View specifics for each drop-off point, including:
    * Address (Zip Code, District, Neighborhood)
    * Opening / Drop-off times
    * Accepted items
    * Contact information and remarks
* **User Authentication:** Log in seamlessly using Google Sign-In with Firebase Authentication.
* **Suggest Changes:** Logged-in users can suggest changes or updates to existing drop-off points. Suggestions are automatically sent to the backend.
* **Admin / Collaborator Mode:** Users authorized as "Mitarbeiter" get access to an admin panel.
* **Firebase Backend:** The app uses Firebase Firestore to fetch location data, manage collaborators, and store user suggestions.

## Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose, Material Design 3
* **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
* **Maps Integration:** Google Maps Compose (`com.google.maps.android:maps-compose`)
* **Authentication:** Firebase Authentication (`com.google.firebase:firebase-auth`) with Google Sign-In (`com.google.android.gms:play-services-auth`)
* **Backend & API:** Firebase Firestore (`com.google.firebase:firebase-firestore`)
* **Concurrency:** Kotlin Coroutines

## Setup and Installation

To build and run this project locally, you need to configure your own Google Cloud and Firebase projects.

### 1. Google Cloud & Firebase Setup

1.  Go to the [Firebase Console](https://console.firebase.google.com/).
2.  Create a new project.
3.  Add an Android app to your Firebase project. You will need your app's package name (`com.example.abgabestellenberlin`) and your debug keystore SHA-1 fingerprint.
4.  Download the generated `google-services.json` file.
5.  Enable Authentication in Firebase and set up the Google Sign-In provider.
6.  Enable Cloud Firestore database in your Firebase project.
7.  Go to the [Google Cloud Console](https://console.cloud.google.com/) for your Firebase project and ensure the **Maps SDK for Android** API is enabled.
8.  Create or locate the API Key for Google Maps in the Google Cloud Console credentials section.

### 2. Project Configuration

1.  Clone the repository and open it in Android Studio.
2.  **Firebase Configuration:**
    * Place the `google-services.json` file you downloaded into the `app/` directory of the project.
3.  **Google Maps API Key:**
    * Open `app/src/main/AndroidManifest.xml`.
    * Replace the placeholder API key in the `<meta-data>` tag with your actual Google Maps API Key:
        ```xml
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_GOOGLE_MAPS_API_KEY" />
        ```
4.  **Firestore Setup:**
    * Ensure your Firestore database has the following collections with the correct document fields:
        * `abgabestellen`: Documents should contain fields like `name`, `anschrift`, `plz`, `ortsteil`, `ansprechpartner`, `telefon`, `annahmezeiten`, `akzeptiert`, `anmeldungNoetig` (boolean), `bemerkungen`, and `location` (GeoPoint).
        * `vorschlaege`: Used for storing user suggestions.
        * `mitarbeiter`: Documents should use the admin email addresses as the document ID to manage access.

### 3. Build and Run

1.  Sync the project with Gradle files.
2.  Select an emulator or connect a physical device.
3.  Click **Run 'app'** in Android Studio.

## Architecture

The app follows the recommended Android Architecture guidelines:
* **UI Layer:** Composed of Jetpack Compose screens (`MainScreen`, `MapScreen`, `ListScreen`, `ProfileScreen`) and ViewModels (`MainViewModel`) that expose state using `StateFlow`.
* **Data Layer:** Contains the `DropOffRepository` which acts as the single source of truth, fetching data from Firebase Firestore.
* **Network/API:** Firebase SDK handles authentication and direct API calls to Firestore.

## License

This project is licensed under the Apache License 2.0 - see the respective Gradle files for details.
