**Proposal Draft: Run with Me App**

**1\. Introduction**

The **Run with Me** app is designed to combine fitness tracking with social interaction. Users can log their running/walking activities, monitor performance metrics, connect with friends, and engage in a gamified fitness community. The app aims to motivate individuals to exercise regularly through coaching, goal setting, and social sharing.

**2\. Structure Diagram**

<img width="1777" height="1945" alt="Image" src="https://github.com/user-attachments/assets/60d0b4d9-2cde-40fc-b4b9-d519e85375e8" />

**3\. User Stories (with Acceptance Criteria)**

1. Authentication & Profile
US 1.1 – Sign In & Basic Profile
As a user, I want to sign in and set my display name & timezone so that my records and “today” logic work correctly.

Supports email/password (or social login) with persistent session.
Profile includes display name, avatar (optional), timezone (default from device).
“Today” is computed using the member’s profile timezone.
Sign-out clears local sensitive data.
Size: S Difficulty: Low Priority: must-have

US 1.2 - Personal Training Goals
As a user, I want to input my Fitness Level (Beginner/Intermediate/Advanced), Weekly Availability (sessions/week, preferred days, max session duration), Height/Weight and Fitness Goal

Needs a suitable algorithm to calculate the goal.
Needs research papers to support the algorithm.
Size: M Difficulty: Low Priority: must-have

2. Social & Groups
US 2.1 – Group Membership & Access Control
As a user, I want to view my groups and leave a group so that I control my participation.

“My Groups” lists groups with member count & progress.
Enter group → group home (calendar + progress).
Leave group: confirmation required; after leaving, user loses access to group data.
Only members can read that group’s data and send reminders within it.
Size: S Difficulty: Low Priority: P0

US 2.2 – Member Tile Actions (Remind vs Like)
As a group member, I want each teammate’s tile to show the right action—Remind if they haven’t checked in today, Like if they have—so that I can nudge or encourage with one tap.

For each other member (not self):
Not checked in today → “Remind” button.
Checked in today → “Like” button with count.
UI states: default / pressed / disabled; optimistic update with server confirmation.
Cannot like the same member more than once per day; can undo like within the same day (toggles count).
“Remind” is disabled if sender hit daily limit for this group.
Size: S Difficulty: Low Priority: P1

US 2.3 – Generate the QR code for Users/Groups
As a user, I want to add other people as my friends or join a groups by scanning their QR code.

Each User/Group can generate their own QR code.
User is able to scan a QR code and it will let them to send join request to "add this person as your friend" or "join the Group".
When other user accept the request, their relationship will be built.
Size: L Difficulty: High Priority: P1

3. Running & Workout Tracking
US 3.1 – Select Workout Mode
As a runner, I want to choose outdoor run, treadmill, or walk so that my tracking fits the activity.

Mode chosen before start; outdoor requires GPS permissions on.
Displayed metrics adapt to mode (e.g., pace+map for outdoor).
Mode is stored with workout.
Size: S Difficulty: Low Priority: P0

US 3.2 – Track Live Workout Data
As a runner, I want real-time data so I can monitor performance during a session.

Shows duration, distance, average pace, calories, heart rate (if available).
Updates every ≤1s; lock-screen display (configurable).
Accuracy within ±5% of device sensors (given permissions granted).
Handles GPS loss gracefully with status banners/fallback.
Size: L Difficulty: High Priority: P1

US 3.3 - AI Coach Voice Feedback
As a runner, I want periodic voice prompts so I can adjust without looking at my phone.

Configurable triggers: per 1 km and/or per 5 min.
Content options: current pace, distance, calories, motivational phrase.
Works with speaker & Bluetooth; users can toggle items in Settings.
Size: M Difficulty: Medium Priority: P2

4. Workout Records & Visibility
US 4.1 – Save Workout with Details & Visibility
As a user, I want to see summaries and histories or each previous workout.

For each workout: steps, duration, distance, pace, calories, heart rate, mode...
Summaries (**TODO: ** think about giving some insights to show the progress With Charts: weekly/monthly totals, streak visual.)
Visibility: Group (shared to the selected group) or Only me (default follows profile setting).
Size: S Difficulty: Low Priority: P0

5. Group Progress
US 5.1 Group Check-In Calendar
As a group member, I want a calendar that shows who checked in each day so that we track team consistency.

Month view shows per-member check-in dots; tap a day to see member summaries (e.g., “2.3 km / 18 min”).
Filter by member; show streak counts.
Only shows data since a member joined the group.
Size: M Difficulty: Medium Priority: P0

US 5.2 – Personal Day Goal, Check-In & Streaks
As a user, I want a clear definition of daily check-in and streak so that consistency is tracked fairly.

A check-in is achieved when a saved workout meets the day goal (e.g., ≥1 km or ≥15 min), evaluated in the user’s timezone.
Streak increases by 1 when day goal met; breaks if missed (no backdating).
Today’s status drives the Remind/Like state in member tiles (US3).
Edge cases: multiple workouts in a day aggregate toward the goal; once met, additional workouts don’t change “checked-in” state.
Size: S Difficulty: Low Priority: P0

US 5.3 – Group Project Progress (Flower Growing/Spaceship Repairing)
As a group member, I want my daily check-ins to add points to a shared project so that our team progresses together.

Each weekly goal achieved by group member counts for 10 points.
If all group members achieved their weekly goals, double the credits.
Design the game for credits using.
Progress log shows who contributed when/how many.
Size: L Difficulty: Medium Priority: P1

6. Settings
US 6.1 – Privacy & Permissions
As a user, I want control over data visibility and permissions so that I feel safe using the app.

Default visibility for new workouts: Group / Only me.
Permission prompts appear the first time features need them (GPS, notifications, microphone for TTS).
Size: S Difficulty: Low Priority: P0

US 6.2 – Workout & Display Settings
As a user, I want to customize my username, password, workout goals, physical situation and app appearance(dark/light mode) so that the app fits my preferences.

Password updating needs security check (like: email confirming).
Changes apply immediately and persist.
Size: S Difficulty: Low Priority: P1

7.Shake
US 7.1 – Shake to Add Friends
As a user, I want to shake my phone to discover and add nearby users so that I can connect with friends in a fun and convenient way.

User can find other who shake in 5 minutes. When two users shake their phones simultaneously, the app detects proximity and shows each other’s profiles.
Works only if both users have granted location and Bluetooth permissions.
Permission prompts appear the first time this feature is used.

Size: M Difficulty: Large Priority: P2
