// 52 hsd the path for where the excel file is
package com.xlstocsv;
// importing packages
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.xlstocsv.utils.DatabaseHandler;
import com.xlstocsv.utils.Utils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


// what the android is going to start with
public class MainActivity extends AppCompatActivity {
    // defining variables
    private static final int PICKFILE_REQUEST_CODE = 1; //
    private Button pickFile; // here is the famous button
    private ArrayList<String> columnNames = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();
    Context context;
    private final String TAG = this.getClass().getName();
    private int counter = 0;
    public static String PACKAGE_NAME;
    //Path that will open the file picker from. If it doesnt exist, it will crash
    private String excelFolderPath = "//storage//emulated//0//Notification History Log//Excel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        initwidgets();
        /// you can insert a differen package sql manager here
        // if I own the sqlmanager app
        // listener to receive db
        // then you send the db from your app to sqlmanager app

        // the following wi9ll lauch the sql manager gui
        //Intent launchIntent = getPackageManager().getLaunchIntentForPackage("dk.andsen.asqlitemanager");
        //if (launchIntent != null) {
        //    startActivity(launchIntent);//null pointer check in case package name was not found
        //}
    }

    private void initwidgets() {
        context = this;
        pickFile = (Button) findViewById(R.id.pickFile_Button);

        pickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageURI = data.getData(); // a uri is a path
                String filepath = "";
                filepath = Utils.getPath(this, selectedImageURI);

                Log.d("Test", "Excel File :" + filepath);
                try {
                    Log.d("onActivityResult: ", filepath);
                    readFile(filepath); // here we try to read filepath
                                        // this method is below
                                        // private void readFile(
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileUri(Intent data) {
        String filePath = null;
        Uri _uri = data.getData();
        Log.d("","URI = "+ _uri);
        if (_uri != null && "content".equals(_uri.getScheme())) {
            Cursor cursor = this.getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            filePath = cursor.getString(0);
            cursor.close();
        } else {
            filePath = _uri.getPath();
        }
        return filePath;
       // Log.d("","Chosen path = "+ filePath);
    }
    private void requestPermission() {
        Dexter.withActivity(this) // dexter has to do with permission
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            new ChooserDialog().with(MainActivity.this)
                                    .withFilter(false, false, "xls")
                                    .withStartFile(excelFolderPath)
                                    .withChosenListener(new ChooserDialog.Result() {
                                        @Override
                                        public void onChoosePath(String path, File pathFile) {
                                            try {
                                                readFile(pathFile.getPath());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidFormatException e) {
                                                e.printStackTrace();
                                            }
                                            Toast.makeText(MainActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .build()
                                    .show();


                            //Old Code

//                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                            Uri uri = Uri.parse(excelFolderPath);
//                            Log.d("Test", "Setting File Picker to : " +  uri.toString());
//                            intent.setDataAndType(uri, "application/vnd.ms-excel");
//                            intent.addCategory(Intent.CATEGORY_OPENABLE);
//                            startActivityForResult(Intent.createChooser(intent,
//                                    "Choose XLS File"), PICKFILE_REQUEST_CODE);
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
//                            showPermissionsAlert();
                        }
                    }

// /storage/emulated/0/Notification History Log/excel/
// /storage/emulated/0/NotificationHistory/0/6/log/excel/
// /storage/emulated/0/NotificationHistory/0/log/excel/
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    } // dexter permission complete here

    private void readFile(String filePath) throws IOException, InvalidFormatException {
        // this is where we read a file in filepath variable

        // Creating a Workbook object from an Excel file (.xls or .xlsx)
        // and the owrkbook is called filepath
        Workbook workbook = WorkbookFactory.create(new File(filePath));

        // 2. Or you can use a for-each loop, there might be > 1 sheet
        // System.out.println("Retrieving Sheets using for-each loop");
        for (Sheet sheet : workbook) {
            System.out.println("=> " + sheet.getSheetName());
        }

        // Getting the Sheet at index zero, get tghe 1st sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        // 2. Or you can use a for-each loop to iterate over the rows and columns
        // System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
        columnNames.clear();
        for (Row row : sheet) {
            System.out.print(row.getRowNum() + "\t");
            for (Cell cell : row) {
                String cellValue = dataFormatter.formatCellValue(cell);

                if (row.getRowNum() == 0) {
                    String colName = cellValue; //NOTIFICATION TITLe
                    cellValue = cellValue.replaceAll(" ", "_");
                    Log.d("ColumnName", cellValue);

                    columnNames.add(cellValue);
                } else {
                    values.add(cellValue);
                }
                System.out.print(cellValue + "\t");
            }
            System.out.println();
        }
        // createTable will make an sqlite database/table in DatabaseHandler
        // this will make a table?
        DatabaseHandler.getInstance(context).createTable(columnNames);
        // this will add both field names and append values
        DatabaseHandler.getInstance(context).addContact(columnNames, values);
        //
        DatabaseHandler.getInstance(context).getAllContacts();
        // now the excel is closed
        workbook.close();
    }
}
