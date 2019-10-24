package ca.staugustinechs.staugustineapp.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ca.staugustinechs.staugustineapp.Activities.Login;
import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.Activities.SignUp;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    Button setsSignOut, setsSignUp, setsFeedback;
    Switch setsClassesPrivate, setsClubsPrivate, setsNotifGeneral, setsDarkMode;

    private SharedPreferences sharedPrefs;
    //key values for shared prefs
    public static final String sharedPrefFile = "ca.staugustinechs.staugustineapp", DARK_KEY = "DARK_MODE";
    private boolean isDark;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sharedPrefs = Objects.requireNonNull(getContext()).getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE);

        isDark = sharedPrefs.getBoolean(DARK_KEY, false);

        setsSignOut = view.findViewById(R.id.setsSignOut);
        setsSignOut.setTextColor(AppUtils.PRIMARY_COLOR);
        setsSignOut.setOnClickListener(this);
        setsFeedback = view.findViewById(R.id.setsFeedback);
        setsFeedback.setTextColor(AppUtils.PRIMARY_COLOR);
        setsFeedback.setOnClickListener(this);
        setsSignUp = view.findViewById(R.id.setsSignUp);
        setsSignUp.setTextColor(AppUtils.PRIMARY_COLOR);
        setsSignUp.setOnClickListener(this);
        if (Main.PROFILE.getStatus() == Main.DEV) {
            setsSignUp.setVisibility(View.VISIBLE);
        }

        setsClassesPrivate = view.findViewById(R.id.setsClassesPrivate);
        setsClassesPrivate.setThumbTintList(AppUtils.ACCENT_COLORSL);
        setsClubsPrivate = view.findViewById(R.id.setsClubsPrivate);
        setsClubsPrivate.setThumbTintList(AppUtils.ACCENT_COLORSL);
        setsNotifGeneral = view.findViewById(R.id.setsNotifGeneral);
        setsNotifGeneral.setThumbTintList(AppUtils.ACCENT_COLORSL);
        setsDarkMode = view.findViewById(R.id.setsDarkMode);
        setsDarkMode.setThumbTintList(AppUtils.ACCENT_COLORSL);

        if (isDark) {
            setsDarkMode.setChecked(true);
        } else {
            setsDarkMode.setChecked(false);
        }

        setsDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    isDark = true;
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Objects.requireNonNull(getActivity()).finish();
                    //getDelegate().applyDayNight();
                } else {
                    isDark = false;
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Objects.requireNonNull(getActivity()).finish();
                    //getDelegate().applyDayNight();
                }
            }
        });

        if (Main.PROFILE != null) {
            setsClassesPrivate.setChecked(!Main.PROFILE.showClasses());
            setsClubsPrivate.setChecked(!Main.PROFILE.showClubs());
            if (Main.PROFILE.getNotifications() != null) {
                setsNotifGeneral.setChecked(Main.PROFILE.getNotifications().contains("general"));
            } else {
                setsNotifGeneral.setChecked(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(setsSignOut)) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this.getActivity(), Login.class);
            startActivity(intent);
            Objects.requireNonNull(this.getActivity()).finish();
        } else if (v.equals(setsFeedback)) {
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", "sachsappteam@gmail.com", null));
            intent.putExtra(Intent.EXTRA_SUBJECT, "[FEEDBACK]");
            startActivity(Intent.createChooser(intent, "Send Email"));
        } else if (v.equals(setsSignUp)) {
            Intent intent = new Intent(this.getActivity(), SignUp.class);
            startActivity(intent);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            if (Main.PROFILE != null) {
                Map<String, Object> data = new HashMap<>();
                if (setsClassesPrivate.isChecked() == Main.PROFILE.showClasses()) {
                    data.put("showClasses", !setsClassesPrivate.isChecked());
                }

                if (setsClubsPrivate.isChecked() == Main.PROFILE.showClubs()) {
                    data.put("showClubs", !setsClubsPrivate.isChecked());
                }

                if (setsNotifGeneral.isChecked() != Main.PROFILE.getNotifications().contains("general")) {
                    data.put("notifications", FieldValue.arrayUnion("general"));
                }

                if (!data.isEmpty()) {
                    FirebaseFirestore.getInstance().collection("users")
                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                            .update(data)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful() && Main.PROFILE != null) {
                                        Main.PROFILE.setPrefs(!setsClassesPrivate.isChecked(),
                                                !setsClubsPrivate.isChecked());
                                    }
                                }
                            });
                }
            }
        }
    }

    @Override
    public void onPause() {
        onHiddenChanged(true);
        super.onPause();
        SharedPreferences.Editor preferenceEditor = sharedPrefs.edit();
        preferenceEditor.putBoolean(DARK_KEY, isDark);
        preferenceEditor.apply();
    }

    @Override
    public void onDestroy() {
        onHiddenChanged(true);
        super.onDestroy();
    }
}
