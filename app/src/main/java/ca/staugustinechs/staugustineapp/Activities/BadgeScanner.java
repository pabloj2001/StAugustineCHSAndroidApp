package ca.staugustinechs.staugustineapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.io.IOException;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.MessagingService;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.TitanTagEncryption;

@SuppressWarnings("deprecation")
public class BadgeScanner extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private String badgeId;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera cam;
    private FirebaseVisionImageMetadata metadata;
    private FirebaseVisionBarcodeDetectorOptions options;
    private boolean isDone = true, dialogHidden = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badgescanner);
        //SET TITLE AND ENABLE BACK ARROW IN TOP LEFT OF ACTIVITY
        this.getSupportActionBar().setTitle("Give Away Badge");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //GET BADGE ID WE ARE GIVING AWAY
        badgeId = getIntent().getExtras().getString("BADGE");
        //SET COLORS
        ((TextView) findViewById(R.id.bsSummary)).setTextColor(AppUtils.ACCENT_COLOR);
        findViewById(R.id.bsLinLayout).setBackgroundColor(AppUtils.PRIMARY_COLOR);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //INIT FIREBASE VISION OPTIONS
        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();

        //INIT METADATA FOR FIREBASE VISION
        try {
            //SUPPOSEDLY 480x360 IS GOOD ENGOUGH FOR IMAGE RECOGNITION,
            //BUT WHEN I FIRST TRIED IT OUT IT DIDN'T WORK SINCE THE IAMGE WAS SO SMALL
            //IT COULDN'T FIND THE QR CODE. THAT GOT FIXED BY MAKING THE IMAGE BIGGER
            metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(1920)   // 480x360 is typically sufficient for
                    .setHeight(1080)  // image recognition (HA not in this case...)
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(getRotationCompensation())
                    .build();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //INIT SURFACE VIEW AKA CAMERA PREVIEW
        this.surfaceView = findViewById(R.id.bsSurfaceView);
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
        this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.surfaceHolder.setSizeFromLayout();
    }

    //EVERY TIME THE SURFACE VIEW HAS A NEW FRAME TO DISPLAY THIS METHOD GETS CALLED
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //IF WE'RE DONE TRYING TO FIND QR CODES IN THE LAST FRAME
        //WE CAN TRY DOING SO IN THE NEXT AVAILABLE FRAME
        if(isDone){
            //GET THE IMAGE FROM THE PREVIEW DATA
            FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(data, metadata);
            //INIT IMAGE QR CODE DETECTOR
            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector(options);

            isDone = false;
            try {
                //EXECUTE TASK TO DETECT QR CODE IN PREVIEW FRAME
                Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                if (cam != null) {
                                    //IF WE'VE FOUND A QR CODE IN THE IMAGE, DO STUFF WITH IT
                                    if (barcodes.size() > 0) {
                                        FirebaseVisionBarcode barcode = barcodes.get(0);
                                        //DECRYPT THE EMAIL FROM THE QR CODE/TITAN TAG
                                        final String email = TitanTagEncryption.decrypt(barcode.getRawValue());
                                        //MAKE SURE EMAIL IS DECODED PROPERLY; BASICALY WE'RE CHECKING
                                        //IF THE EMAIL HAS ONLY LETTERS FROM A - Z AND NUMBERS FROM 0 - 9
                                        //AS WELL AS THE CHARACTERS . AND @
                                        if (email.matches("[A-Za-z0-9.@]+")) {
                                            //IF WE AREN'T SHOWING A CONFIRM DIALOG, CONTINUE
                                            if (dialogHidden) {
                                                dialogHidden = false;

                                                //VIBRATE DEVICE
                                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                // Vibrate for 500 milliseconds
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    v.vibrate(VibrationEffect.createOneShot(500,
                                                            VibrationEffect.DEFAULT_AMPLITUDE));
                                                } else {
                                                    //deprecated in API 26
                                                    v.vibrate(500);
                                                }

                                                //CREATE DIALOG PROMPTING ADMIN TO GIVE BADGE TO USER
                                                AlertDialog.Builder builder = new AlertDialog.Builder(BadgeScanner.this);
                                                builder.setMessage("Give badge to " + email + "?");
                                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialogHidden = true;
                                                        dialog.cancel();
                                                    }
                                                });
                                                //IF THE ADMIN CLICKS GIVE, GIVE THE USER WHOSE TITAN TAG
                                                //WE'VE SCANNED THE IMAGE
                                                builder.setPositiveButton("Give", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        giveBadge(email);
                                                        dialogHidden = true;
                                                    }
                                                });
                                                builder.create().show();
                                            }
                                        }
                                    }
                                    isDone = true;
                                }
                            }
                        });
            }catch(Exception e){
                isDone = true;
            }
        }
    }

    private void giveBadge(String email){
        final String showEmail = email;

        //MAKE SURE IT CONTAINS @ycdsbk12.ca
        if(!email.contains("@ycdsbk12.ca")){
            email += "@ycdsbk12.ca";
        }

        //GET USER'S DOCUMENT
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    final List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    final TextView bsSummary = findViewById(R.id.bsSummary);
                    if(docs.size() > 0 && docs.get(0).exists()){
                        final UserProfile user = new UserProfile(docs.get(0).getId(), docs.get(0).getData());
                        //GIVE USER THE BADGE
                        user.giveBadge(badgeId, new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task2) {
                                if (task2.isSuccessful()) {
                                    //SAY THE BADGE WAS GIVEN
                                    bsSummary.setText("Given badge to \n" + showEmail);
                                    bsSummary.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(!BadgeScanner.this.isDestroyed()){
                                                bsSummary.setText("Scan Titan Tag");
                                            }
                                        }
                                    }, 5000L);

                                    //GIVE USER POINTS
                                    user.updatePoints(AppUtils.ATTENDING_EVENT_POINTS, false,
                                            null, null);

                                    //SEND NOTIFICATION TO USER
                                    MessagingService.sendMessageToUser(docs.get(0).getString("msgToken"), "",
                                            "You Have Received A New Badge!");
                                }
                            }
                        });
                    }else{
                        bsSummary.setText("Couldn't give badge to " + showEmail);
                    }
                }
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //CHECK IF WE CAN USE THE CAMERA
        if(hasCamera()){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){
                //GET CAMERA
                this.cam = getCameraInstance();
                if(this.cam != null){
                    try {
                        //SET CAMERA PARAMETERS
                        Camera.Parameters parameters = cam.getParameters();
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        parameters.setPictureFormat(ImageFormat.NV21);
                        this.cam.setParameters(parameters);

                        //START DISPLAYING CAMERA PREVIEW ON SURFACE VIEW
                        this.cam.setPreviewCallback(this);
                        this.cam.setDisplayOrientation(90);
                        this.cam.setPreviewDisplay(surfaceHolder);
                        this.cam.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    //IF WE CAN'T USE THE CAMERA, FINISH THE ACTIVITY
                    finish();
                }
            }else{
                //REQUEST CAMERA PERMISSIONS
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 21);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.cam.setPreviewCallback(null);
        this.cam.stopPreview();
        this.cam.release();
    }

    /** Check if this device has a camera */
    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            return null;
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        //TRY TO CREATE THE SURFACE AGAIN ONCE THE CAMERA PERMISSION HAS BEEN GIVEN
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            surfaceCreated(surfaceHolder);
        }else{
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //WHEN THE BACK ARROW IS PRESSED IN THE TOP LEFT CORNER, FINISH THE ACTIVITY
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //STUFF TO DO WITH ORIENTATION, DON'T PAY ATTENTION
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation()
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // On most devices, the sensor orientation is 90 degrees, but for some
        // devices it is 270 degrees. For devices with a sensor orientation of
        // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraManager.getCameraIdList()[0])
                .get(CameraCharacteristics.SENSOR_ORIENTATION);
        rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

        // Return the corresponding FirebaseVisionImageMetadata rotation value.
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e("HERE", "Bad rotation value: " + rotationCompensation);
        }
        return result;
    }
}
