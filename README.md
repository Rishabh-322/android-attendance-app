# Android Attendance App

This is an Android attendance application I built as part of a hiring assignment.

I have previously worked with Flutter and Dart for cross-platform mobile development, but this was my first time building an application using Kotlin and the native Android ecosystem.

The main goal of the project was to create a simple attendance system where an Admin can register staff members and enroll their faces, while Staff can mark attendance using selfie-based face verification.

When attendance is successfully marked, the application stores the timestamp, device location and captured selfie locally on the device.


## Features

The application has two user roles:

- Admin
- Staff


### Admin

The Admin can:

- Login using the demo Admin account
- View the list of staff members
- Add a staff member using their name and employee ID
- Open an individual staff profile
- Enroll a staff member's face
- Re-enroll the face if required
- View attendance information


### Staff

Staff members can:

- Login using their employee ID
- Check whether their face has been enrolled
- Mark attendance using the front camera
- Verify their face against the enrolled face
- See today's attendance status

Attendance is only recorded when the captured face matches the enrolled face.


## Technology Used

The application was built using:

- Kotlin
- Jetpack Compose
- Navigation Compose
- Room Database
- CameraX
- Google ML Kit Face Detection
- TensorFlow Lite
- MobileFaceNet
- Google Play Services Location API
- Android internal file storage


## How the App Works

I kept the application completely local for this assignment, so it does not require a backend server or cloud database.

Jetpack Compose is used to build the user interface, and Navigation Compose handles navigation between the different screens.

Room Database is used to store staff details, enrolled face data and attendance records.

CameraX is used for the camera preview and selfie capture.

Google ML Kit is used to detect a face in the captured image before trying to perform face recognition.

For face recognition, I used a MobileFaceNet TensorFlow Lite model.

The model converts a detected face into a numerical representation called a face embedding.

When the Admin enrolls a staff member, this embedding is generated and stored against that staff member.

When the staff member later tries to mark attendance, the application captures another selfie and generates a new embedding.

The new embedding is then compared with the stored embedding using cosine similarity.

If the similarity is above the configured threshold, the face is treated as a match and attendance can be recorded.


## Application Flow

### Face Enrollment

The face enrollment flow works like this:

1. Admin logs into the application.
2. Admin creates a staff member using their name and employee ID.
3. Admin opens the staff member's profile.
4. Admin selects the face enrollment option.
5. The front camera is opened.
6. A photo is captured.
7. ML Kit checks whether a face is present.
8. MobileFaceNet generates a face embedding.
9. The embedding is stored in the Room database against that staff member.


### Attendance

The attendance flow works like this:

1. Staff logs in using their employee ID.
2. Staff selects **Mark Attendance**.
3. The front camera opens.
4. A selfie is captured.
5. ML Kit checks whether a valid face is present.
6. MobileFaceNet generates a new face embedding.
7. The new embedding is compared with the enrolled embedding.
8. If the face matches, the application retrieves the current device location.
9. The attendance selfie is saved to internal application storage.
10. An attendance record is saved in Room.

The attendance record contains:

- Staff ID
- Timestamp
- Latitude
- Longitude
- Selfie file path

If the face does not match, attendance is rejected and no attendance record is created.


## Face Matching

Face embeddings are compared using cosine similarity.

The current matching threshold used in the application is:

```text
0.65
```

During my testing, the same enrolled person generally produced similarity values around:

```text
76%
78%
80%
```

Testing with a different person produced values around:

```text
34%
36%
38%
```

Based on these results, I used `0.65` as the threshold for this demo.

This threshold is only meant for the assignment and has not been calibrated for a production biometric authentication system.


## Demo Credentials

### Admin

Username:

```text
admin
```

Password:

```text
admin123
```


### Staff

Example Employee ID:

```text
EMP001
```

Password:

```text
staff123
```

The staff member needs to be created by the Admin first.

The employee ID entered while creating the staff member is then used during Staff login.


## How to Run the Project

### 1. Clone the repository

```bash
git clone <repository-url>
```

Move into the project directory:

```bash
cd AttendanceApp
```


### 2. Open the project in Android Studio

Open Android Studio and select:

```text
Open
```

Then select the `AttendanceApp` project directory.


### 3. Wait for Gradle Sync

Android Studio will download and configure the required dependencies.

Wait for Gradle Sync to finish successfully before running the application.


### 4. Start an Android Device

The application can be run using:

- An Android emulator
- A physical Android device

