# Location Reminder

Location Reminder - GPS Alarm is a simple location reminder app. Working with the application is very easy. You put a marker on the map and get an audio notification when you reach your destination. For your convenience, the application is completely free of ads.

Location Reminder uses your location to alert you when you reach your destination. This is especially useful for long trips during which you may want to sleep in.
Factors such as traffic, the route you choose, etc. can affect the overall length of your trip. This is why regular time-based alarms are useless when traveling.

You can be sure that you will only be woken up when you are near your destination.

### Screenshots

![Screenshot 1](/screenshots/Screenshot1.png)
![Screenshot 2](/screenshots/Screenshot2.png)
![Screenshot 3](/screenshots/Screenshot3.png)

### Installation

To get the project running on your local machine, you need to follow these steps:


---

**Step 1: Clone the repo**

Use this to clone it to your local machine:
```bash
git clone https://github.com/cryoggen/Location-Reminder
```

---

**Step 2: Check out the ‘master’ branch**

This branch is going to let you start working with it. The command to check out a branch would be:

```bash
git checkout master
```

---

**Step 3: Get api key for google maps**

Follow the link and follow the instructions to get google api key:
[developers.google.com](https://developers.google.com/maps/documentation/android/start#get-key)
Insert the resulting key into the local.properties file, replacing the phrase YOUR_KEY_HERE with the key.

---

**Step 4: Create a Firebase project**

Before you can use Firebase in the Location Reminder app, you need to create a Firebase project to connect to the app.

1. In the [Firebase console](https://console.firebase.google.com/), click Add project, then select or enter a Project name. You can name your project anything, but try to pick a name relevant to the app you’re building.
2. Click Continue.
3. You can skip setting up Google Analytics and chose the Not Right Now option.
4. Click Create Project to finish setting up the Firebase project.

---

**Step 5: Register your app with Firebase**

Now that you have a Firebase project, you can add your Android app to it.

1. In the center of the [Firebase console's project overview page](https://console.firebase.google.com/), click the Android icon to launch the setup workflow.
2. Enter your app's [application ID](https://developer.android.com/studio/build/application-id) in the Android package name field. Make sure you enter the ID your app is using, otherwise you cannot add or modify this value after you’ve registered your app with your Firebase project.

   An application ID is sometimes referred to as a package name.
  
   Find this application ID in your module (app-level) Gradle file, usually app/build.gradle (example ID: com.yourcompany.yourproject).
  
3. Enter the debug signing certificate SHA-1. You can generate this key by entering the following command in your command line terminal
```bash
keytool -alias androiddebugkey -keystore ~/.android/debug.keystore -list -v -storepass android
```
4. Click to Register app.

---

**Step 6: Add the Firebase configuration file to your project**

Add the Firebase Android configuration file to your app:

1. Click Download google-services.json to obtain your Firebase Android config file (google-services.json).

   You can download your [Firebase Android config file](http://support.google.com/firebase/answer/7015592) again at any time.
  
   Make sure the config file is not appended with additional characters and should only be named google-services.json.
  
2. Move your config file into the module (app-level) directory of your app.

---

**Step 7: Enable Authentication Methods**

The Location Reminder app uses Firebase to authenticate users. 
Users must be allowed to sign in with their provided email address, their Google account, or anonymously.

1. Navigate to the [Firebase console](http://console.firebase.google.com/) and select your project if you are not there already.
2. Select Develop > Authentication on the left side panel.
3. Select the Sign In Method tab on the top navigation bar.
4. Click on the Email/Password row and toggle the ‘Enabled’ switch and click Save.
5. Click on the Google row, toggle the Enabled switch, enter a Project support email, and click Save.
6. 5. Click on the Anonymus, toggle the Enabled switch, and click Save.

---

#### App use libraries and technologies:

- ViewModel
- LiveData
- Data Binding
- Navigation
- Notification
- Service
- BroadcastReceiver
- Coroutines
- Firebase Authentication
- Google maps API
- Room
- Recyclerview
- Repository
- Binding adapters
- Geolocation
