package ca.staugustinechs.staugustineapp;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ca.staugustinechs.staugustineapp.Activities.Main;

public class AppUtils {

    public static String SONG_REQUEST_THEME;
    public static String ANDROID_VERSION;
    public static boolean APP_ONLINE, ALLOW_ACCOUNTS, SHOW_USERS_ON_SONGS;
    public static int PRIMARY_COLOR, PRIMARY_DARK_COLOR, ACCENT_COLOR, STATUS_TWO_COLOR;
    public static ColorStateList PRIMARY_COLORSL, ACCENT_COLORSL;
    public static int[] PIC_COSTS;
    public static int MAX_SONGS, REQUEST_SONG_COST;
    public static double SUPER_VOTE_MULT;
    public static int SUPER_VOTE_MIN;
    public static int STARTING_POINTS, ATTENDING_EVENT_POINTS, JOINING_CLUB_POINTS;
    public static boolean SHOW_LOGO_IN_TT, ADD_K12_TO_TT;

    public static boolean shouldGetFile(String imgName, Activity activity) {
        File file = activity.getFileStreamPath(imgName);
        if(file.exists()){
            return false;
        }else{
            return true;
        }
    }

    public static void saveImg(Bitmap img, String imgName, long time, Activity activity){
        try {
            FileOutputStream stream = activity.openFileOutput(imgName + "_" + time, Context.MODE_PRIVATE);
            img.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            AppUtils.deleteExtraImgs(imgName, time, activity);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteExtraImgs(String imgName, long time, Activity activity){
        File dir = activity.getFileStreamPath(imgName);
        for(File file : dir.getParentFile().listFiles()){
            if(!file.getName().equals(imgName + "_" + time) && file.getName().contains(imgName)){
                file.delete();
            }
        }
    }

    public static Bitmap getImg(String imgName, Activity activity){
        try {
            return BitmapFactory.decodeFile(activity.getFileStreamPath(imgName).getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        if(activity != null){
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }else{
            return false;
        }
    }

    public static int dpToPx(float dp, Activity activity) {
        float density = activity.getApplicationContext().getResources().getDisplayMetrics().density;
        return (int) Math.ceil(dp * density);
    }

    public static int getDimen(int dimenId, Activity activity){
        return (int) activity.getResources().getDimension(dimenId);
    }

    public static int getDeviceDimen(int dimen, Activity activity){
        if(activity != null){
            float targetDp = 4f;
            float density = activity.getResources().getDisplayMetrics().density;
            return Math.round(AppUtils.getDimen(dimen, activity) * (density / targetDp));
        }
        return 0;
    }

    public static boolean loadCalendarAdded(Activity activity) {
        Map<String, String> data = AppUtils.loadMapFile(Main.CALENDAR_ADDED, activity);
        if(data != null){
            return Boolean.parseBoolean(data.get("CALENDAR_ADDED"));
        }else{
            return false;
        }
    }

    public static void saveCalendarAdded(boolean calendarAdded, Activity activity){
        Map<String, String> data = new HashMap<String, String>();
        data.put("CALENDAR_ADDED", calendarAdded + "");
        AppUtils.saveMapFile(Main.CALENDAR_ADDED, data, activity);
    }

    public static String[] loadFile(String filePath, Activity activity){
        File file = new File(activity.getFilesDir(), filePath);
        if (file.exists()) {
            FileInputStream is = null;
            BufferedReader br = null;
            try {
                is = activity.openFileInput(filePath);
                br = new BufferedReader(new InputStreamReader(is));

                StringBuilder data = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    data.append(line);
                }

                return data.toString().split(";");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Map<String, String> loadMapFile(String filePath, Activity activity){
        File file = new File(activity.getFilesDir(), filePath);
        if (file.exists()) {
            FileInputStream is = null;
            BufferedReader br = null;
            try {
                is = activity.openFileInput(filePath);
                br = new BufferedReader(new InputStreamReader(is));

                Map<String, String> data = new HashMap<String, String>();
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] dataPiece = line.split(":");
                    if(dataPiece.length > 1){
                        data.put(dataPiece[0], dataPiece[1]);
                    }
                }

                return data;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void saveFile(String file, String[] data, Activity activity){
        FileOutputStream fos = null;
        try{
            fos = activity.openFileOutput(file, Context.MODE_PRIVATE);

            String combined = AppUtils.combineWithRegex(data);

            fos.write(combined.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean saveMapFile(String file, Map<String, String> data, Activity activity){
        FileOutputStream fos = null;
        try{
            fos = activity.openFileOutput(file, Context.MODE_PRIVATE);

            StringBuilder combined = new StringBuilder();
            for(Map.Entry<String, String> piece : data.entrySet()){
                combined.append(piece.getKey() + ":" + piece.getValue() + "\n");
            }

            fos.write(combined.toString().getBytes());
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String combineWithRegex(String[] data){
        String combined = "";
        for(String piece : data){
            combined += piece + ";";
        }
        combined = combined.substring(0, combined.length() - 1);

        return combined;
    }

    public static byte[] getImgBytes(Uri imgUri, int width, int height, Activity activity){
        try {
            Bitmap img = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imgUri);
            return getImgBytes(img, width, height, activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getImgBytes(Bitmap img, int width, int height, Activity activity){
        try {
            if(width <= 0 || height <= 0){
                width = 700;
                height = (int) (((double) width / (double) img.getWidth()) * (double) img.getHeight());
            }
            img = Bitmap.createScaledBitmap(img, width, height, false);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.PNG, Main.IMG_QUALITY, stream);
            byte[] byteArray = stream.toByteArray();
            img.recycle();

            return byteArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void uploadImg(String imgName, byte[] imgBytes, String bucket,
                                 final OnCompleteListener completeListener){
        if(imgBytes != null){
            FirebaseStorage.getInstance().getReference()
                    .child(bucket + imgName)
                    .putBytes(imgBytes)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskStorage) {
                            if (taskStorage.isSuccessful()) {
                                if(completeListener != null){
                                    completeListener.onComplete(taskStorage);
                                }
                            } else {
                                if (taskStorage.getResult() != null) {
                                    FirebaseStorage.getInstance()
                                            .getReference(taskStorage.getResult().getStorage().getPath())
                                            .delete();
                                }
                            }
                        }
                    });
        }
    }

    public static String getRandomKey(int length){
        StringBuilder key = new StringBuilder();
        //48 - 57, 65 - 90, 97 - 122
        for(int i = 0; i < length; i++){
            int type = (int) Math.floor(Math.random() * 10);
            if(type <= 4){
                key.append((int) Math.floor(Math.random() * 9));
            }else if(type <= 7){
                key.append((char) (Math.floor(Math.random() * (90 - 65)) + 65));
            }else{
                key.append((char) (Math.floor(Math.random() * (122 - 97)) + 97));
            }
        }
        return key.toString();
    }

    public static void setDefaultVariables() {
        FirebaseRemoteConfig frc = FirebaseRemoteConfig.getInstance();

        AppUtils.APP_ONLINE = frc.getBoolean("AppOnline");
        AppUtils.ALLOW_ACCOUNTS = frc.getBoolean("AllowAccounts");

        AppUtils.ANDROID_VERSION = frc.getString("ANDROID_VERSION");

        AppUtils.PRIMARY_COLOR = Color.parseColor(frc.getString("primaryColor"));
        AppUtils.PRIMARY_DARK_COLOR = Color.parseColor(frc.getString("darkerPrimary"));
        AppUtils.ACCENT_COLOR = Color.parseColor(frc.getString("accentColor"));
        AppUtils.PRIMARY_COLORSL = new ColorStateList(new int[][]{{}}, new int[]{AppUtils.PRIMARY_COLOR});
        AppUtils.ACCENT_COLORSL = new ColorStateList(new int[][]{{}}, new int[]{AppUtils.ACCENT_COLOR});
        AppUtils.STATUS_TWO_COLOR = Color.parseColor(frc.getString("statusTwoPrimary"));

        AppUtils.PIC_COSTS = new int[5];
        AppUtils.PIC_COSTS[0] = (int) frc.getLong("basicPic");
        AppUtils.PIC_COSTS[1] = (int) frc.getLong("commonPic");
        AppUtils.PIC_COSTS[2] = (int) frc.getLong("rarePic");
        AppUtils.PIC_COSTS[3] = (int) frc.getLong("coolPic");
        AppUtils.PIC_COSTS[4] = (int) frc.getLong("legendaryPic");

        AppUtils.MAX_SONGS = (int) frc.getLong("maxSongs");
        AppUtils.REQUEST_SONG_COST = (int) frc.getLong("requestSong");
        AppUtils.SHOW_USERS_ON_SONGS = frc.getBoolean("showUsersOnSongs");
        AppUtils.SUPER_VOTE_MULT = frc.getDouble("supervoteRatio");
        AppUtils.SUPER_VOTE_MIN = (int) frc.getLong("supervoteMin");

        AppUtils.STARTING_POINTS = (int) frc.getLong("startingPoints");
        AppUtils.ATTENDING_EVENT_POINTS = (int) frc.getLong("attendingEvent");
        AppUtils.JOINING_CLUB_POINTS = (int) frc.getLong("joiningClub");

        AppUtils.SONG_REQUEST_THEME = frc.getString("songRequestTheme");

        AppUtils.SHOW_LOGO_IN_TT = frc.getBoolean("showLogoInTT");
        AppUtils.ADD_K12_TO_TT = frc.getBoolean("addK12ToTT");
    }

    public static void performCrop(Uri picUri, int width, int height, int code, Activity activity) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", (width / height));
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", width);
            cropIntent.putExtra("outputY", height);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            activity.startActivityForResult(cropIntent, code);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            anfe.printStackTrace();
        }
    }

    public static int longToInt(Long number){
        if(Build.VERSION.SDK_INT > 23) {
            return Math.toIntExact(number);
        }else{
            return (int) ((long) number);
        }
    }
}
