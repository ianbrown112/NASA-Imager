package com.example.cst2335finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.android.material.snackbar.Snackbar;

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
    TextView dateText;
    NasaImage activeImage;
    String defaultDate;
    SQLiteDatabase db;
    String NASAurl = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=";
    boolean default_color;
    boolean start = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        /**default_color variable allows app to switch between
         * light and dark palettes depending on user's preference
         */
        default_color = true;

        if ( sharedPref.getBoolean("default_color", false) ) {
            default_color = false;
        }

        if ( default_color ) {
            setContentView(R.layout.activity_main_light);
        }
        else {
            setContentView(R.layout.activity_main_dark);
        }

        /**load any saved favourites from database
         */
        loadDataFromDatabase(false);

        calendar = Calendar.getInstance();

        defaultDate = DateFormat.getDateInstance().format(calendar.getTime());

        /**get current year, month and day so default image is today's image*/
        String today = LocalDate.now().toString();

        dateText = findViewById(R.id.selectedDate);
        dateText.setText(defaultDate);

        /**instantiate toolbar*/
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

        /**dateSelectButton launches calendar fragment so user can select date*/
        dateSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment calendarFragment = new CalendarFragment();
                calendarFragment.show(getSupportFragmentManager(),"Calendar Fragment");
            }
        });

        /**Set up favouriting function, with a button to add image to favourites
        and an array list to hold favourited images
         */

        /**if returning to home screen from another activity in app,
         * use bundle so favourites list persists*/
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

                String confirm_favourite = getResources().getString(R.string.confirm_favourite);
                String undo = getResources().getString(R.string.undo);
                String already_fav = getResources().getString(R.string.already_favourite);

                Iterator favImagesIterator = favNasaImages.iterator();

                /**iterate through favourites list, if image already exists in list do
                 * not add it again*/
                while (favImagesIterator.hasNext()) {
                    NasaImage nasaImage = (NasaImage) favImagesIterator.next();
                    if ( activeImage.getPublishedDate().equals(nasaImage.getPublishedDate()) ) {
                        Toast.makeText(MainActivity.this, already_fav, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                /**and write information to the database if new image*/
                ContentValues newRowValues = new ContentValues();

                newRowValues.put(DB_Opener.COL_FILENAME, activeImage.getParsedFileName());
                newRowValues.put(DB_Opener.COL_TITLE, activeImage.getTitle());
                newRowValues.put(DB_Opener.COL_PUBLISHED_DATE, activeImage.getPublishedDate());
                newRowValues.put(DB_Opener.COL_EXPLANATION, activeImage.getExplanation());
                newRowValues.put(DB_Opener.COL_FILEPATH, activeImage.getFilePath());

                db.insert(DB_Opener.TABLE_NAME, null, newRowValues);

                /**reload from database to get auto-generated ID for new favourited image*/
                loadDataFromDatabase(true);

                Snackbar.make(view, confirm_favourite, Snackbar.LENGTH_LONG)
                        .setAction(undo, new MyUndoListener())
                        .show();
            }

        });

        /**start Asynch function to load default image from web*/
        DailyNASA_Image dailyNASA_Image = new DailyNASA_Image();
        dailyNASA_Image.execute(NASAurl + today);
    }

    /**Calendar code based on tutorial @ https://www.youtube.com/watch?v=33BFCdL0Di0&t=338s
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
        /**AsyncTask connects to server, downloads image and displays it
         * modelled after Lab 6 where we loaded Cat images from server*/
        protected NasaImage doInBackground(String ... args) {
                try {

                    /**create a URL object of what server to contact*/
                    URL url = new URL(args[0]);

                    /**open the connection*/
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    /**wait for data:*/
                    InputStream response = urlConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();

                    /**create JSON object with result*/
                    JSONObject imageInfo = new JSONObject(result);

                    String title = imageInfo.getString("title");
                    String imageURL = imageInfo.getString("url");
                    String publishedDate = imageInfo.getString("date");

                    String explanation = imageInfo.getString("explanation");

                    int lastIndex = imageURL.lastIndexOf('/') + 1;
                    String parsedFileName = imageURL.substring(lastIndex);

                    /**check if picture is already saved to drive*/

                    String directory = String.valueOf(getExternalFilesDir(null));
                    String filename = directory + "/" + parsedFileName;
                    File f = new File(filename);

                    /**if file does not already exist in storage, download it*/
                    if (f.isFile() == false) {

                        System.out.println("file does not exist in storage");
                        URL sp_url = new URL(imageURL);

                        int count;

                        InputStream sp_bufferedInput = new BufferedInputStream(sp_url.openStream(), 8192);

                        OutputStream sp_bufferedOutput = new FileOutputStream(directory + "/" + parsedFileName);

                        byte data[] = new byte[1024];

                        while ((count = sp_bufferedInput.read(data)) != -1) {
                            sp_bufferedOutput.write(data, 0, count);
                        }
                        /** flushing output */
                        sp_bufferedOutput.flush();

                        /** closing streams */
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

        /** set ImageView to newly downloaded image once it completes */
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
    /** convert selected date into proper format for API url
    i.e YYYY-MM-DD */
    String API_DateFormatter(int year, int month, int day) {
        /** months are zero-indexed so january=0, add 1 to make compatible index e.g March is set as 2, therefore add 1 to resolve */
        month = month+1;
        String API_Date = Integer.toString(year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day);
        return API_Date;
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

        return true;
    }

    /** Sets up items in toolbar to allow help message or switch palette*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message = null;
        switch(item.getItemId())
        {
            case R.id.palette:
                DrawerLayout bgElement = (DrawerLayout) findViewById(R.id.drawer_layout);
                SharedPreferences sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                if ( start ) {
                    if (default_color) {
                        start = false;
                        bgElement.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));
                        imageTitleView.setTextColor(getColor(R.color.black));
                        dateText.setTextColor(getColor(R.color.black));
                        favButton.setImageDrawable(getResources().getDrawable(R.drawable.heart2));
                        message = getResources().getString(R.string.light_palette);
                        editor.putBoolean("default_color", false);
                        editor.apply();
                        default_color = false;
                    } else {
                        start = false;
                        bgElement.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
                        imageTitleView.setTextColor(getColor(R.color.white));
                        dateText.setTextColor(getColor(R.color.white));
                        favButton.setImageDrawable(getResources().getDrawable(R.drawable.red_heart));
                        message = getResources().getString(R.string.dark_palette);
                        editor.putBoolean("default_color", true);
                        editor.apply();
                        default_color = true;
                    }
                }
                if ( !start ) {
                    if (default_color) {
                        bgElement.setBackgroundColor(getResources().getColor(R.color.cardview_light_background));
                        imageTitleView.setTextColor(getColor(R.color.black));
                        dateText.setTextColor(getColor(R.color.black));
                        favButton.setImageDrawable(getResources().getDrawable(R.drawable.heart2));
                        message = getResources().getString(R.string.light_palette);
                        editor.putBoolean("default_color", false);
                        editor.apply();
                        default_color = false;
                    } else {
                        bgElement.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
                        imageTitleView.setTextColor(getColor(R.color.white));
                        dateText.setTextColor(getColor(R.color.white));
                        favButton.setImageDrawable(getResources().getDrawable(R.drawable.red_heart));
                        message = getResources().getString(R.string.dark_palette);
                        editor.putBoolean("default_color", true);
                        editor.apply();
                        default_color = true;
                    }
                }
                break;

            case R.id.help:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.help))
                        .setMessage(R.string.home_help).setPositiveButton(android.R.string.ok, null).show();
                break;


        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("favs", favNasaImages);
        switch(item.getItemId())
        {
            case R.id.favourite:
                Intent intent_favs = new Intent(this, FavouritesList.class);
                intent_favs.putExtras(bundle);
                startActivity(intent_favs);
                break;
            case R.id.home:
                Intent intent_home = new Intent(this, MainActivity.class);
                intent_home.putExtras(bundle);
                startActivity(intent_home);
                break;

            case R.id.random:
                Intent intent_random = new Intent(this, RandomImage.class);
                intent_random.putExtras(bundle);
                startActivity(intent_random);
                break;

            case R.id.exit:
                finishAffinity();
                break;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    /** allows loading of favourites from SQLite database
     * based on Lab 5 of this course
     * */
    private void loadDataFromDatabase(boolean isNew)
    {
        DB_Opener dbOpener = new DB_Opener(this);
        db = dbOpener.getWritableDatabase();

        String [] columns = {DB_Opener.COL_ID,
                DB_Opener.COL_FILENAME,
                DB_Opener.COL_TITLE,
                DB_Opener.COL_FILEPATH,
                DB_Opener.COL_PUBLISHED_DATE,
                DB_Opener.COL_EXPLANATION};

        Cursor results = db.query(false, DB_Opener.TABLE_NAME, columns, null, null, null, null, null, null);

        printCursor(results);

        int filenameColIndex = results.getColumnIndex(DB_Opener.COL_FILENAME);
        int titleColIndex = results.getColumnIndex(DB_Opener.COL_TITLE);
        int filepathColIndex = results.getColumnIndex(DB_Opener.COL_FILEPATH);
        int publishedDateColIndex = results.getColumnIndex(DB_Opener.COL_PUBLISHED_DATE);
        int explanationColIndex = results.getColumnIndex(DB_Opener.COL_EXPLANATION);
        int idColIndex = results.getColumnIndex(DB_Opener.COL_ID);

        while (results.moveToNext()) {
            String filename = results.getString(filenameColIndex);
            String title = results.getString(titleColIndex);
            String filepath = results.getString(filepathColIndex);
            String publishedDate = results.getString(publishedDateColIndex);
            String explanation = results.getString(explanationColIndex);
            long id = results.getLong(idColIndex);

            if (!isNew) {
                /** add the new image to the array list: */
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

    /** trouble shooting function to show data loaded from database in console
     * based on code from Lab 5 */
    private void printCursor(Cursor c) {
        System.out.println("-------DEBUG INFO START-------");
        System.out.println("db version: " + db.getVersion());

        int cols = c.getColumnCount();
        System.out.println("number of columns: " + cols);

        for (int i=0; i<cols; i++) {
            System.out.println("column names: " + c.getColumnName(i));
        }

        int results = c.getCount();
        System.out.println("total number of results: " + results);

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

        c.moveToPosition(-1);

        System.out.println("-------DEBUG INFO END-------");
    }

    /** function to remove NasaImage object from ArrayList used in bundle */
    public void removeFavourite(NasaImage image){
        System.out.println(image.getTitle());
        deleteImage(image);
        favNasaImages.remove(image);

        int index = 0;
        Iterator favImagesIterator = favNasaImages.iterator();
        while (favImagesIterator.hasNext()) {
            NasaImage nasaImage = (NasaImage) favImagesIterator.next();
            if ( image.getPublishedDate().equals(nasaImage.getPublishedDate())) {
                favNasaImages.remove(index);
            }
            index+=1;
        }
    }
    /** function to remove NasaImage object data from database */
    protected void deleteImage(NasaImage image)
    {
        DB_Opener dbOpener = new DB_Opener(this);
        db = dbOpener.getWritableDatabase();
        try {
            db.delete(DB_Opener.TABLE_NAME, DB_Opener.COL_ID + "= ?", new String[]{Long.toString(image.getId())});
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /** undo favourite action, applied in SnackBar */
    public class MyUndoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            removeFavourite(activeImage);
        }
    }
}