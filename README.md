# Run-With-Me
A mobile app that record running metrics(time, duration, trace, speed, steps) of yourself and your friend list. Aiming to encourage people to keep their exercise routine by building social connection with their running partners.

## Build & Run Instructions

### Prerequisites
- Ensure you have Java Development Kit (JDK) 11 or higher installed.
- Install Android Studio (latest stable version recommended).
- Install Gradle (if not bundled with Android Studio).
- For the backend, ensure you have Maven installed.

### Steps to Build and Run

#### Frontend (Android App)
1. Open Android Studio.
2. Click on `File > Open` and select the `Run-With-Me` project directory.
3. Let Android Studio sync the Gradle files.
4. Connect an Android device or start an emulator.
5. Click on the `Run` button (green triangle) in Android Studio to build and run the app.

#### Backend (Spring Boot Application)
1. Open a terminal and navigate to the `backend/rwm` directory.
2. Run the following command to build the backend:
   ```bash
   ./mvnw clean install
   ```
3. Start the backend server by running:
   ```bash
   ./mvnw spring-boot:run
   ```
4. The backend will be accessible at `http://localhost:8080`.

### Configure Network for Real Device

When running the app on a real device, ensure the following:

1. **Update `BASE_URL` in RetrofitClient**
   - Open the file `app/src/main/java/com/example/myapplication/landr/RetrofitClient.kt`.
   - Replace the default `BASE_URL` with the local IP address of the computer running the backend server. For example:
     ```kotlin
     const val BASE_URL = "http://192.168.x.x:8080"
     ```

2. **Ensure Same Network**
   - Connect both the real device and the computer running the backend to the same Wi-Fi network.

3. **Verify Backend Accessibility**
   - Open a browser on the real device and navigate to `http://<backend_IP>:8080`.
   - Ensure the backend service is accessible from the real device.

If the backend is not accessible, check the network configuration and firewall settings.

### Notes
- Ensure the backend is running before using the app to access its full functionality.
- Update the `local.properties` file in the `app` module to point to the backend URL if necessary.
