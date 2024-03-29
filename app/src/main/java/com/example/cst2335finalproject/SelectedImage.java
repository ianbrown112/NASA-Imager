package com.example.cst2335finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class SelectedImage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    ImageView selectedImageView;
    TextView selectedImageTitle;
    TextView explanationView;
    EditText editTitleBox;
    Button changeTitleBtn;
    Button unfavouriteBtn;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);

        Toolbar tBar = findViewById(R.id.toolbar);
        setSupportActionBar(tBar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawer, tBar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        selectedImageView = findViewById(R.id.selectedImage);
        selectedImageTitle = findViewById(R.id.selectedImageTitle);
        explanationView = findViewById(R.id.explanationView);
        editTitleBox = findViewById(R.id.editTitleBox);
        changeTitleBtn = findViewById(R.id.changeTitleBtn);
        unfavouriteBtn = findViewById(R.id.unfavouriteBtn);

        Intent intent = getIntent();
        Bundle bundle =  intent.getExtras();
        favNasaImages = bundle.getParcelableArrayList("favs");

        int imageIndex = bundle.getInt("imageIndex");
        NasaImage selectedImage = favNasaImages.get(imageIndex);

        //Create button to allow user to customize title of the image
        changeTitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newTitle = String.valueOf(editTitleBox.getText());
                selectedImage.setTitle(newTitle);
                selectedImageTitle.setText(newTitle);
                updateImageTitle(selectedImage);
            }
        });

        /**Create button to allow removal of image from favourites and return to favourites list activity
        /* also removes favourite from table in database*/
        unfavouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImage(selectedImage);
                favNasaImages.remove(imageIndex);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("favs", favNasaImages);
                Intent intent_favs = new Intent(SelectedImage.this, FavouritesList.class);
                intent_favs.putExtras(bundle);
                startActivity(intent_favs);
                Toast.makeText(SelectedImage.this, "Image removed from favourites list", Toast.LENGTH_LONG).show();
            }

        });



        /**Load all info from selected image into this activity*/
        try {
            String directory = String.valueOf(getExternalFilesDir(null));
            String path = directory + "/" + selectedImage.getParsedFileName();
            selectedImage.setFilePath(path);
            Bitmap bmImg = BitmapFactory.decodeFile(path);
            selectedImageView.setImageBitmap(bmImg);
            selectedImageTitle.setText(selectedImage.getTitle());
            explanationView.setText(selectedImage.getExplanation());
        } catch (Exception e2) {
            System.out.print(e2);
        }
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_menu, menu);

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

    /**method to delete image from database*/
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

    /**method to update image title in database*/
    protected void updateImageTitle(NasaImage image)
    {
        DB_Opener dbOpener = new DB_Opener(this);
        db = dbOpener.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TITLE", image.getTitle());
        try {
            db.update(DB_Opener.TABLE_NAME, cv, DB_Opener.COL_ID + "= ?", new String[]{Long.toString(image.getId())});
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.help:
                new android.app.AlertDialog.Builder(this).setTitle(getResources().getString(R.string.help))
                        .setMessage(R.string.selected_help).setPositiveButton(android.R.string.ok, null).show();
                break;
        }
        return true;
    }
}