```markdown
# 🚌 Kondosthara — Public Transport Companion App

**Kondosthara** is a mobile application designed to modernize and streamline Sri Lanka’s public transportation system. It offers a one-stop solution for passengers, bus owners, conductors, and drivers—specifically focusing on **highway buses**.

Key features include:
- 🚍 Real-time bus tracking
- 💳 Cashless payments
- 📍 Live location and route details

---

## 📱 Tech Stack

- **Kotlin**
- **Android Studio**
- **Firebase Realtime Database**
- **Google Maps API**

---

## 🧑‍🤝‍🧑 Target Users

- Passengers
- Bus Owners
- Conductors
- Drivers

Each user role gets access to customized functionalities tailored to their transportation needs.

---

## ⚙️ Project Setup

### 🔑 Prerequisites

- [Android Studio](https://developer.android.com/studio) installed  
- A valid **Google Maps API Key**  
- Firebase project with **Realtime Database** enabled

### 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Kondosthara.git
   ```

2. Open the project in Android Studio.

3. Add your **Google Maps API key** in the `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE"/>
   ```

4. Set up **Firebase**:
   - Create a Firebase project.
   - Enable Realtime Database.
   - Connect Firebase to your Android Studio project.
   - Add the `google-services.json` file to your `app/` directory.

---

## 📌 Note

Kondosthara is still under development and is currently tailored to highway bus operations. Future updates may expand support to other transport modes and introduce more features like route planning, fare estimation, and offline access.

---

## 📷 Screenshots (Coming Soon)

---

Feel free to contribute or raise issues. Let’s build a smarter transport experience for Sri Lanka!
```