package com.crocosauruscove.croccove;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_BL = 2;

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "CrocCove";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private static String imageStoragePath;

    private long lastBackPressTime = 0;
    private Toast toast;

    ProgressBar mDialog;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner scanner;
    ScanSettings scanSettings;

    private List<String> scannedDeivcesList;
    private List<String> scannedDeivcesListForDisconnection;
    private ArrayAdapter<String> adapter;


    //DEFINE LAYOUT
    ListView devicesList;

    String mBeaconName, mBeaconNameLast;
    String mBeaconAddress;
    String mBeaconString;
    Boolean isDialogOpen = false;
    Boolean isPlayed = false;
    Boolean isCompleted = true;

    SessionManager session;
    String lang = "";
    String path = "";
    String isDundee = "";

    MediaPlayer mp;
    Button btnCamera, btnPlay;
    Integer position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Checking availability of the camera
        if (!CameraUtils.isDeviceSupportCamera(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device doesn't have camera
            finish();
        }

        mDialog = findViewById(R.id.progress_loader);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        devicesList = findViewById(R.id.deviceList);
        scannedDeivcesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, scannedDeivcesList);

        devicesList.setAdapter(adapter);

        session = new SessionManager(this);
        if(session.isLoggedIn()) {
            HashMap<String, String> user = session.getUserDetails();
            // name
            lang = user.get(SessionManager.KEY_LANG);
            path = user.get(SessionManager.KEY_PATH);
            isDundee = user.get(SessionManager.KEY_TRAIL);
        }

        if(!lang.equals("")){
            setLanguage(lang);
        }

        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraUtils.checkPermissions(getApplicationContext())) {
                    captureImage();
                } else {
                    //requestCameraPermission(MEDIA_TYPE_IMAGE);
                }
            }
        });

        setupMP();

        btnPlay = findViewById(R.id.btnPause);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isCompleted) {
                    PlayAudio();
                }else{
                    setupMP();
                    PlayAudio();
                }
            }
        });

        checkLocBT();
        initializeBluetooth();
        initBT();

        Bundle extra = getIntent().getExtras();
        if(extra != null){
            String bn = extra.getString("BEACON");
            Integer rs = extra.getInt("RSSI");
            String ad = extra.getString("ADDRESS");

            if(!bn.equals("")){
                mBeaconName = bn;
            }
            mBeaconString = rs.toString() + "  " + bn + "\n       (" + ad + ")";
            position = 0;
            vibrate();
            Toast.makeText(getApplicationContext(),"Connected to " + mBeaconName,Toast.LENGTH_LONG).show();
        }

        startLeScan(true);
    }

    /**
     * Capturing Camera Image will launch camera app requested image capture
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    public void setupMP(){
        mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_green));
                if (isDialogOpen) {
                    startLeScan(true);
                    isDialogOpen = false;
                }
                mp.release();
                mp = null;
                isCompleted = true;
                isPlayed = false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG);
            toast.show();
            this.lastBackPressTime = System.currentTimeMillis();
        } else {
            if (toast != null) {
                toast.cancel();
            }
            super.onBackPressed();
        }
    }

    private void setLanguage(String languageToLoad){
        String default_lang = "en";
        switch (languageToLoad){
            case "Chinese":
                default_lang = "ch";
                break;

            case "French":
                default_lang = "fr";
                break;

            case "German":
                default_lang = "gr";
                break;

            case "Dutch":
                default_lang = "de";
                break;

            case "Italian":
                default_lang = "it";
                break;
        }
        Locale locale = new Locale(default_lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_player);
    }

    private void initializeBluetooth(){

        //Check if device does support BT by hardware
        if (!getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            //Toast shows a message on the screen for a LENGTH_SHORT period
            Toast.makeText(this, "BLUETOOTH NOT SUPPORTED!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Check if device does support BT Low Energy by hardware. Else close the app(finish())!
        if (!getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //Toast shows a message on the screen for a LENGTH_SHORT period
            Toast.makeText(this, "BLE NOT SUPPORTED!", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            //If BLE is supported, get the BT adapter. Preparing for use!
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //If getting the adapter returns error, close the app with error message!
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "ERROR GETTING BLUETOOTH ADAPTER!", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                //Check if BT is enabled! This method requires BT permissions in the manifest.
                if (!mBluetoothAdapter.isEnabled()) {
                    //If it is not enabled, ask user to enable it with default BT enable dialog! BT enable response will be received in the onActivityResult method.
                    Intent enableBTintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTintent, PERMISSION_REQUEST_COARSE_BL);
                }
            }
        }
    }

    @TargetApi(23)
    private void checkLocBT(){
        //If Android version is M (6.0 API 23) or newer, check if it has Location permissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //If Location permissions are not granted for the app, ask user for it! Request response will be received in the onRequestPermissionsResult.
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        //Check if permission request response is from Location
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                return;
            }
        }
    }

    /**
     * Saving stored image path to saved instance state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(KEY_IMAGE_STORAGE_PATH, imageStoragePath);
    }

    /**
     * Restoring image path from saved instance state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        imageStoragePath = savedInstanceState.getString(KEY_IMAGE_STORAGE_PATH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Check if the response is from BT
        if(requestCode == PERMISSION_REQUEST_COARSE_BL) {
            // User chose not to enable Bluetooth.
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // successfully captured the image
                // display it in image view
                //previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // video successfully recorded
                // preview the recorded video
                //previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        startLeScan(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLeScan(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        startLeScan(false);
    }

    private void initBT(){
        final BluetoothManager bluetoothManager =  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Create the scan settings
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        //Set scan latency mode. Lower latency, faster device detection/more battery and resources consumption
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //Wrap settings together and save on a settings var (declared globally).
        scanSettings = scanSettingsBuilder.build();
        //Get the BLE scanner from the BT adapter (var declared globally)
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startLeScan(boolean endis) {
        if (endis) {
            scanner.startScan(null, scanSettings, mScanCallback);
        }else{
            scanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            String dName = result.getDevice().getName();
            if(dName != null) {
                if (dName.contains("CC Beacon #")) {
                    if(!dName.equals(mBeaconName)){
                        for(int i=0; i<scannedDeivcesList.size(); i++) {
                            if (!scannedDeivcesList.get(i).contains(dName)) {
                                scannedDeivcesList.add(mBeaconString);
                                scannedDeivcesListForDisconnection.remove(mBeaconString);
                            }
                        }
                        mBeaconString = result.getRssi() + "  " + dName + "\n       (" + result.getDevice().getAddress() + ")";
                        position += 1;
                        mBeaconNameLast = mBeaconName;
                        mBeaconName = dName;
                        mBeaconAddress = result.getDevice().getAddress();
                        isDialogOpen = true;
                        isPlayed = false;
                        vibrate();
                        btnPlay.setEnabled(true);
                        btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_green));
                        Toast.makeText(getApplicationContext(),"Connected to " + mBeaconName,Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    private void disconnectDevice(){
        btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_red));
        mp.reset();
        mBeaconName = "";
        mBeaconAddress = "";
        mBeaconString = "";
        btnPlay.setEnabled(false);
    }

    private void vibrate(){
        try {
            Vibrator vibrator = (Vibrator)getSystemService(this.VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private String getBeaconId(String beaconName){
        String b[] = beaconName.split("#");
        return b[1];
    }

    private String getFileName(String bId){
        if(session.isLoggedIn()) {
            String fileName = path+ "/";
            if(!isDundee.equals("")){
                fileName = fileName + isDundee;
            }
            fileName = fileName + lang + "_LOC" + bId + ".mp3";
            return fileName;
        }else {
            return "";
        }
    }

    private void PlayAudio(){
        String beaconName = mBeaconName;
        String bId = "";
        String fileName = "";
        isCompleted = false;
        if(!isPlayed) {
            if(!position.equals(0)){
                mp.reset();
            }
            if (!beaconName.equals(""))
                bId = getBeaconId(beaconName);

            if (!bId.equals(""))
                fileName = getFileName(bId);

            if (!fileName.equals("")) {
                File f = new File(fileName);
                if (f.exists()) {
                    try {
                        mp.setDataSource(fileName);//Write your location here
                        mp.prepare();
                        mp.start();
                        btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_gray));
                        isPlayed = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.unable_to_play), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.unable_to_play), Toast.LENGTH_LONG).show();
            }
        }else{
            if (mp.isPlaying()) {
                mp.pause();
                btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_green));
            } else {
                mp.start();
                btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_gray));
            }
        }
    }

//    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//        if (newState == BluetoothProfile.STATE_CONNECTED) {
//            //Device connected, start discovering services
//            if(gatt.getDevice().getName().equals(mBeaconName)) {
//                btnPlay.setEnabled(true);
//            }
//            mBluetoothGatt.discoverServices();
//        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//            //Device disconnected
//            if(gatt.getDevice().getName().equals(mBeaconName)) {
//                btnPlay.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.play_red));
//                mp.reset();
//                isDialogOpen = false;
//                mBeaconName = "";
//                mBeaconAddress = "";
//                mBeaconString = "";
//                startLeScan(true);
//                btnPlay.setEnabled(false);
//            }
//        }
//    }
}
