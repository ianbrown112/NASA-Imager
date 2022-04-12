package com.example.cst2335finalproject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Iterator;

public class FavouritesList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private MyListAdapter myAdapter;
    ArrayList<NasaImage> favNasaImages = new ArrayList<NasaImage>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites_list);

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
        ListView myList = findViewById(R.id.ListView1);

        Intent intent = getIntent();

        Bundle favsBundle =  intent.getExtras();

        favNasaImages = favsBundle.getParcelableArrayList("favs");

        System.out.println(favNasaImages.size());

        myList.setAdapter( myAdapter = new MyListAdapter());

        myList.setOnItemLongClickListener( (p, b, pos, id) -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.unfavourite_message))

                    //what the Yes button does:
                    .setPositiveButton(getResources().getString(R.string.yes), (click, arg) -> {
                        NasaImage currentImage;
                        currentImage = myAdapter.getItem(pos);
                        favNasaImages.remove(currentImage);
                        myAdapter.notifyDataSetChanged();
                    })
                    //What the No button does:
                    .setNegativeButton(getResources().getString(R.string.no), (click, arg) -> { })

                    //Show the dialog
                    .create().show();
            return true;
        });
    }
    private class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return favNasaImages.size();
        }

        @Override
        public NasaImage getItem(int position) {
            NasaImage item = favNasaImages.get(position);
            return item;
        }

        @Override
        public long getItemId(int position) {
            return (long) position;
        }

        @Override
        public View getView(int position, View old, ViewGroup parent) {
            View newView = old;
            LayoutInflater inflater = getLayoutInflater();

            //make a new row:
            if(newView == null) {
                newView = inflater.inflate(R.layout.list_item, parent, false);

            }
            //set what the text should be for this row:
            TextView tView = newView.findViewById(R.id.itemText);
            Button selectBtn = (Button) newView.findViewById(R.id.selectBtn);
            NasaImage nasaImage;
            nasaImage = getItem(position);
            tView.setText( nasaImage.getTitle() );

            //set on click listener for selectBtn to send to next activity
            selectBtn.setOnClickListener( click-> {
                Intent selectedImage = new Intent(FavouritesList.this, SelectedImage.class);
                Bundle bundle = new Bundle();
                bundle.putInt("imageIndex", position);
                bundle.putParcelableArrayList("favs", favNasaImages);
                selectedImage.putExtras(bundle);
                //message = "You clicked on your favourites list";
                startActivity(selectedImage);
            } );
            //return it to be put in the table
            return newView;
        }
    };

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

        //Toast.makeText(this, "NavigationDrawer: " + message, Toast.LENGTH_LONG).show();
        return false;
    }
}