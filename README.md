# Run-With-Me
A mobile app that record running metrics(time, duration, trace, speed, steps) of yourself and your friend list. Aiming to encourage people to keep their exercise routine by building social connection with their running partners.

## Calorie Calculation Method

Our app uses scientifically-proven formulas to calculate calorie expenditure during workouts. The calculation adapts based on the type of activity (walking vs. running) and uses research-backed methods from exercise physiology.

### Calculation Formulas

#### Distance-Based Calculation (Primary Method)

We primarily use distance-based formulas, which are widely recognized in exercise physiology:

- **Walking** (speed < 2.23 m/s or 8.0 km/h):
  ```
  Calories = 0.57 × Body Weight (kg) × Distance (km)
  ```

- **Running** (speed ≥ 2.23 m/s or 8.0 km/h):
  ```
  Calories = 1.036 × Body Weight (kg) × Distance (km)
  ```

#### MET-Based Calculation (Fallback Method)

When distance data is minimal, we use the MET (Metabolic Equivalent of Task) formula:

```
Calories = MET × Body Weight (kg) × Duration (hours)
```

**MET Values by Activity Type** (based on ACSM standards):
- Walking: 3.5 MET
- Brisk Walking: 4.5 MET
- Jogging: 7.0 MET
- Running: 9.8 MET
- Fast Running: 12.3 MET

### Scientific Basis

1. **Distance Formula Coefficient (1.036)**: Derived from oxygen consumption and energy conversion research in exercise physiology. This coefficient represents the net energy cost of running per kilogram of body weight per kilometer.

2. **Walking vs. Running Threshold (2.23 m/s)**: This speed represents the physiological transition point where the body naturally switches from a walking gait to a running gait, with different biomechanical efficiency.

3. **MET Values**: Sourced from the American College of Sports Medicine (ACSM) guidelines, representing the ratio of working metabolic rate to resting metabolic rate.

### Example Calculation

**Scenario**: 65 kg person runs 5 km in 30 minutes

- **Speed**: 5000 m ÷ 1800 s = 2.78 m/s (Running)
- **Formula Used**: Running distance-based formula
- **Calculation**: 65 × 5 × 1.036 = **337 kcal**

This provides a more accurate estimate compared to simplified time-only or fixed-coefficient methods.

## How to Run

1. **Clone the Project**  
   Clone the project in Android Studio.

2. **Wait for Dependencies**  
   Android Studio will automatically download and set up the required dependencies.  

3. **Run the Frontend (App)**  
   Click the **Run ▶️ button** in the top-right corner of Android Studio.  
   Choose an emulator or a physical device to launch the Android app.  

4. **Run the Backend (Ktor Server)**  
   Open `backend1/main/java/Application.kt`, right-click, and select **Run 'Application.kt'**.  
   Once started, the backend server will be running on the configured port.
