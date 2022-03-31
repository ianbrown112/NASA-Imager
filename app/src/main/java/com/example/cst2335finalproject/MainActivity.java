package com.example.cst2335finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Button button;
    Calendar calendar;
    ImageView displayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
        String NASAurl = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=2020-02-12";
        setContentView(R.layout.activity_main);
        String defaultDate = DateFormat.getDateInstance().format(calendar.getTime());

        TextView dateText = findViewById(R.id.selectedDate);
        dateText.setText(defaultDate);

        ImageView dailyImage = findViewById(R.id.dailyImage);

        button=findViewById(R.id.btn1);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DialogFragment calendarFragment = new CalendarFragment();
                calendarFragment.show(getSupportFragmentManager(),"Calendar Fragment");
            }
        });

        DailyNASA_Image dailyNASA_Image = new DailyNASA_Image();
        dailyNASA_Image.execute(NASAurl);

    }

    /*Calendar code based on tutorial @ https://www.youtube.com/watch?v=33BFCdL0Di0&t=338s
     "DatePickerDialog - Android Studio Tutorial" by Coding in Flow
     Selecting date from calendar updates text box with given date
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        String selectedDate = DateFormat.getDateInstance().format(calendar.getTime());
        TextView dateText = findViewById(R.id.selectedDate);
        dateText.setText(selectedDate);
    }

    protected class DailyNASA_Image extends AsyncTask<String, String, String> {
        protected String doInBackground(String ... args) {
                try {

                    //create a URL object of what server to contact:
                    URL url = new URL(args[0]);

                    //open the connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //wait for data:
                    InputStream response = urlConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();

                    //create JSON object with result
                    JSONObject imageInfo = new JSONObject(result);

                    String imageURL = imageInfo.getString("url");

                    int lastIndex = imageURL.lastIndexOf('/') + 1;
                    String parsedFileName = imageURL.substring(lastIndex);
                    System.out.println("filename: " + parsedFileName);

                    //check if picture is already saved to drive

                    String directory = String.valueOf(getExternalFilesDir(null));
                    String filename = directory + "/" + parsedFileName;
                    File f = new File(filename);

                    //if file does not already exist in storage, download it
                    if (f.isFile() == false) {

                        System.out.println("file does not exist in storage");
                        URL sp_url = new URL(imageURL);

                        String fileName = sp_url.getFile();
//                    int lastIndex = fileName.lastIndexOf('/') + 1;
//                    String parsedFileName = fileName.substring(lastIndex);
//                    System.out.println("filename: " + parsedFileName);

                        String environment_directory = Environment.getExternalStorageDirectory().toString();
                        System.out.println("environment_directory: " + environment_directory);

                        //open the connection
                        HttpURLConnection sp_urlConnection = (HttpURLConnection) sp_url.openConnection();

                        //wait for data:

                        int count;

                        InputStream sp_bufferedInput = new BufferedInputStream(sp_url.openStream(), 8192);

                        OutputStream sp_bufferedOutput = new FileOutputStream(directory + "/" + parsedFileName);

                        byte data[] = new byte[1024];

                        while ((count = sp_bufferedInput.read(data)) != -1) {
                            sp_bufferedOutput.write(data, 0, count);
                        }
                        // flushing output
                        sp_bufferedOutput.flush();

                        // closing streams
                        sp_bufferedOutput.close();
                        sp_bufferedInput.close();
                    } else {
                        System.out.println("file does exist in storage");
                    }
                    //publishProgress(parsedFileName);
                    return parsedFileName;
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(e.getStackTrace());
                }
             return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //update progress bar if input is int
            try {
                System.out.println("In try clause of onPostExecute");
                String directory = String.valueOf(getExternalFilesDir(null));
                String path = directory + "/" + s;
                System.out.println(path);
                Bitmap bmImg = BitmapFactory.decodeFile(path);
                System.out.println("bmImg is set");
                System.out.println("Display image should be set");

            } catch (Exception e2) {
                System.out.print(e2);
            }

        }
        //overloading onProgressUpdate using try / catch technique
        //based on following stackoverflow post:
        //https://stackoverflow.com/questions/23251315/can-publishprogress-be-overloaded-in-asynctask

//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate();
//            //update progress bar if input is int
//            try {
//                String directory = String.valueOf(getExternalFilesDir(null));
//                String path = directory + "/" + values[0];
//                Bitmap bmImg = BitmapFactory.decodeFile(path);
//                displayImage.setImageBitmap(bmImg);
//                System.out.println("In try clause of onProgressUpdate");
//            } catch (Exception e2) {
//                System.out.print(e2);
//            }
//        }
    }
}