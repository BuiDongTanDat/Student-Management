# README.txt
# Group: GK09

## Project
Student Management

## Description
    A simple Android application for managing student information, including functionalities for image handling, 
    user roles, and importing/exporting CSV files.

## Table of Contents
1. Prerequisites
2. Build Informations
3. Pre-existing Account Credentials
4. Import and Export Sample Files
5. Clone the Repository
6. Additional Notes
7. Demo Video Link

1. Prerequisites
   - Ensure you have [Java SDK version] installed.
   - Set your SDK location correctly (if need):
        Go to: File --> Project Structure --> SDK Location
   - Please check that you are running project in suitable device which adapted all below build gradle.
   - Put two files for import demo in somewhere that app can access to your folder like 'documents'.
   - You should run app in DEBUG MODE.

2. Build infomations:
   - minSdk = 24
   - targetSdk = 34
   - gradle version = 8.9
   - Using addition libraries:
        + To Fill imageView: 
            implementation(libs.glide)
            implementation ("com.squareup.picasso:picasso:2.8")
            ! You can check for more detail in: Gradle Scripts/build.gradle.kts (Module:app)
        + To Store images: 
            implementation(libs.cloudinary.android)
            ! You can check for more detail in: app/java/com.example.gk09/ConfigCloudinary.java
        + Import/Export csv files:
            implementation("com.opencsv:opencsv:5.5")


3. Pre-existing Account Credentials:
    Using these credentials to login to the system:
    - Admin: 
        email: admin@gmail.com
        password: 123456
    - Manager: 
        email: manager@gmail.com
        password: 123456
    - Employee: 
        email:employee@gmail.com
        password: 123456

4. Import, export file sample:
    ! Please check in project that we have 2 files for import Demo.
    - Import file: students_import.csv and certificates_import.csv
    - We recommend you to put those file in 'documents' folder in Android
    - All exported files will be saved in: storage/emulated/0/Android/data/com.example.gk09/files/exports/

5. Clone the Repository:
    If you have any  issues with the current project, you can clone it from github by using the following command:
    - Choose branch `master`:
            git clone https://github.com/BuiDongTanDat/Student-Management.git
    - Import, export files also pushed on this repository.
    

6. Additional Notes
    - After login, the account information is saved in SharedPreferences, so you won't need to log in again when restarting the app. 
    However, you can log out by: Clicking on the avatar image -> selecting LOGOUT --> Choose YES to confirm.
    - While running the project, please enable all necessary permissions that the app requests on your device.
    - Testing: The project has been tested on an Android Virtual Device (AVD) configured as a Medium Phone API 32 (Android 12L "Sv2", x86_64).
    - If you are testing in pair device, please run it as DEBUG MODE, then the data folder(com.example.gk09) can be created in your device.

7. Demo Video Link:
    Youtube: https://youtu.be/VLjsPWus5sM
    Google Drive: https://drive.google.com/file/d/1g2ym2Vs5tRiEKrfgVO55GlNh4jTifiGx/view?usp=drive_link
