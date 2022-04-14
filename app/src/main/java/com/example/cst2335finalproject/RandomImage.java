package com.example.cst2335finalproject;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomImage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageButton favButton;
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    ImageView randomImageView;
    TextView imageTitleView;
    NasaImage activeImage;
    ProgressBar progressBar;
    boolean isRunning;
    SQLiteDatabase db;
    String NASAurl = "https://api.nasa.gov/planetary/apod?api_key=DgPLcIlnmN0Cwrzcg3e9NraFaYLIDI68Ysc6Zh3d&date=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** isRunning boolean required to stop while loop from running once
         * we leave activity
         */
        isRunning = true;
        loadDataFromDatabase(false);

        setContentView(R.layout.activity_random_image);
        Toolbar tBar = findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        randomImageView = findViewById(R.id.randomImage);

        imageTitleView = findViewById(R.id.randomImageTitle);

        progressBar = findViewById(R.id.progressBar);
        /**Set up favouriting function, with a button to add image to favourites
        and an array list to hold favourited images
         */

        try {
            Intent intent = getIntent();

            Bundle bundle = intent.getExtras();

            favNasaImages = bundle.getParcelableArrayList("favs");
        } catch (Exception e) {
            System.out.println(e);
        }

        favButton = findViewById(R.id.favouriteRandom);

        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Iterator favImagesIterator = favNasaImages.iterator();
                while (favImagesIterator.hasNext()) {
                    NasaImage nasaImage = (NasaImage) favImagesIterator.next();

                    if ( activeImage.getPublishedDate().equals(nasaImage.getPublishedDate()) ) {
                        return;
                    }
                }
                ContentValues newRowValues = new ContentValues();

                newRowValues.put(DB_Opener.COL_FILENAME, activeImage.getParsedFileName());
                newRowValues.put(DB_Opener.COL_TITLE, activeImage.getTitle());
                newRowValues.put(DB_Opener.COL_PUBLISHED_DATE, activeImage.getPublishedDate());
                newRowValues.put(DB_Opener.COL_EXPLANATION, activeImage.getExplanation());
                newRowValues.put(DB_Opener.COL_FILEPATH, activeImage.getFilePath());

                db.insert(DB_Opener.TABLE_NAME, null, newRowValues);

                /**reload from database to get auto-generated ID for new favourited image*/
                loadDataFromDatabase(true);
            }
        });

        RandomNasaImage RandomNASA_Image = new RandomNasaImage();
        RandomNASA_Image.execute(NASAurl);
    }

    protected class RandomNasaImage extends AsyncTask<String, String, String> {
        protected String doInBackground(String ... args) {
            while ( isRunning ) {
                try {
                    String randomUrl = args[0] + randomDateGenerator();
                    URL url = new URL(randomUrl);

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream response = urlConnection.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response, "UTF-8"), 8);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();

                    JSONObject imageInfo = new JSONObject(result);

                    String title = imageInfo.getString("title");
                    String imageURL = imageInfo.getString("url");
                    String publishedDate = imageInfo.getString("date");

                    String explanation = imageInfo.getString("explanation");

                    int lastIndex = imageURL.lastIndexOf('/') + 1;
                    String parsedFileName = imageURL.substring(lastIndex);

                    /** check if picture is already saved to drive */
                    String directory = String.valueOf(getExternalFilesDir(null));
                    String filename = directory + "/" + parsedFileName;
                    File f = new File(filename);

                    if (f.isFile() == false) {

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
                    for (int i = 0; i < 100; i++) {
                        try {
                            String str_i = String.valueOf(i);
                            publishProgress(str_i);
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    /**update ImageView to show new NasaImage*/
                    NasaImage currentNasaImage = new NasaImage(title, parsedFileName, publishedDate, explanation);
                    String path = directory + "/" + parsedFileName;
                    currentNasaImage.setFilePath(path);
                    publishProgress(currentNasaImage.getFilePath());

                    activeImage = currentNasaImage;
                    progressBar.setProgress(0);
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println(e.getStackTrace());
                }
            }
            return null;
        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate();
            /**update progress bar if input is int*/
            try {
                int i = Integer.parseInt(values[0]);
                progressBar.setProgress(i);
                /**load new NASA picture if string*/
            } catch (NumberFormatException e) {
                try {
                    String path = values[0];
                    Bitmap bmImg = BitmapFactory.decodeFile(path);
                    randomImageView.setImageBitmap(bmImg);
                    imageTitleView.setText(activeImage.getTitle());
                } catch (Exception e2) {
                    System.out.print(e2);
                }
            }
        }
    }

    /** return a date between Jun 16, 1995 and today
    * generating random date based on this post from StackOverflow:
    * https://stackoverflow.com/questions/40253332/generating-random-date-in-a-specific-range-in-java
    */

    String randomDateGenerator() {
        Random rand = new Random();
        LocalDate d1 = LocalDate.of(1995, 6, 16);
        LocalDate d2 = LocalDate.now();
        int days = (int) Duration.between(d1.atStartOfDay(), d2.atStartOfDay()).toDays();
        LocalDate randomDate = d1.plusDays(
                rand.nextInt(days+1));
        return randomDate.toString();
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        isRunning = false;
        String message = null;
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.help:
                android.app.AlertDialog helpAlert = new android.app.AlertDialog.Builder(this).setTitle(getResources().getString(R.string.help))
                        .setMessage(R.string.random_help).setPositiveButton(android.R.string.ok, null).show();
                break;
        }
        return true;
    }
}