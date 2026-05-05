# Introduction
There are two types of builds.
1) /clean/* - clean jar, without seeded data for testing (without demo user)
2) /seeded/* - seeded version of application, contains demo user for fast testing.

## Demo user
### Pro User
- **email**: moises@gmail.com
- **password**: 12345678


# Execution
If you want to have clean install or install older version of the application than current, remove everything inside `%APPDATA%/Triplify/*` folder.
Each of modules contains jar executable. There are strict prerequisites:
- **JDK 25** or higher is required

For executing the program, execute run.bat or run.sh as administrator, depending on your OS:
- Windows - run.bat
- Linux or mac - run.sh

You can also directly execute .jar file with command:
Windows:
```
@echo off
java --enable-native-access=ALL-UNNAMED -jar "triplify.jar"
pause
```

Linux:
```
#!/bin/sh
java --enable-native-access=ALL-UNNAMED -jar "triplify.jar"
```