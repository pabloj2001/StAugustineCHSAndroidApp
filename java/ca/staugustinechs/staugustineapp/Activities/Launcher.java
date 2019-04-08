package ca.staugustinechs.staugustineapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.R;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //FIREBASE REMOTE CONFIG
        final FirebaseRemoteConfig frc = FirebaseRemoteConfig.getInstance();
        frc.fetch(360).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    frc.activateFetched();
                }
                //SET RC VARIABLES
                AppUtils.setDefaultVariables();

                //GO TO MAIN
                Intent intent = new Intent(Launcher.this, Main.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });

        this.setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
    }
}
