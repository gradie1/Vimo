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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

public class LogIn extends AppCompatActivity {


    Utils utils ;
    String TAG = "LOG IN ACTIVITY";

    TextInputEditText email, password;
    TextView resetPassword;
    Button login;
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

    TextView toSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_log_in);


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
        utils = new Utils(LogIn.this);


        //Initialize the widgets
        initWigets();


        //ONACTION
        try{
            SignIn();
        }catch (Exception e){
            Toast.makeText(this, "An error has occured", Toast.LENGTH_SHORT).show();
            Log.i(TAG,"Sign up Exception");
        }

    }


    public void initWigets(){

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        login = findViewById(R.id.login);
        signWithGoogle = findViewById(R.id.signWithGoogle);

        TextView GBTextView = (TextView) signWithGoogle.getChildAt(0);
        GBTextView.setText("Log in with Google");

        loginButton = findViewById(R.id.login_button);

        toSignup = findViewById(R.id.toSignup);
        toSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogIn.this,SignUp.class));
            }
        });

        resetPassword = findViewById(R.id.resetPassword);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogIn.this,ResetPassword.class));
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
                            Toast.makeText(LogIn.this, task.getException().toString(),Toast.LENGTH_SHORT).show();

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

    public void SignIn(){


        try {
            PackageInfo info = LogIn.this.getPackageManager().getPackageInfo("vimo.kivubox.www.vimo",
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



        //SIGN IN WITH EMAIL AND PASSwORD
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                utils.Loader().setMessage("Loggin in..");

                if(utils.isLoginValid(email,password)){

                    utils.Loader().show();

                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString() )
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {

                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        //Go to main activity
                                        utils.Loader().setMessage("Logged in");
                                        startActivity(new Intent(LogIn.this,MainActivity.class));


                                    } else {

                                        utils.Loader().hide();

                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LogIn.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();


                                    }

                                }
                            });

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

                            //SEND TO PROFILING SCREEN (SetPhoneProfil.class)
                            startActivity(new Intent(LogIn.this,SetPhoneProfil.class));

                        }else{

                            //OPEN MAIN ACTIVITY
                            startActivity(new Intent(LogIn.this,MainActivity.class));

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
                Toast.makeText(LogIn.this, "An error occured, Try again", Toast.LENGTH_SHORT).show();
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
                        startActivity(new Intent(LogIn.this,SetPhoneProfil.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //Hide loader
                utils.Loader().hide();

                Toast.makeText(LogIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


}
