package com.example.cst2335finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.time.*;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, NavigationView.OnNavigationItemSelectedListener {

    private Button dateSelectButton;
    private ImageButton favButton;
    ArrayList<NasaImage> favNasaImages;
    Calendar calendar;
    ImageView dailyImage;
    TextView imageTitle;
    NasaImage activeImage;
    String NASAurl = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();

        setContentView(R.layout.activity_main);
        String defaultDate = DateFormat.getDateInstance().format(calendar.getTime());

        //get current year, month and day so default image is today's image
        String today = LocalDate.now().toString();

        TextView dateText = findViewById(R.id.selectedDate);
        dateText.setText(defaultDate);

        //For toolbar:
        Toolbar tBar = findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dailyImage = findViewById(R.id.dailyImage);

        imageTitle = findViewById(R.id.imageTitle);

        dateSelectButton=findViewById(R.id.btn1);

        dateSelectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DialogFragment calendarFragment = new CalendarFragment();
                calendarFragment.show(getSupportFragmentManager(),"Calendar Fragment");
            }
        });

        /*Set up favouriting function, with a button to add image to favourites
        and an array list to hold favourited images
         */
        favNasaImages = new ArrayList<NasaImage>();

        favButton = findViewById(R.id.favourite);

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Iterator favImagesIterator = favNasaImages.iterator();
                while (favImagesIterator.hasNext()) {
                    NasaImage nasaImage = (NasaImage) favImagesIterator.next();
                    System.out.println(nasaImage.getTitle());

                    //if image is already in favourites list, exit method without adding it again
                    if ( activeImage.getTitle().equals(nasaImage.getTitle()) ) {
                        return;
                    }
                }
                //otherwise, add image to favourites ArrayList
                favNasaImages.add(activeImage);
            }
        });

        DailyNASA_Image dailyNASA_Image = new DailyNASA_Image();
        dailyNASA_Image.execute(NASAurl + today);

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

        /*convert selected date into proper format for API url
        i.e YYYY-MM-DD
         */
        String API_Date = API_DateFormatter(year, month, day);

        DailyNASA_Image dailyNASA_Image = new DailyNASA_Image();
        dailyNASA_Image.execute(NASAurl + API_Date);

    }

    protected class DailyNASA_Image extends AsyncTask<String, String, NasaImage> {
        protected NasaImage doInBackground(String ... args) {
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

                    String title = imageInfo.getString("title");
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

                    NasaImage currentNasaImage = new NasaImage(title, parsedFileName);

                    return currentNasaImage;
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(e.getStackTrace());
                }
             return null;
        }

        @Override
        protected void onPostExecute(NasaImage currentImage) {
            super.onPostExecute(currentImage);

            try {
                System.out.println("In try clause of onPostExecute");
                String directory = String.valueOf(getExternalFilesDir(null));
                String path = directory + "/" + currentImage.getParsedFileName();

                currentImage.setFilePath(path);

                System.out.println(path);
                Bitmap bmImg = BitmapFactory.decodeFile(path);
                dailyImage.setImageBitmap(bmImg);

                imageTitle.setText(currentImage.getTitle());

                System.out.println("bmImg is set");
                System.out.println("Display image should be set");

                activeImage = currentImage;

            } catch (Exception e2) {
                System.out.print(e2);
            }
        }
    }
    /*convert selected date into proper format for API url
    i.e YYYY-MM-DD */
    String API_DateFormatter(int year, int month, int day) {
        //bug where month is one less than proper index e.g March is set as 2, therefore add 1 to resolve
        month = month+1;
        String API_Date = Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
        return API_Date;
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String message = null;
        message = "You clicked on your favourites list";
        System.out.println(message);
        switch(item.getItemId())
        {
            case R.id.favourite:
                System.out.println("------------you click on Favourites-----------");
                Intent intent_favs = new Intent(this, FavouritesList.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("favs", favNasaImages);
                intent_favs.putExtras(bundle);
                //message = "You clicked on your favourites list";
                startActivity(intent_favs);
                break;
            case R.id.home:
                Intent intent_home = new Intent(this, MainActivity.class);
                message = "You clicked on the home";
                startActivity(intent_home);
                break;

            case R.id.exit:
                finishAffinity();
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        Toast.makeText(this, "NavigationDrawer: " + message, Toast.LENGTH_LONG).show();
        return false;
    }
}