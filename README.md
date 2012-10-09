BT4Android
==========

Android app for finding times of the Blacksburg Transit System

Play Store Link: https://play.google.com/store/apps/details?id=com.love.apps.BT4U
Set Up
======

This app relies on the ActionBarSherlock Library to use the newer Android 
style guides for Android versions lower than 3.0.

I use Eclipse, so that is how I set this up. If you want to set it up 
differently that is up to you, but please don't attempt to push changes to 
ActionBarSherlock. 

1) abs library folder must be located immediately next to the bt4android folder (like they currently are in the github), and symbolic links will not work. This is due to eclipse + ADT bug (see [here][1]. The best approach here is to just leave them alone after a checkout, and create a new eclipse workspace right on top of the git repository. E.g.
$ git clone git@github.com:NateLove/BT4Android-trunk.git BT4Android

Then Create a workspace in the BT4Android folder.

2) A bug in eclipse prevents you from importing existing Android projects when they are in a situation like is required for (1). See [here][2] for details, but the basic idea is that you have to move the projects, import them to eclipse (to get their information into the .metadata folder), then delete them without removing the contents on disk, then move them back, then import existing projects into workspace and you can select them. Annoying, but here's the whole process (all $ are done in the shell, all # are done in eclipse)

-$ git clone ... BT4Android

-# Open eclipse, create a new workspace on top of BT4Android

-$ cd BT4Android

-$ mv ActionBarSherlockLibrary ~/Desktop

-# Select File-> Import --> Existing Android Code into Workspace

-# Select ~/Desktop/ActionBarSherlockLibrary for import

-# Right click on abs library-->properties: Ensure that Properties-->Android lists at least API 15 and that 'is Library' is checked

-# Ensure Properties-->Java Compiler has compiler compliance level set to 1.6

-# Delete the imported project, but leave contents on disk

-$ mv ~/ActionBarSherlockLibrary .

-# File --> Import --> Existing Projects into Workspace --> Browse to BT4Android and select ActionBarSherlock

Repeat the process (the moving / importing part) for BT4Android

Finally, start hacking....

Any questions, just let me know. ngl9789@gmail.com

License
=======

    Copyright 2012 Nathan Love

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: http://stackoverflow.com/questions/5167273/in-eclipse-unable-to-reference-an-android-library-project-in-another-android-pr%5D%20for%20more
[2]: http://stackoverflow.com/questions/4054216/opening-existing-project-from-source-control
