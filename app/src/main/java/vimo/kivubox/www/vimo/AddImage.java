package vimo.kivubox.www.vimo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class AddImage extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar ;

    List<Bitmap> images;


    int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        //INITIALIZE THE WIDGETS
        initWidgets();

        //SELECT THE IMAGE
        selectImageFromGalery();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            Uri filePath = data.getData();

            addImage(filePath);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initWidgets(){

        //SET TOOLBAR
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //SET NAVIGATION
        getSupportActionBar().setTitle("Add images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //start activity with the choosen image
    public void addImage(Uri uri){

        try{

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);


        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    //SELECT IMAGE FROM GALERY
    public void selectImageFromGalery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
