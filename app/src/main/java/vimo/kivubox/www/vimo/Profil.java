package vimo.kivubox.www.vimo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profil extends AppCompatActivity {

    android.support.v7.widget.Toolbar toolbar ;
    AlertDialog.Builder alertDialogBuilder;

    TextView displayName, displayEmail,
    txtfname, txtlname, txtEmail, txtPhone;

    RelativeLayout fname,lname,email,phone,password;

    LinearLayout valueBlock, emailBlock, phoneBlock, passwordBlock;

    TextInputEditText newValue, newPassword, oldPassword, newEmail, emailPassword;

    FloatingActionButton fab;

    CountryCodePicker code;

    EditText telephone;

    String childToUpdate;

    Utils utils;

    CircleImageView profilPicture;

    int PICK_IMAGE_REQUEST = 1;

    Uri filePath;

    ImageSaver imageSaver;

    AlertDialog alertDialog;

    SharedPreferences sharedPreferences ;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database ;
    DatabaseReference ref;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        //PREFERENCES
        sharedPreferences = this.getSharedPreferences("vimo.kivubox.vimo.user_preferences",MODE_PRIVATE);

        //init the utility
        utils = new Utils(Profil.this);

        //INITIALIZE DIFFERENT VARIABLES
        initVars();

        //Initialize the widgets
        initWidgets();


    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();

        //display the user information
        displayUserInfo();

        //Load the profil image
        loadProfilImage();

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
                saveProfilImage(result.getUri());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    public void saveProfilImage(Uri image){



        utils.Loader().setMessage("Please wait..");
        utils.Loader().show();

        if(filePath != null){


            try{


                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);


                storageReference.child("Users")
                        .child(currentUser.getUid())
                        .child("profilImage")
                        .putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                SetInDB(taskSnapshot.getDownloadUrl().toString());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Profiling",e.getMessage());
                                utils.Loader().hide();
                                Toast.makeText(Profil.this, "PutFile Operation failed", Toast.LENGTH_SHORT).show();
                                Toast.makeText(Profil.this, "firebase : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });



            }catch (IOException e)
            {
                e.printStackTrace();
            }





        }

    }

    public void SetInDB(String path){

        ref.child("Users")
                .child(currentUser.getUid())
                .child("profilPicture")
                .setValue(path)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        try{

                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);

                            //SAVE IMAGE TO LOACAL STORAGE
                            imageSaver.setFileName("profil.png")
                                      .setDirectoryName("Vimo")
                                      .save(bitmap);

                        }catch (IOException e)
                        {
                            e.printStackTrace();
                            Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }


                        utils.Loader().hide();

                        loadProfilImage();

                        Toast.makeText(Profil.this, "Updated", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.Loader().hide();
                        Toast.makeText(Profil.this, "URL Operation failed", Toast.LENGTH_SHORT).show();
                        Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void loadProfilImage(){

        try{

            Bitmap bitmap = imageSaver.setFileName("profil.png")
                    .setDirectoryName("Vimo")
                    .load();

            profilPicture.setImageBitmap(bitmap);

        }catch (Exception e){
            Toast.makeText(this, "An error occured, Reload", Toast.LENGTH_SHORT).show();
        }

    }

    public void initVars(){

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        //DB
        database = FirebaseDatabase.getInstance();
        //REF
        ref = database.getReference();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageSaver = new ImageSaver(Profil.this);
        imageSaver.setExternal(true);

    }

    public void initWidgets(){

        //SET TOOLBAR
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //SET NAVIGATION
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init the alert dialog
        alertDialogBuilder = new AlertDialog.Builder(Profil.this);

        profilPicture = findViewById(R.id.profilImage);
        profilPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGalery();
            }
        });

        //Delete account fab
        fab = findViewById(R.id.deleteAccount);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAccount();
            }
        });


        displayName = findViewById(R.id.displayName);
        displayEmail = findViewById(R.id.displayEmail);

        View.OnClickListener change = new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                switch (view.getId()){

                    case R.id.fName:

                        childToUpdate = "firstName";

                        inflateDialog(R.layout.edit_profil_dialog);

                        break;

                    case R.id.lName:

                        childToUpdate = "lastName";

                        inflateDialog(R.layout.edit_profil_dialog);

                        break;

                    case R.id.phone:

                        childToUpdate = "phoneNumber";

                        inflateDialog(R.layout.edit_profil_dialog);

                        break;

                    case R.id.email:

                        childToUpdate = "email";

                        inflateDialog(R.layout.edit_profil_dialog);

                        break;

                    case R.id.password:

                        childToUpdate = "password";

                        inflateDialog(R.layout.edit_profil_dialog);

                        break;

                }

            }
        };

        fname = findViewById(R.id.fName);
        fname.setOnClickListener(change);
        lname = findViewById(R.id.lName);
        lname.setOnClickListener(change);
        email = findViewById(R.id.email);
        email.setOnClickListener(change);
        phone = findViewById(R.id.phone);
        phone.setOnClickListener(change);
        password = findViewById(R.id.password);
        password.setOnClickListener(change);

        txtfname = findViewById(R.id.textfName);
        txtlname = findViewById(R.id.textlName);
        txtPhone = findViewById(R.id.textPhone);
        txtEmail = findViewById(R.id.textEmail);

    }

    public void selectImageFromGalery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //initialize the user information
    public void displayUserInfo(){

        ref.child("Users").child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                          User user = dataSnapshot.getValue(User.class);

                          displayName.setText(user.getFirstName()+" "+user.getLastName());

                          displayEmail.setText(currentUser.getEmail());


                          txtfname.setText(user.getFirstName());
                          txtlname.setText(user.getLastName());
                          txtEmail.setText(currentUser.getEmail());
                          txtPhone.setText(user.getPhoneNumber());

                          if(user.getProfilPicture() == null || user.getProfilPicture().isEmpty()){
                            profilPicture.setImageResource(R.drawable.avatar_placeholder);
                          }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(Profil.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteAccount(){

        utils.Loader().setMessage("Suspending");

        AlertDialog.Builder builder = new AlertDialog.Builder(Profil.this);

        builder.setTitle("Suspend account");

        builder.setMessage("Do you really want to suspend your account?");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("Suspend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                utils.Loader().show();

                ref.child("Users")
                        .child(currentUser.getUid())
                        .child("status").setValue("suspended")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mAuth.signOut();
                                startActivity(new Intent(Profil.this, LogIn.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.Loader().hide();
                    }
                });
            }
        });

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.setCancelable(false);
        alert.show();

    }

    //set the profil update input dialog
    public void inflateDialog(int layout){


        LayoutInflater layoutInflater = LayoutInflater.from(Profil.this);
        View view = layoutInflater.inflate(layout,null);
        alertDialogBuilder.setView(view);


        //new value dialog edit text
        newValue = view.findViewById(R.id.newValue);

        valueBlock = view.findViewById(R.id.valueBlock);
        emailBlock = view.findViewById(R.id.emailBlock);
        phoneBlock = view.findViewById(R.id.phoneBlock);
        passwordBlock = view.findViewById(R.id.passwordBlock);

        newEmail = view.findViewById(R.id.newEmail);
        emailPassword = view.findViewById(R.id.emailPassword);

        oldPassword = view.findViewById(R.id.oldPassword);
        newPassword = view.findViewById(R.id.newPassword);

        code = view.findViewById(R.id.code);
        telephone = view.findViewById(R.id.telephone);

        swapDialogViews();

        newValue.setHint(childToUpdate);

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                if(childToUpdate.equals("firstName") || childToUpdate.equals("lastName")){
                    updateChild(childToUpdate,newValue.getText().toString());
                }else if(childToUpdate.equals("phoneNumber")){
                    updateChild(childToUpdate,code.getSelectedCountryCodeWithPlus()+telephone.getText().toString());
                }else if(childToUpdate.equals("email")){
                    changeEmail(newEmail.getText().toString(),emailPassword.getText().toString());
                }
                else{
                   changePassword(oldPassword.getText().toString(),newPassword.getText().toString());
                }



            }
        }).setTitle("Modify");

        alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    //chooses what to show on the dialog
    public void swapDialogViews(){

        if(childToUpdate.equals("firstName") || childToUpdate.equals("lastName")){

            phoneBlock.setVisibility(View.GONE);
            passwordBlock.setVisibility(View.GONE);
            valueBlock.setVisibility(View.VISIBLE);
            emailBlock.setVisibility(View.GONE);

        }else if(childToUpdate.equals("phoneNumber")){

            phoneBlock.setVisibility(View.VISIBLE);
            passwordBlock.setVisibility(View.GONE);
            valueBlock.setVisibility(View.GONE);
            emailBlock.setVisibility(View.GONE);

        }else if(childToUpdate.equals("email")){

            phoneBlock.setVisibility(View.GONE);
            passwordBlock.setVisibility(View.GONE);
            valueBlock.setVisibility(View.GONE);
            emailBlock.setVisibility(View.VISIBLE);

        }
        else{

            phoneBlock.setVisibility(View.GONE);
            passwordBlock.setVisibility(View.VISIBLE);
            valueBlock.setVisibility(View.GONE);
            emailBlock.setVisibility(View.GONE);

        }

    }

    //get and validate the value to update
    public void updateChild(String child, String value){

        //show loader
        utils.Loader().setMessage("Updating..");

        if(childToUpdate != null) {


            //if is name or fname
            if (child.equals("firstName") || child.equals("lastName")) {

                if (value.isEmpty()) {
                    newValue.setError(getString(R.string.not_empty));
                    return;
                }

            }

            if (child.equals("phoneNumber")) {

                if (value.isEmpty()) {
                    telephone.setError("Empty Field");
                    return;
                }

            }


            //Update value in db

            utils.Loader().show();

            utils.saveInFirebaseDb(ref.
                    child("Users").
                    child(currentUser.getUid()), child, value);


        }

    }

    public void changeEmail(final String e, String p){

        //show loader
        utils.Loader().setMessage("Updating..");

        if(e.isEmpty()){
            newEmail.setError(getString(R.string.not_empty));
            return;
        }
        else if(!e.matches("^[A-Za-z0-9.-_]+@[A-Za-z0-9.-_]+\\.[A-Za-z]{2,4}$")){
            newEmail.setError(getString(R.string.right_email));
            return;
        }

        if(p.isEmpty()){
            emailPassword.setError(getString(R.string.not_empty));
            return;
        }

        utils.Loader().show();
        alertDialog.hide();

        AuthCredential credential = EmailAuthProvider.getCredential(e,p);

        currentUser.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        currentUser.updateEmail(e).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Profil.this, "Updated", Toast.LENGTH_SHORT).show();
                                utils.Loader().hide();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                utils.Loader().hide();
                                Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.Loader().hide();
                Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void changePassword(String p, final String p1){

        utils.Loader().setMessage("Updating..");

        //password
        if(p.isEmpty()){
            oldPassword.setError(Profil.this.getString(R.string.not_empty));
            return;
        }
        if(p.length()<6){
            oldPassword.setError("Password too short");
            return;
        }

        //passwordConfirm
        if(p1.isEmpty()){
            newPassword.setError(Profil.this.getString(R.string.not_empty));
            return ;
        }
        if(p1.length()<6){
            newPassword.setError("Password too short");
            return;
        }


        utils.Loader().show();
        alertDialog.hide();

        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(),p);

        currentUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                currentUser.updatePassword(p1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        utils.Loader().hide();
                        Toast.makeText(Profil.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        utils.Loader().hide();
                        Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                utils.Loader().hide();
                Toast.makeText(Profil.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.account_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logout){
            mAuth.signOut();
            startActivity(new Intent(Profil.this, LogIn.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
