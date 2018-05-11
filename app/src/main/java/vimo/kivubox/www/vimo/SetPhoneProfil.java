package vimo.kivubox.www.vimo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetPhoneProfil extends AppCompatActivity {

    Utils utils;

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 1;


    //WIDGETS
    CircleImageView profil;
    EditText telephone;
    CountryCodePicker code;
    Button save;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseUser user;

    //FIREABASE
    private FirebaseAuth mAuth;
    private FirebaseDatabase database ;
    private DatabaseReference ref;

    private String UID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_phone_profil);

        //Init utilities
        utils = new Utils(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAuth = FirebaseAuth.getInstance();
        //DB
        database = FirebaseDatabase.getInstance();
        //REF
        ref = database.getReference();

        //INITIALIZE THE WIDGETS
        initWidgets();


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        user = FirebaseAuth.getInstance().getCurrentUser();

    }


    public void initWidgets(){
        profil = findViewById(R.id.profil);
        telephone = findViewById(R.id.telephone);
        code = findViewById(R.id.code);
        save = findViewById(R.id.save);


        profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGalery();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(telephone.getText().toString().isEmpty()){
                    telephone.setError("This field can't be empty");
                }else{

                    utils.Loader().setMessage("Saving..");
                    utils.Loader().show();

                    ref.child("Users").child(user.getUid())
                            .child("phoneNumber")
                            .setValue(code.getSelectedCountryCodeWithPlus()+telephone.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    statusActive();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            utils.Loader().hide();
                            Toast.makeText(SetPhoneProfil.this, "(Telephone) "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }
        });

    }

    public void statusActive(){

        ref.child("Users").child(user.getUid())
                .child("status")
                .setValue("active")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadProfilImage();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
             utils.Loader().hide();
                Toast.makeText(SetPhoneProfil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void selectImageFromGalery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // handle result of pick image chooser
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            filePath = data.getData();

            //START CROP ACTIVITY
            startCropImageActivity(filePath);
        }

        //handle cropped image result
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultToImageView(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void resultToImageView(Uri croppedImage){

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), croppedImage);
            // Log.d(TAG, String.valueOf(bitmap));

            profil.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    public void uploadProfilImage(){


        if(filePath != null){

            // Get the data from an ImageView as bytes
            profil.setDrawingCacheEnabled(true);
            profil.buildDrawingCache();
            Bitmap bitmap = profil.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            utils.Loader().setMessage("Setting profil picture..");
            utils.Loader().show();

            storageReference.child("Users")
                    .child(user.getUid())
                    .child("profilImage")
                    .putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            SetInDB(taskSnapshot.getDownloadUrl());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Profiling",e.getMessage());
                            utils.Loader().hide();
                            Toast.makeText(SetPhoneProfil.this, "Operation failed", Toast.LENGTH_SHORT).show();
                            Toast.makeText(SetPhoneProfil.this, "firebase : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            startActivity(new Intent(SetPhoneProfil.this,MainActivity.class));
        }

    }


    public void SetInDB(Uri uri){

        String url = uri.getPath();
        String imageName = uri.getLastPathSegment();

        ref.child("Users")
                .child(user.getUid())
                .child("profilPicture")
                .setValue(url)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(SetPhoneProfil.this,MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.Loader().hide();
                        Toast.makeText(SetPhoneProfil.this, "Operation failed", Toast.LENGTH_SHORT).show();
                        Toast.makeText(SetPhoneProfil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
