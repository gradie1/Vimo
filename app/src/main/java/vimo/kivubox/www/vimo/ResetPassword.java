package vimo.kivubox.www.vimo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {

    TextView login;
    Button reset;
    TextInputEditText email;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initWidgets();

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isEmailValid()){

                    progressDialog.setMessage("Sending..");
                    progressDialog.show();

                    mAuth.sendPasswordResetEmail(email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        Toast.makeText(ResetPassword.this, "Reset link successfuly sent ", Toast.LENGTH_SHORT).show();

                                    }else{
                                        Log.d("Reset Password",task.getException().toString());
                                        Toast.makeText(ResetPassword.this, "An error occured", Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                }


            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResetPassword.this,LogIn.class));
            }
        });

    }

    public void initWidgets(){
        login = findViewById(R.id.login);
        reset = findViewById(R.id.reset);
        email = findViewById(R.id.email);
        progressDialog = new ProgressDialog(ResetPassword.this);

    }

    public boolean isEmailValid(){

        //email
        if(!isFieldEmpty(email)){

            if(!email.getText().toString().matches("^[A-Za-z0-9.-_]+@[A-Za-z0-9.-_]+\\.[A-Za-z]{2,4}$")){
                email.setError(ResetPassword.this.getString(R.string.right_email));
                return false;
            }

        }else{
            email.setError(ResetPassword.this.getString(R.string.not_empty));
            return false;
        }

        return true;

    }

    public boolean isFieldEmpty(TextInputEditText field){
        if(field.getText().toString().isEmpty()){
            return true;
        }
        return false;
    }

}
