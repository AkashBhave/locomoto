# LocoMoto
This is an Android application that has Uber-like features and is similar to a taxi service. _A user can be in one of two positions:_
* __Rider:__ You can request a ride while looking at your location (which is updated very frequently) on a map. Once you click the 'Request Ride' button, your request is stored on the online database. You also have the option to cancel your request, which will in turn remove it from the database. Any drivers in the area can see your request.
* __Driver:__ You can view available requests from other riders, which is all displayed in an [Android ListView](http://developer.android.com/guide/topics/ui/layout/listview.html), which is essentially a vertical table with clickable elements. The requests are in order from closest to farthest. Once you click on a request, you see the rider's postition relative to yours on a map. Accepting the request brings up a navigation dialog to the rider's position.

*Application Information:*
* To store all the data, the application uses a service called Cloudboost (available at [www.cloudboost.io](http://www.cloudboost.io)). The app id on the database is 'ktdffagvxbnq'. With this, alongside the client key, you can access the database using specific commands.
* The name of the package is "com.akashbhave.locomoto", which can be seen in the 'Apps' tab of the Android Settings.
* This application is compatible with Android versions 4.1 (Jelly Bean, API 16) and up, but it recommended for Android versions 5.0 (Lollipop, API 21) and up.
* You must have at all times an internet connection and a GPS connection (optional).

<br />
**The locations of the files:** 
* The Java files are located [here](https://github.com/AkashBhave/LocoMoto/tree/master/app/src/main/java/com/akashbhave/locomoto), in the 'java' directory. These files are in the .java file format. 
  * [MainActivity.java](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/java/com/akashbhave/locomoto/MainActivity.java): Reponsible for the signup/login part of the app, doesn't load if you have already saved your role on the device.
  * [YourLocation.java](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/java/com/akashbhave/locomoto/YourLocation.java): Mainly used while in the 'rider' role. Contains a Google Map and some buttons on top.
  * [ViewAvailable.java](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/java/com/akashbhave/locomoto/ViewAvailable.java): Used while in the 'driver' role, lets you see all the current requests from other riders in order of nearest to farthest from your location.
  * [ViewRiderLocation.java](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/java/com/akashbhave/locomoto/ViewRiderLocation.java): Used when looking at the rider's position relative to yours in the 'driver' role. You can also navigate to the rider's position using Google Maps, which is prompted.
* The layout files are located [here](https://github.com/AkashBhave/LocoMoto/tree/master/app/src/main/res/layout), in the 'layout' directory. These files are in the .xml file format. Here you can find the layout of the UI (User Interface).
  * [activity_main.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/activity_main.xml) and [content_main.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/content_main.xml): Responsible for the layout of the "MainActivity.java" page, or the signup/login part of the app.
  * [activity_your_location.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/activity_your_location.xml): Responsible for the layout and interface of the rider's map portion of the app.
  * [activity_view_available.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/activity_view_available.xml) and [content_view_available.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/content_view_available.xml): Responsible for the driver's ListView of the app, which also will contain the map of the locations.
  * [activity_view_rider_location.xml](https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/layout/activity_view_rider_location.xml): Responsible for the map of the driver and the rider, in the 'driver' role.
  * __NOTE:__ Some activities (like ViewAvailable) have both activity files and content files. Some activities (like YourLocation) have only the activity file, which has all the information in one file.  

<br />
**Screenshots:**   
<br/ >
<img src="https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/raw/ss1.png" width="300px" alt="Screenshot 1"/><img src="https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/raw/ss2.png" width="300px" alt="Screenshot 2"/><img src="https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/raw/ss3.png" width="300px" alt="Screenshot 3"/><img src="https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/raw/ss4.png" width="300px" alt="Screenshot 4"/><img src="https://github.com/AkashBhave/LocoMoto/blob/master/app/src/main/res/raw/ss5.png" width="300px" alt="Screenshot 5"/>
