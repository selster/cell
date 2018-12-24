#XlsToCsv

This app reads the excel file and stores it to a local database.

By Default the app will currently look for the excel files in the folder 

```
/storage/emulated/0/notif/excel
```
Before you run the app you should change the location folder.
In order to look at a different folder change the variable **excelFolderPath in MainActivity to your needs

```
/storage/emulated/0/6/notification/ ... 
```

## File Chooser Library

```
    implementation 'com.github.hedzr:android-file-chooser:1.1.11'
```
