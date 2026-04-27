# Abgabestellen Berlin

An Android application built with Jetpack Compose that helps users find and manage drop-off points (Abgabestellen) in Berlin. It provides an interactive map, a list view of locations, and integrates with Google Sheets as a backend for storing and managing drop-off point data and user suggestions.

## Features

* **Interactive Map View:** Browse drop-off points across Berlin using Google Maps integration.
* **List View:** Scroll through a detailed list of all available drop-off points.
* **Detailed Information:** View specifics for each drop-off point, including:
    * Address (Zip Code, District, Neighborhood)
    * Opening / Drop-off times
    * Accepted items
    * Contact information and remarks
* **User Authentication:** Log in seamlessly using Google Sign-In.
* **Suggest Changes:** Logged-in users can suggest changes or updates to existing drop-off points. Suggestions are automatically sent to the backend.
* **Admin / Collaborator Mode:** Users authorized as "Mitarbeiter" get access to an admin panel.
* **Google Sheets Backend:** The app uses a Google Spreadsheet to fetch location data, manage collaborators, and store user suggestions.

## Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose, Material Design 3
* **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
* **Maps Integration:** Google Maps Compose (`com.google.maps.android:maps-compose`)
* **Authentication:** Google Play Services Auth (`com.google.android.gms:play-services-auth`)
* **Backend & API:** Google Sheets API v4 (`com.google.apis:google-api-services-sheets`)
* **Concurrency:** Kotlin Coroutines

## Setup and Installation

To build and run this project locally, you need to configure your own Google Cloud project and API keys.

### 1. Google Cloud Console Setup

1.  Go to the [Google Cloud Console](https://console.cloud.google.com/).
2.  Create a new project.
3.  Enable the following APIs:
    * **Maps SDK for Android**
    * **Google Sheets API**
4.  Create Credentials:
    * **API Key:** Generate an API Key for Google Maps.
    * **OAuth 2.0 Client ID:** Create an OAuth client ID for Android. You will need your app's package name (`com.example.abgabestellenberlin`) and your debug keystore SHA-1 fingerprint.

### 2. Project Configuration

1.  Clone the repository and open it in Android Studio.
2.  **Google Maps API Key:**
    * Open `app/src/main/AndroidManifest.xml`.
    * Replace the placeholder API key in the `<meta-data>` tag with your actual Google Maps API Key:
        ```xml
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="YOUR_GOOGLE_MAPS_API_KEY" />
        ```
    * *Note: You may also need to update the `apiKey` variable in `GoogleSheetsService.kt` if you rely on API keys for public sheet access.*
3.  **Google Sign-In Configuration:**
    * Ensure the package name in your Google Cloud OAuth consent screen and credentials matches the app.
    * Download the generated `client_secret_*.json` from Google Cloud and place it in the project root if required by your build setup (though not strictly necessary for standard Android Google Sign-In which relies on the SHA-1 fingerprint).
4.  **Google Sheets Setup:**
    * The app reads from a specific Spreadsheet ID defined in `GoogleSheetsService.kt`.
    * To use your own sheet, change the `spreadsheetId` in `GoogleSheetsService.kt`.
    * Ensure your spreadsheet has the following sheets (tabs) with the correct columns:
        * `Abgabestellen`: Columns A to P (Name, Address, ZipCode, District, Neighborhood, ContactPerson, Phone, DropOffTimes, AcceptedItems, PhoneRegistrationRequired, Capacity, Remarks, Website, LastUpdated, Latitude, Longitude).
        * `Vorschläge`: For user suggestions.
        * `Mitarbeiter`: Column A for admin email addresses.

### 3. Build and Run

1.  Sync the project with Gradle files.
2.  Select an emulator or connect a physical device.
3.  Click **Run 'app'** in Android Studio.

## Architecture

The app follows the recommended Android Architecture guidelines:
* **UI Layer:** Composed of Jetpack Compose screens (`MainScreen`, `MapScreen`, `ListScreen`, `ProfileScreen`) and ViewModels (`MainViewModel`) that expose state using `StateFlow`.
* **Data Layer:** Contains the `DropOffRepository` which acts as the single source of truth, fetching data via `GoogleSheetsService`.
* **Network/API:** `GoogleSheetsService` handles OAuth credentials, public access, and direct API calls to Google Sheets using the Java API client.

## License

This project is licensed under the Apache License 2.0 - see the respective Gradle files for details.