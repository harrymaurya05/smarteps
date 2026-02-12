# 📱 How to Add EPS Files to Emulator for Testing

## Method 1: ADB Push (Command Line - Fastest) ⚡

### Step 1: Ensure Emulator is Running
```bash
# Check connected devices
adb devices
```

### Step 2: Push EPS File to Emulator
```bash
# Push a file to the Downloads folder
adb push /path/to/your/file.eps /sdcard/Download/

# Example:
adb push ~/Desktop/sample.eps /sdcard/Download/
```

### Step 3: Verify File Was Copied
```bash
adb shell ls -la /sdcard/Download/*.eps
```

---

## Method 2: Drag and Drop (Easiest - GUI) 🖱️

### Steps:
1. **Start your emulator**
2. **Locate your EPS file** on your Mac
3. **Simply drag the .eps file** and **drop it onto the emulator window**
4. File will be automatically copied to `/sdcard/Download/`
5. Open your app and use file picker to find it

---

## Method 3: Using Android Studio Device File Explorer 📂

### Steps:
1. Open **Android Studio**
2. Go to **View → Tool Windows → Device File Explorer**
3. Navigate to `/sdcard/Download/` in the explorer
4. Right-click on the `Download` folder
5. Select **Upload...**
6. Choose your EPS file(s) from your laptop
7. Click **OK**

---

## Method 4: Create Sample EPS Files on Emulator 📝

If you don't have EPS files, you can download samples:

```bash
# Download sample EPS files to your Mac first
curl -o ~/Desktop/sample1.eps "https://www.epsfiles.com/sample.eps"

# Then push to emulator
adb push ~/Desktop/sample1.eps /sdcard/Download/
```

---

## Quick Commands for Your Use Case

### Push Multiple EPS Files at Once:
```bash
# Navigate to folder with EPS files
cd /path/to/your/eps/files/

# Push all EPS files
for file in *.eps; do
    adb push "$file" /sdcard/Download/
done
```

### Create a Test EPS File (Simple PostScript):
```bash
# Create a simple EPS test file on your Mac
cat > ~/Desktop/test.eps << 'EOF'
%!PS-Adobe-3.0 EPSF-3.0
%%BoundingBox: 0 0 200 200
%%Title: Test EPS File
%%Creator: TestScript
%%Pages: 1
%%EndComments

% Draw a red rectangle
1 0 0 setrgbcolor
50 50 100 100 rectfill

% Draw black border
0 0 0 setrgbcolor
2 setlinewidth
50 50 100 100 rectstroke

% Add text
/Helvetica findfont 20 scalefont setfont
60 90 moveto
(TEST EPS) show

showpage
%%EOF
EOF

# Now push it to emulator
adb push ~/Desktop/test.eps /sdcard/Download/
```

---

## Testing Your App After Adding Files

### Step 1: Launch Your App
```bash
# Install the app (if not already installed)
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.example.epsviewer/.ui.splash.SplashActivity
```

### Step 2: In the App:
1. Tap the **"Convert" tab** (bottom navigation)
2. Tap the large blue **"SELECT EPS FILE"** button
3. Choose **"Downloads"** from the file picker
4. Select your EPS file
5. File should open in the viewer!

---

## Troubleshooting

### Issue: File Not Showing in File Picker
**Solution:** Refresh the media database
```bash
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
    -d file:///sdcard/Download/yourfile.eps
```

### Issue: Permission Denied
**Solution:** Use different location
```bash
# Try Documents folder instead
adb push your-file.eps /sdcard/Documents/
```

### Issue: File Picker Shows No Files
**Solution:** Grant storage permissions manually
```bash
# Grant storage permissions to your app
adb shell pm grant com.example.epsviewer android.permission.READ_EXTERNAL_STORAGE
```

---

## Recommended Workflow for Testing

### 1. Prepare Test Files on Mac:
```bash
# Create a test folder
mkdir -p ~/EPS_Test_Files

# Add your EPS files there
# Or create test files (see above)
```

### 2. Push All Files to Emulator:
```bash
# Push all files at once
cd ~/EPS_Test_Files
for file in *.eps; do
    echo "Pushing $file..."
    adb push "$file" /sdcard/Download/
done
```

### 3. Verify Files Are There:
```bash
adb shell ls -lh /sdcard/Download/*.eps
```

### 4. Test the App:
- Open app
- Go to Convert tab
- Select file
- Test conversion to PNG/JPG

---

## Quick Test Script

Save this as `push-eps-files.sh`:

```bash
#!/bin/bash

# Push EPS Files to Android Emulator
echo "📱 Pushing EPS files to emulator..."

# Check if emulator is running
if ! adb devices | grep -q "emulator"; then
    echo "❌ No emulator detected. Please start the emulator first."
    exit 1
fi

# Push files
EPS_DIR="$1"
if [ -z "$EPS_DIR" ]; then
    echo "Usage: $0 /path/to/eps/files/"
    exit 1
fi

cd "$EPS_DIR"
count=0
for file in *.eps; do
    if [ -f "$file" ]; then
        echo "Pushing: $file"
        adb push "$file" /sdcard/Download/
        ((count++))
    fi
done

echo "✅ Pushed $count EPS files to emulator"
echo "📂 Files are in /sdcard/Download/"
```

### Usage:
```bash
chmod +x push-eps-files.sh
./push-eps-files.sh ~/Desktop/my-eps-files/
```

---

## Summary - Fastest Method RIGHT NOW:

1. **Start emulator** (if not running)
2. **Run this command:**
```bash
adb push /path/to/your/file.eps /sdcard/Download/yourfile.eps
```
3. **Open your app**
4. **Tap Convert → Select EPS File → Downloads → Pick the file**

That's it! Your file is now accessible in the app.

---

## Need Sample EPS Files?

If you don't have EPS files to test, I can help you:
1. Create simple test EPS files (using the script above)
2. Download from public EPS repositories
3. Convert existing images to EPS format

Just let me know what you need!

