package com.example.cst2335finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    Calendar calendar;
    ImageView dailyImageView;
    TextView imageTitleView;
    NasaImage activeImage;
    String defaultDate;
    SQLiteDatabase db;
    String NASAurl = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load any saved favourites from database
        loadDataFromDatabase(false);

        calendar = Calendar.getInstance();

        setContentView(R.layout.activity_main);
        defaultDate = DateFormat.getDateInstance().format(calendar.getTime());

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

        dailyImageView = findViewById(R.id.dailyImage);

        imageTitleView = findViewById(R.id.imageTitle);

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

        try {
            Intent intent = getIntent();

            Bundle bundle = intent.getExtras();

            favNasaImages = bundle.getParcelableArrayList("favs");
        } catch (Exception e) {
            System.out.println(e);
        }

        favButton = findViewById(R.id.favourite);

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Iterator favImagesIterator = favNasaImages.iterator();
                while (favImagesIterator.hasNext()) {
                    NasaImage nasaImage = (NasaImage) favImagesIterator.next();

                    //if image is already in favourites list, exit method without adding it again
                    if ( activeImage.getPublishedDate().equals(nasaImage.getPublishedDate()) ) {
                        return;
                    }
                }
                //and write information to the database
                ContentValues newRowValues = new ContentValues();

                newRowValues.put(DB_Opener.COL_FILENAME, activeImage.getParsedFileName());
                newRowValues.put(DB_Opener.COL_TITLE, activeImage.getTitle());
                newRowValues.put(DB_Opener.COL_PUBLISHED_DATE, activeImage.getPublishedDate());
                newRowValues.put(DB_Opener.COL_EXPLANATION, activeImage.getExplanation());
                newRowValues.put(DB_Opener.COL_FILEPATH, activeImage.getFilePath());

                db.insert(DB_Opener.TABLE_NAME, null, newRowValues);

                //reload from database to get auto-generated ID for new favourited image
                loadDataFromDatabase(true);
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
                    String publishedDate = imageInfo.getString("date");

                    String explanation = imageInfo.getString("explanation");

                    int lastIndex = imageURL.lastIndexOf('/') + 1;
                    String parsedFileName = imageURL.substring(lastIndex);

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

                    NasaImage currentNasaImage = new NasaImage(title, parsedFileName, publishedDate, explanation);

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
                String directory = String.valueOf(getExternalFilesDir(null));
                String path = directory + "/" + currentImage.getParsedFileName();

                currentImage.setFilePath(path);
                Bitmap bmImg = BitmapFactory.decodeFile(path);
                
                dailyImageView.setImageBitmap(bmImg);

                imageTitleView.setText(currentImage.getTitle());

                activeImage = currentImage;

            } catch (Exception e2) {
                System.out.print(e2);
            }
        }
    }
    /*convert selected date into proper format for API url
    i.e YYYY-MM-DD */
    String API_DateFormatter(int year, int month, int day) {
        //months are zero-indexed so january=0, add 1 to make compatible index e.g March is set as 2, therefore add 1 to resolve
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
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("favs", favNasaImages);
        switch(item.getItemId())
        {
            case R.id.favourite:
                Intent intent_favs = new Intent(this, FavouritesList.class);
                intent_favs.putExtras(bundle);
                //message = "You clicked on your favourites list";
                startActivity(intent_favs);
                break;
            case R.id.home:
                Intent intent_home = new Intent(this, MainActivity.class);
                intent_home.putExtras(bundle);
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

    private void loadDataFromDatabase(boolean isNew)
    {
        //get a database connection:
        DB_Opener dbOpener = new DB_Opener(this);
        db = dbOpener.getWritableDatabase();

        // We want to get all of the columns. Look at DB_Opener.java for the definitions:
        String [] columns = {DB_Opener.COL_ID,
                DB_Opener.COL_FILENAME,
                DB_Opener.COL_TITLE,
                DB_Opener.COL_FILEPATH,
                DB_Opener.COL_PUBLISHED_DATE,
                DB_Opener.COL_EXPLANATION};
        //query all the results from the database:
        Cursor results = db.query(false, DB_Opener.TABLE_NAME, columns, null, null, null, null, null, null);

        printCursor(results);
        //Now the results object has rows of results that match the query.
        //find the column indices:
        int filenameColIndex = results.getColumnIndex(DB_Opener.COL_FILENAME);
        int titleColIndex = results.getColumnIndex(DB_Opener.COL_TITLE);
        int filepathColIndex = results.getColumnIndex(DB_Opener.COL_FILEPATH);
        int publishedDateColIndex = results.getColumnIndex(DB_Opener.COL_PUBLISHED_DATE);
        int explanationColIndex = results.getColumnIndex(DB_Opener.COL_EXPLANATION);
        int idColIndex = results.getColumnIndex(DB_Opener.COL_ID);

        //iterate over the results, return true if there is a next item:

        while (results.moveToNext()) {
            String filename = results.getString(filenameColIndex);
            String title = results.getString(titleColIndex);
            String filepath = results.getString(filepathColIndex);
            String publishedDate = results.getString(publishedDateColIndex);
            String explanation = results.getString(explanationColIndex);
            long id = results.getLong(idColIndex);

            if (!isNew) {
                //add the new image to the array list:
                NasaImage nasaImageFromDB = new NasaImage(title, filename, publishedDate, explanation);
                nasaImageFromDB.setFilePath(filepath);
                nasaImageFromDB.setId(id);
                favNasaImages.add(nasaImageFromDB);
            }
            else {
                if (results.isLast()) {
                    NasaImage nasaImageFromDB = new NasaImage(title, filename, publishedDate, explanation);
                    nasaImageFromDB.setFilePath(filepath);
                    nasaImageFromDB.setId(id);
                    favNasaImages.add(nasaImageFromDB);
                }
            }
        }
    }

    private void printCursor(Cursor c) {

        System.out.println("-------DEBUG INFO START-------");
        //print db version from static db attribute in MainActivity
        System.out.println("db version: " + db.getVersion());

        //get number of columns
        int cols = c.getColumnCount();
        System.out.println("number of columns: " + cols);

        //get names of columns
        for (int i=0; i<cols; i++) {
            System.out.println("column names: " + c.getColumnName(i));
        }

        //get number of results
        int results = c.getCount();
        System.out.println("total number of results: " + results);

        //use cursor to iterate through each row and print out the values
        c.moveToFirst();
        while(!c.isAfterLast() ){
            int id = c.getInt(0);
            String filename = c.getString(1);
            String title = c.getString(2);
            String filepath = c.getString(3);
            String publishedDate = c.getString(4);
            String explanation = c.getString(5);
            System.out.println("id: " + id + ", filename: " + filename + ", title: " + title +
                    ", filepath: " + filepath + ", publishedDate: " + publishedDate);
            c.moveToNext(); }

        //move cursor back to first row so it can be used to display within the app
        c.moveToPosition(-1);

        System.out.println("-------DEBUG INFO END-------");
    }
}