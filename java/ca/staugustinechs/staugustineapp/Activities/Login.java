package ca.staugustinechs.staugustineapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.ExecuteScriptTask;
import ca.staugustinechs.staugustineapp.R;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private int RC_SIGN_IN = 9823;
    private SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);

        this.getSupportActionBar().setTitle(R.string.app_name);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        signInButton = (SignInButton) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);
    }

    public void createSignInIntent() {
        //MAKE GOOGLE SIGN IN OPTIONS
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);
        googleClient.signOut();

        //CREATE SIGN IN WINDOW
        startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_IN);
    }

    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> googleTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (googleTask.isSuccessful()) {
                // Successfully signed in
                GoogleSignInAccount account = googleTask.getResult();
                if(account.getEmail().contains("@ycdsbk12.ca") || AppUtils.ALLOW_ACCOUNTS){
                    FirebaseAuth.getInstance()
                            .signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        finishSignIn();
                                    }else{
                                        signInButton.setEnabled(true);
                                    }
                                }
                            });
                }else{
                    signInButton.setEnabled(true);
                    Toast.makeText(this,
                            "Sorry, you aren't a member of St. A's (Be sure to use your 'ycdsbk12' account!)",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                signInButton.setEnabled(true);
                Toast.makeText(this, "Sign in Failed!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void finishSignIn(){
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Intent intent = null;
                    if(task.getResult().exists()){
                        if(AppUtils.shouldGetFile(FirebaseAuth.getInstance().getUid() + SignUp.SIGNUP_FILE,
                                Login.this)){
                            //SAVE THAT USER HAS SIGNED UP
                            Map<String, String> signUpData = new HashMap<String, String>();
                            signUpData.put("signedUp", "true");
                            AppUtils.saveMapFile(FirebaseAuth.getInstance().getUid() + SignUp.SIGNUP_FILE,
                                    signUpData, Login.this);
                        }
                        intent = new Intent(Login.this, Main.class);
                    }else{
                        intent = new Intent(Login.this, SignUp.class);
                    }
                    Login.this.startActivity(intent);
                    Login.this.finish();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(AppUtils.isNetworkAvailable(this)){
            //DISABLE SIGN IN BUTTON
            signInButton.setEnabled(false);
            //ATTEMPT TO SIGN IN
            createSignInIntent();
        }else{
            Toast.makeText(this, "Internet Unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}
