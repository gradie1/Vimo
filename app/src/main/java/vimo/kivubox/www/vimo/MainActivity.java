package vimo.kivubox.www.vimo;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    //Toolbar drawer
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle mToggle;

    //Navigation View
    NavigationView navigationView;

    FloatingActionButton fab;

    //FIREABASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase database ;
    private DatabaseReference ref;

    private final String USER_TAG = "MAINACTIVITY_USER";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //INITIALIZE DIFFERENT VARIABLES
        initVars();

        //initialize the widgets
        initWidgets();


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //startActivity(new Intent(MainActivity.this,SetPhoneProfil.class));

        //CHECK LOGIN STATUS
        checkUser();
    }

    public void initWidgets(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(MainActivity.this,drawer,R.string.drawer_open,R.string.drawer_close);
        drawer.addDrawerListener(mToggle);
        mToggle.syncState();

        //Enable Navigation icon
        getSupportActionBar().setElevation(2f);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //set the toolbar icon
        getSupportActionBar().setTitle("");
        //Set the layout and behavior
        //LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        fab = findViewById(R.id.addProduct);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AddImage.class));
            }
        });

    }


    //Drawer header items
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.account:{
                startActivity(new Intent(MainActivity.this,Profil.class));
                break;
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);

        //Search menu
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Toast.makeText(MainActivity.this, newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initVars(){

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        //DB
        database = FirebaseDatabase.getInstance();
        //REF
        ref = database.getReference();

    }

    //CHECK FOR USER AUTHENTIFICATION
    public void checkUser(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String uid = user.getUid();

            Log.i(USER_TAG,user.toString());

            //Check if profiling, if yes redirect
            isProfiling(user);

        }else{

            //No user logged in, redirect to login activity
            startActivity(new Intent(MainActivity.this,LogIn.class));

        }

    }

    public void isProfiling(final FirebaseUser user){

        //CHECK IF NEW USER
        //CHECK IF USER EXISTS
        ref.child("Users")
                .child(user.getUid())
                .child("status")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                    if(dataSnapshot.getValue().equals("profiling")){


                            //SEND TO PROFILING SCREEN (SetPhoneProfil.class)
                            //startActivity(new Intent(MainActivity.this,SetPhoneProfil.class));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "An error occured, Try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
