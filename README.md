# 📏 AR Ruler

An ARCore-based Android application that measures real-world distances using anchor-based plane detection.

Built using Kotlin and ARCore without depth API support.

---

## 🚀 Features

- 📍 Anchor-based 3D measurement using ARCore
- 🎯 Crosshair-based point placement
- 📏 Real-time distance calculation (cm & inches)
- 🔄 Undo and Retrack functionality
- 🌀 AR session loading indicator
- 📐 Plane locking for improved stability
- 🎨 Clean white minimalist UI overlay
- 📊 Kalman filter smoothing to reduce SLAM jitter

---

## 🛠 Tech Stack

- Kotlin
- ARCore
- Sceneform
- Android SDK
- Custom Canvas Overlay Rendering

---

## 📱 How It Works

1. Move phone to detect a surface
2. Tap to place first anchor point
3. Move to second location
4. Tap again to measure distance
5. Distance shown in centimeters (switchable to inches)

---

## 📸 Screenshots

_Add screenshots in the screenshots folder and link here._

Example:


---

## ⚠ Limitations

- Accuracy may vary on non-depth devices due to ARCore SLAM refinement
- Works best on textured, well-lit flat surfaces
- Minor anchor drift may occur due to world origin refinement

---

## 🔮 Future Improvements

- Depth API integration (if supported)
- Multi-point polygon measurement
- Area calculation
- Measurement history
- Screenshot export feature
- UI enhancements & animations

---

## 👨‍💻 Author

**Thulasihan Elitchelvan**

---

## ⭐ If You Like This Project

Give it a star on GitHub!