package com.example.cst2335finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);

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

        selectedImageView = findViewById(R.id.selectedImage);

        selectedImageTitle = findViewById(R.id.selectedImageTitle);

        Intent intent = getIntent();

        Bundle bundle =  intent.getExtras();

        favNasaImages = bundle.getParcelableArrayList("favs");

        NasaImage selectedImage = bundle.getParcelable("selectedImage");
        System.out.println(selectedImage.getTitle());

        try {
            String directory = String.valueOf(getExternalFilesDir(null));
            String path = directory + "/" + selectedImage.getParsedFileName();

            selectedImage.setFilePath(path);

            System.out.println(path);
            Bitmap bmImg = BitmapFactory.decodeFile(path);
            selectedImageView.setImageBitmap(bmImg);

            selectedImageTitle.setText(selectedImage.getTitle());

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
}