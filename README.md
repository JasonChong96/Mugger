# Mugger

![Mugger](https://i.imgur.com/aecirYh.png)

## Introduction

Mugger is an Android app-based platform that allows its users to quickly scan through the study listings available, indicate their attendance for those that they are interested in, as well as create their own listings for others to join and delete them once they are done.

It was designed with the intention to increase the number of face-to-face opportunities to socialise and make useful connections by studying together for a common module.

## Quick Start

1. Ensure you have a phone running Android 4.4 (KitKat) or later.
2. Go to [https://goo.gl/Z5f3cC](https://www.google.com/url?q=https://goo.gl/Z5f3cC&sa=D&ust=1530187014671000) and install the Mugger application.
3. Open Mugger from your android device's launcher.
4. Tap the login button to log in with your Google account. After doing so, it should redirect you to a second login page for your NUSNET account (only for first-time users).

3. Features

The UI for these features is shown in our video, along with a brief reiteration of our project aims and scope. Most of the UI has been adjusted to conform to material design guidelines to provide a better user experience.

## Features

### 3.0 Login

Primary login method will be using your Google account, although first-time users will be prompted to login a second time with their NUSNET (IVLE) account, to prove that they are a current NUS student/staff and to fetch necessary information like current modules.

There will be an in-app explanation to inform the user the reason for logging in to IVLE and the data that we collect and the hashing (SHA-256) we use to keep their actual NUSNET ID unretrievable from our data. Google login was implemented as the main method due to the fact that it allows for the login information to be remembered, thus users do not need to re-enter it every time they log out of the app, unlike IVLE login which does not have that user-friendly feature.

![Main Navigation Drawer](https://i.imgur.com/gUC7owE.png)
### 3.1 Listings

Listings are used to facilitate the organising of face-to-face study sessions and/or consultations among all users, including Teaching Assistants (TAs) and professors.

##### 3.1.1 Available Listings By Module

Shows all the available study session listings created by users, including yourself. Listings created by yourself will be labelled "Yours", and listings you are att ending (including your own) will be  labelled  in orange (F2845C) at the top right hand corner. T As and professors will also have their listings labelled in cya n (97D3CA) and violet (9EB3ED) respe ctively to further differentiate the types of listings available. You can also filter out specific modules, and create your own listing directly by tapping on the orange floating action button . You can view a list of people attending a session by clicking on the icon on the top right of listings.

Tapping on the 'down' arrow on a listing will expand it and give you more information about it, where you can also indicate your attendance. All listings will also have a chatroom specific to that listing, where users can interact with others attending that same listing, including the listing creator. Do note that chats serve to help users decide if they want to attend that particular listing, and are not meant to replace the end goal of a face-to-face study session.

In the chat and attendees list, you can tap on a user's display name to  be redirected to their profile (refer to 3.2  below).

##### 3.1.2 Listings I'm Joining

Only the listings that you have joined will show up here, for you to easily keep track of all your appointments.

##### 3.1.3 My Listings

Shows the listings that you have created.

##### 3.1.4 Create/Edit Listings

You can also edit and delete your listings from these pages as shown on the right. Note that when a creator deletes his/her listing, all of its information (including chat messages) will be erased, and it will also disappear from all the listings pages. There will also be a button on the bottom right of the screen to create listings. Admins are able to edit and delete any listing, including those that are not their own.

### 3.2 My Profile

Here you will find your personal information, like modules and current display names. You can also type in additional information to personalise your bio. All these will be displayed to any user that views your profile for added user experience.

### 3.3 Administrative Tools

Only admins will have this extra option in the navigation drawer to access all the reports, feedback and prof/TA requests sent by users. They can then decide to take further action, such as muting malicious users for a specific number of hours . Other profile options that are only displayed for admins include 'Make TA/Prof ' and 'Change Mugger Role ', which allows for a user to be elevated to become an admin or vice versa . This promotion or demotion of roles can only be done by a master , who oversees all admins, and the admins in turn manage the users.

### 3.4 Settings

Settings can be accessed by tapping on the three dots at the top right hand corner of any page. There, you can enable or disable different categories of notifications . Your display name can also be changed here, otherwise the default will be the one associated with your Google account. Previously, we wanted to include a geolocation option, but after taking feedback into consideration, that feature will not be implemented as most students would usually join and create listings within the same faculty or nearby.

### 3.5 Refresh Modules

This feature can also be accessed from the three dots action bar. It is used for refreshing the current modules of a user, in the event that they have changed modules or for the beginning of a new semester. Users will be required to go through IVLE login again to fetch the required data.

### 3.6 Submit Feedback

This page allows users to send feedback that is viewable only by the admins. This is a channel for users to suggest application improvements and report bugs. This is valuable for us to improve the app in terms of user experience.

### 3.7 Request Prof/TA Role

This page allows users to request to be registered as a Professor or TA for the module that they are teaching. They'll be required to type in the module code along with proof of their position. These requests can only be viewed by admins who can approve such requests and give them their respective roles(section 3.3) so that they can make specially tagged listings as mentioned in section 3.1.

### 3.8 Logout

After the user logs out, he/she will be brought back to the main login screen until the next time the app is opened again.