A working front camera is required for face enrollment and attendance testing.

For an emulator, make sure the configured camera can provide an image to the application.


### 5. Run the Application

Select the Android device or emulator from the device list in Android Studio.

Then click the green **Run** button.


## Testing the Application

A simple way to test the complete flow is:

1. Login as Admin using:

```text
Username: admin
Password: admin123
```

2. Add a new staff member.

For example:

```text
Name: Test User
Employee ID: EMP001
```

3. Open the staff member's profile.

4. Select the option to enroll their face.

5. Capture the face image.

6. Wait for the successful enrollment message.

7. Logout from the Admin account.

8. Login as Staff using:

```text
Employee ID: EMP001
Password: staff123
```

9. Select **Mark Attendance**.

10. Capture a selfie.

11. Allow location permission if requested.

12. If the captured face matches the enrolled face, the attendance record will be saved.

13. The staff dashboard will show the latest attendance information.


## Building the APK

The debug APK can also be generated directly from the terminal.

From the project directory:

```bash
cd ~/Projects/AttendanceApp
```

Run:

```bash
./gradlew assembleDebug
```

After a successful build, the APK can be found at:

```text
app/build/outputs/apk/debug/app-debug.apk
```


## Permissions

The application requires the following permissions.


### Camera

Camera permission is required for:

- Face enrollment
- Attendance selfie capture


### Location

Location permission is required when recording attendance.

The application stores the latitude and longitude returned by the device along with the attendance record.


## Data Storage

The application uses Room Database for local persistence.

Room stores information such as:

- Staff name
- Employee ID
- Face embedding
- Attendance timestamp
- Latitude
- Longitude
- Selfie path

The actual attendance selfie is stored separately in the application's internal file storage.

There is currently no remote backend or cloud database.


## Project Structure

The main parts of the application are separated into different packages.


### `data/`

Contains the Room database related code, including:

- Staff entity
- Attendance entity
- DAO classes
- Database setup
- Attendance selfie storage


### `face/`

Contains the face recognition related logic, including:

- Face detection
- Image processing
- MobileFaceNet embedding generation
- Face comparison


### `location/`

Contains the code used to retrieve the current device location.


### `navigation/`

Contains the application's navigation flow and connects the different screens together.


### `ui/`

Contains the Jetpack Compose screens used throughout the application.


## Assumptions and Limitations

This project was built as a hiring assignment and is intended as a working demonstration rather than a production biometric attendance system.


### Local Storage Only

All application data is stored locally on the Android device.

There is currently no cloud synchronization or remote backend.

If the application data is cleared or the application is uninstalled, the stored information may also be lost.


### Demo Authentication

The current Admin and Staff authentication uses simple demo credentials.

A production system should use proper authentication and securely store user credentials.


### Face Recognition Threshold

The `0.65` similarity threshold was selected based on testing during development.

It has not been tested against a large number of users or calibrated for production use.


### Face Recognition Conditions

Recognition results can be affected by factors such as:

- Lighting
- Camera quality
- Face angle
- Distance from the camera
- Image quality

A production biometric system would require much more extensive testing.


### Spoof Detection

The current version verifies similarity between face embeddings, but it does not include advanced liveness detection.

A production system should include protection against attempts such as showing a photograph to the camera.


### Location Accuracy

Location accuracy depends on the Android device, GPS availability and location settings.

When running the application on an emulator, the location may be simulated.


## What I Learned

I already had experience developing cross-platform mobile applications using Flutter and Dart, so concepts such as screens, navigation and building mobile interfaces were familiar to me.

The new part for me in this assignment was working with Kotlin and the native Android development ecosystem.

During the project I worked with:

- Kotlin for Android development
- Jetpack Compose for UI
- Navigation Compose
- Room Database
- CameraX
- Android permissions
- Google ML Kit
- TensorFlow Lite
- Face embeddings
- Cosine similarity
- Android location services
- Local file storage

One of the main challenges was connecting all of the separate parts into one complete flow.

The camera, face detection, face recognition, location retrieval, file storage and Room database all worked as separate components first.

The final step was connecting them together so that the attendance process became:

```text
Capture selfie
      ↓
Detect face
      ↓
Generate face embedding
      ↓
Compare with enrolled face
      ↓
Get device location
      ↓
Save selfie
      ↓
Save attendance record
```

Working through that process gave me a much better understanding of how native Android applications are structured compared with the Flutter applications I had worked on previously.
