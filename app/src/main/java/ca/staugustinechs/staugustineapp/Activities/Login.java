package ca.staugustinechs.staugustineapp.Activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
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

        //SET TITLE AND ACTION BAR COLORS
        this.getSupportActionBar().setTitle(R.string.app_name);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        //SET UP SIGN IN BUTTON
        signInButton = (SignInButton) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);
    }

    public void createSignInIntent() {
        //MAKE GOOGLE SIGN IN OPTIONS
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        //SIGN ALL USERS OUT OF THE GOOGLE CLIENT SO IT DOESN'T AUTOMATICALLY
        //TRY TO SIGN IN WITH THE LAST GOOGLE ACCOUNT USED AND USER CAN CHOOSE WHICH
        //GOOGLE ACCOUNT TO USE.
        GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);
        googleClient.signOut();

        //CREATE SIGN IN WINDOW AND CALL OUR onActivityResult METHOD WHEN DONE
        startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_IN);
    }

    // [START auth_fui_result]
    //THIS METHOD GETS CALLED WHEN SIGN IN INTENT IS FINISHED
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            //GET THE GOOGLE SIGN IN TASK RESULT
            Task<GoogleSignInAccount> googleTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (googleTask.isSuccessful()) {
                // Successfully signed in
                //GET THE GOOGLE ACCOUNT
                GoogleSignInAccount account = googleTask.getResult();
                //ENSURE ACCOUNT EMAIL CONTAINS @ycdsbk12.ca
                if(account.getEmail().contains("@ycdsbk12.ca") || AppUtils.ALLOW_ACCOUNTS){
                    //SIGN INTO FIREBASE USING OUR GOOGLE ACCOUNT CREDENTIALS
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
        //GET THE USER DOCUMENT
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Intent intent = null;
                    if(task.getResult().exists()){
                        //IF THE USER'S DOCUMENT EXISTS (IF THE USER HAS SIGNED IN BEFORE),
                        //TAKE THEM TO THE MAIN ACTIVITY.
                        //BEFORE DOING THAT, CHECK IF THE FILE SAYING THAT WE HAVE SIGNED UP EXISTS,
                        //AND IF IT DOESN'T, MAKE THE FILE.
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
                        //TAKE THE USER TO THE SIGN UP ACTIVITY
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
