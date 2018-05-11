package vimo.kivubox.www.vimo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUp extends AppCompatActivity {

    Utils utils ;
    String TAG = "SIGN UP ACTIVITY";

    TextInputEditText firstName, lastName, email, password, passwordConfirm;
    TextView toLogin;
    Button signup;
    CardView signWithFacebook;
    SignInButton signWithGoogle;
    LoginButton loginButton;

    private FirebaseAuth mAuth;
    FirebaseDatabase database ;
    DatabaseReference ref;

    GoogleApiClient mGoogleApiClient;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_sign_up);


        mCallbackManager = CallbackManager.Factory.create();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        //DB
        database = FirebaseDatabase.getInstance();
        //REF
        ref = database.getReference();

        //INITIALIZING THE UTIL CLASS
        utils = new Utils(SignUp.this);

        //INITIALIZE THE WIDGETS
        initWidgets();

        //ONACTION
        try{
            Signup();
        }catch (Exception e){
            Toast.makeText(this, "An error has occured", Toast.LENGTH_SHORT).show();
            Log.i(TAG,"Sign up Exception");
        }

    }

    public void initWidgets(){

        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        passwordConfirm = findViewById(R.id.passwordConfirm);

        signup = findViewById(R.id.signup);
        signWithGoogle = findViewById(R.id.signWithGoogle);

        TextView GBTextView = (TextView) signWithGoogle.getChildAt(0);
        GBTextView.setText("Log in with Google");

        loginButton = findViewById(R.id.login_button);

        toLogin = findViewById(R.id.toLogin);
        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, LogIn.class));
            }
        });

    }


    //GOOGLE SIGN UP
    // Configure Google Sign In
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("592341159258-9bq69ihb5m5ah1vqkb8tcchjp5q8pdni.apps.googleusercontent.com")
            .requestEmail()
            .build();

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);

                utils.Loader().hide();

            }
        }


        //FACEBOOK ACTIVITY RESULT
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);


    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //create the user by setting information in the database then open the next activity
                            createUser(user,user.getDisplayName(),"");

                            utils.Loader().setMessage("Logging in");


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignUp.this, task.getException().toString(),Toast.LENGTH_SHORT).show();

                            utils.Loader().hide();

                        }

                        // ...
                    }
                });
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //create the user by setting information in the database then open the next activity
                            createUser(user,acct.getGivenName(),acct.getFamilyName());


                        } else {
                            // If sign in fails, display a message to the user.
                            utils.Loader().hide();


                        }

                        // ...
                    }
                });
    }




    //  SIGN
    public void Signup(){


        try {
            PackageInfo info = SignUp.this.getPackageManager().getPackageInfo("vimo.kivubox.www.vimo",
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                String sign= Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash:", sign);
                //  Toast.makeText(getApplicationContext(),sign,     Toast.LENGTH_LONG).show();
            }
            Log.d("KeyHash:", "****------------***");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //SIGN WITH FACEBOOK
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                utils.Loader().setMessage("Signing with facebook..");
                utils.Loader().show();

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
                utils.Loader().hide();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
                utils.Loader().hide();
            }
        });





        //SIGN WITH GOOGLE
        signWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Set Dialog message
                utils.Loader().setMessage("Signing with google..");
                utils.Loader().show();

                signInWithGoogle();
            }
        });



        //SIGN UP WITH EMAIL AND PASSWORD
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Set Dialog message
                utils.Loader().setMessage("Please wait..");


                if(utils.isSignupValid(firstName,lastName,email,password,passwordConfirm)){

                    //Show progress dialog
                    utils.Loader().show();

                    mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){

                                        //Display message
                                        utils.Loader().setMessage("Successfully authentificated");

                                        FirebaseUser user = mAuth.getCurrentUser();

                                        //open next activity
                                        createUser(user,firstName.getText().toString(),lastName.getText().toString());

                                    }else{

                                        //Hide loader
                                        utils.Loader().hide();

                                        Toast.makeText(SignUp.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //Hide loader
                            utils.Loader().hide();

                            Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });


                }else{
                    //If the form is not validated
                }

            }
        });

    }

    public void createUser(final FirebaseUser user,String fName,String lName){

        //NEW USER
        final User newUser = new User(fName,lName,"standard","profiling");

        //CHECK IF NEW USER
        //CHECK IF USER EXISTS
        ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot id: dataSnapshot.getChildren()){

                    if(id.getKey().equals(user.getUid())){

                        //IS NOT A NEW USER

                        //CHECKS STATUS
                        if(id.child("status").toString().equals("profiling")){

                            //SEND TO PROFILING SCREEN
                            startActivity(new Intent(SignUp.this,SetPhoneProfil.class));

                        }else{

                            //OPEN MAIN ACTIVITY
                            startActivity(new Intent(SignUp.this,MainActivity.class));

                        }
                    }else{

                        //IF NEW USER

                        //CREATE USER
                        setDataInDB( user, newUser);

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUp.this, "An error occured, Try again", Toast.LENGTH_SHORT).show();
            }
        });



    }


    public void setDataInDB(FirebaseUser user, User newUser){

        //SET DATA IN DB
        ref.child("Users")
                .child(user.getUid())
                .setValue(newUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        utils.Loader().setMessage("Logging in..");

                        //SEND TO PROFILING SCREEN
                        startActivity(new Intent(SignUp.this,SetPhoneProfil.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //Hide loader
                utils.Loader().hide();

                Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


}
