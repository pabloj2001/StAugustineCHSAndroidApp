package ca.staugustinechs.staugustineapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;
import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.TagCreationTask;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.TitanTagEncryption;

public class TitanTagFragment extends Fragment implements MenuItem.OnMenuItemClickListener {

    private static final String PERMISSION_FILE = "titanTagPermission.dat";
    private ProgressBar ttLoadingCircle;
    private ImageView titanTag;
    private TextView ttDebugText;
    private Bitmap logo;
    private TagCreationTask task;
    private int previousBrightnessMode, previousBrightness;
    private boolean autoBrightness;
    private MenuItem itemAutoBrightness;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getAutoBrightPrefs();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_titantag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ttLoadingCircle = view.findViewById(R.id.ttLoadingCircle);
        ttLoadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
        titanTag = view.findViewById(R.id.ttTitanTag);

        if(Main.PROFILE.getStatus() == Main.DEV){
            ttDebugText = view.findViewById(R.id.ttDebugText);
            ttDebugText.setVisibility(View.VISIBLE);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.stalogo);
        logo = Bitmap.createScaledBitmap(bitmap, 160, 160, false);

        onHiddenChanged(false);
    }

    private void updateTitanTag(){
        if(task == null || task.isFinished()) {
            String email;
            if(AppUtils.ADD_K12_TO_TT){
                email = Main.PROFILE.getEmail();
            }else{
                email = Main.PROFILE.getEmail().split("@")[0];
            }

            task = new TagCreationTask(logo, this);
           // task.execute("will.smith19");
            task.execute(TitanTagEncryption.encrypt(email));
        }

        titanTag.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isHidden() && titanTag != null) {
                    if(ttDebugText !=  null){
                        ttDebugText.setText(TitanTagEncryption.encrypt(Main.PROFILE.getEmail())
                                + "\n" + TitanTagEncryption.getTime());
                    }
                    updateTitanTag();
                }
            }
        }, 500L);
        //Snackbar.make(getView(), TitanTagEncryption.getTime() + "", Snackbar.LENGTH_SHORT).show();
    }

    public void updateTag(Bitmap img){
        if(!this.isHidden()){
            int size = AppUtils.getDeviceDimen(R.dimen.tag_size, this.getActivity());
            titanTag.setImageBitmap(Bitmap.createScaledBitmap(img, size, size, false));
            titanTag.setVisibility(View.VISIBLE);
            ttLoadingCircle.setVisibility(View.GONE);
        }
    }

    private void updateBrightness(boolean hidden){
        if(autoBrightness){
            if(hidden){
                if(Settings.System.canWrite(this.getActivity())){
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE, previousBrightnessMode);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, previousBrightness);
                }
            }else{
                if(Settings.System.canWrite(this.getActivity())){
                    try {
                        previousBrightnessMode = Settings.System.getInt(getActivity().getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS_MODE);
                        previousBrightness = Settings.System.getInt(getActivity().getContentResolver(),
                                Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, 255);
                }else{
                    //ASK FOR PERMISSIONS
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("In order for Titan Tag scanning to work properly," +
                            " your phone's brightness should be set to max." +
                            " We can do this for you every time you enter this screen if you allow the app access to" +
                            " write system settings. Click \"Okay\" to go to your phone's settings where you can allow this.");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            itemAutoBrightness.setTitle("Enable Auto Brightness");
                            autoBrightness = false;
                            saveAutoBrightPrefs();
                        }
                    });
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TAKE USER TO SYSTEM SETTINGS
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:ca.staugustinechs.staugustineapp"));
                            startActivityForResult(intent, 21);

                            itemAutoBrightness.setTitle("Disable Auto Brightness");
                            saveAutoBrightPrefs();
                        }
                    });
                    builder.create().show();
                }
            }
        }
    }

    private void saveAutoBrightPrefs(){
        Map<String, String> data = new HashMap<String, String>();
        data.put("autoBrightness", autoBrightness + "");
        AppUtils.saveMapFile(TitanTagFragment.PERMISSION_FILE, data, this.getActivity());
    }

    private void getAutoBrightPrefs(){
        Map<String, String> data = AppUtils.loadMapFile(TitanTagFragment.PERMISSION_FILE, this.getActivity());
        if(data != null){
            autoBrightness = data.get("autoBrightness").equals("true");
        }else{
            autoBrightness = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_titantag, menu);

        if(!autoBrightness){
            menu.getItem(0).setTitle("Enable Auto Brightness");
        }
        itemAutoBrightness = menu.getItem(0).setOnMenuItemClickListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.equals(itemAutoBrightness)){
            if(!autoBrightness){
                //USER WANTS AUTO BRIGHTNESS
                //TRY TO ENABLE AUTO BRIGHTNESS AGAIN
                autoBrightness = true;
                updateBrightness(false);
            }else{
                //USER DOESN'T WANT AUTO BRIGHTNESS
                //SET BRIGHTNESS TO WHAT IT WAS BEFORE
                updateBrightness(true);
                //DISABLE AUTO BRIGHTNESS
                itemAutoBrightness.setTitle("Enable Auto Brightness");
                autoBrightness = false;
                saveAutoBrightPrefs();
            }

            saveAutoBrightPrefs();
            return true;
        }
        return false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden){
            if(task != null){
                task.cancel(true);
            }
        }else{
            task = null;
            updateTitanTag();
        }
        updateBrightness(hidden);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 21){
            updateBrightness(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDetach(){
        onHiddenChanged(true);
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        onHiddenChanged(true);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        onHiddenChanged(true);
        super.onStop();
    }
}