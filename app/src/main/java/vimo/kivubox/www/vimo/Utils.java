package vimo.kivubox.www.vimo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Grace Lungu Birindwa on 20/04/2018.
 */

public class Utils {

    Context context;

    public static ProgressDialog progressDialog;


    public Utils(Context context) {

        this.context = context;

        this.progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void saveInFirebaseDb(DatabaseReference userRef, String child, String value){


        userRef.
                child(child).
                setValue(value).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.hide();
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public ProgressDialog Loader(){
        return progressDialog;
    }

    public boolean isFieldEmpty(TextInputEditText field){
        if(field.getText().toString().isEmpty()){
            return true;
        }
        return false;
    }


    public boolean isLoginValid(TextInputEditText email, TextInputEditText password){

        //email
        if(!isFieldEmpty(email)){

            if(!email.getText().toString().matches("^[A-Za-z0-9.-_]+@[A-Za-z0-9.-_]+\\.[A-Za-z]{2,4}$")){
                email.setError(context.getString(R.string.right_email));
                return false;
            }

        }else{
            email.setError(context.getString(R.string.not_empty));
            return false;
        }

        //password
        if(isFieldEmpty(password)){
            password.setError(context.getString(R.string.not_empty));
            return false;
        }
        if(password.getText().toString().length()<6){
            password.setError("Password too short");
            return false;
        }

        return true;

    }

    public boolean isSignupValid(TextInputEditText firstName, TextInputEditText lastName, TextInputEditText email, TextInputEditText password, TextInputEditText passwordConfirm){

        //Fname
        if(!isFieldEmpty(firstName)){

            if(!firstName.getText().toString().trim().matches("[A-Za-z]{2,20}")){
                firstName.setError(context.getString(R.string.right_name_alert));
                return false;
            }

        }else{
            firstName.setError(context.getString(R.string.not_empty));
            return false;
        }


        //Lname
        if(!isFieldEmpty(lastName)){

            if(!lastName.getText().toString().trim().matches("[A-Za-z]{2,20}")){
                lastName.setError(context.getString(R.string.right_name_alert));
                return false;
            }

        }else{
            lastName.setError(context.getString(R.string.not_empty));
            return false;
        }


        //email
        if(!isFieldEmpty(email)){

            if(!email.getText().toString().matches("^[A-Za-z0-9.-_]+@[A-Za-z0-9.-_]+\\.[A-Za-z]{2,4}$")){
                email.setError(context.getString(R.string.right_email));
                return false;
            }

        }else{
            email.setError(context.getString(R.string.not_empty));
            return false;
        }

        //password
        if(isFieldEmpty(password)){
            password.setError(context.getString(R.string.not_empty));
            return false;
        }
        if(password.getText().toString().length()<6){
            password.setError("Password too short");
            return false;
        }

        //passwordConfirm
        if(isFieldEmpty(passwordConfirm)){
            passwordConfirm.setError(context.getString(R.string.not_empty));
            return false;
        }

        if(!password.getText().toString().equals(passwordConfirm.getText().toString())){
            passwordConfirm.setError(context.getString(R.string.password_no_match));
            return false;
        }

        return true;

    }

}
