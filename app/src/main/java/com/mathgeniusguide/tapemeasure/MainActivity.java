package com.mathgeniusguide.tapemeasure;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.SensorEventListener;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.util.Measure;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.os.Handler;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.Toast;

import com.mathgeniusguide.tapemeasure.data.SettingsDbHelper;
import com.mathgeniusguide.tapemeasure.util.IabHelper;
import com.mathgeniusguide.tapemeasure.util.IabResult;
import com.mathgeniusguide.tapemeasure.util.Inventory;
import com.mathgeniusguide.tapemeasure.util.Purchase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.button;
import static android.R.attr.id;
import static android.widget.Toast.makeText;
import static com.mathgeniusguide.tapemeasure.R.id.cameraAngle;
import static com.mathgeniusguide.tapemeasure.R.id.depthfoot;
import static com.mathgeniusguide.tapemeasure.R.id.depthinch;
import static com.mathgeniusguide.tapemeasure.R.id.heightfoot;
import static com.mathgeniusguide.tapemeasure.R.id.inchlabels;
import static com.mathgeniusguide.tapemeasure.R.id.lengthfoot;
import static com.mathgeniusguide.tapemeasure.R.id.lengthinch;
import static com.mathgeniusguide.tapemeasure.R.id.plusminus;
import static com.mathgeniusguide.tapemeasure.R.id.slantfoot;
import static com.mathgeniusguide.tapemeasure.R.id.slantinch;
import static com.mathgeniusguide.tapemeasure.R.id.typeScreen;
import static com.mathgeniusguide.tapemeasure.R.id.widthfoot;
import static com.mathgeniusguide.tapemeasure.R.id.widthinch;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    float pitch;
    float pitchActual;
    double yaw;
    double yawActual;
    int eyeset;
    int verticalErrorSide = -1;
    int magneticErrorSide = -1;
    String unit = "in";
    boolean drawYaw = false;
    boolean drawPitch = false;
    int currentStep = 0;
    boolean fixable[] = {false, false, false};
    double pitchx[] = {402, 402, 402, 402, 402, 402, 402, 402, 402, 402, 402};
    double yawx[] = {402, 402, 402, 402, 402, 402, 402, 402, 402, 402, 402};
    String phrase[] = {"Step 1", "Step 2", "Step 3", "Step 4", "Step 5", "Step 6", "Step 7", "Step 8", "Step 9", "Step 10"};
    String goal = "calibrate";
    String type = "enter";
    String position = "inside";
    String facing = "parallel";
    String target = "eye1";
    double eye1 = 60;
    double eye2 = 30;
    double stride = 36;
    double offset = 0;
    double incline = 0;
    double inclineYaw = 0;
    double cameraHeight = 5.25;
    double cameraAngle = 26.5651;
    double heightResult = 0;
    double depthResult = 0;
    double widthResult = 0;
    double lengthResult = 0;
    double slantResult = 0;
    double adjustmentResult = 0;
    double angleResult = 0;
    double heightDisplay = 0;
    double depthDisplay = 0;
    double widthDisplay = 0;
    double lengthDisplay = 0;
    double slantDisplay = 0;
    double adjustmentDisplay = 0;
    double angleDisplay = 0;
    boolean skipped = false;
    boolean negativeAllowed = false;
    boolean accUsing = false;
    boolean magUsing = false;
    int accuracy = 100;
    int accuracy2 = 100;
    int fixtime = 100;
    int timeacc = 100;
    int timemag = 100;
    int clickAlarmAcc = -1;
    int clickAlarmMag = -1;
    float[] gData = new float[3]; // accelerometer values
    float[] mData = new float[3]; // magnetometer values
    float[] rMat = new float[9];
    float[] iMat = new float[9];
    float[] orientation = new float[3];
    CountDownTimer oneSecond = new CountDownTimer(1000, 500) {
        public void onTick(long a) {

        }

        public void onFinish() {
            currentStep = 3;
            Button Press = (Button) findViewById(R.id.press);
            pitchx[2] = pitchx[0];
            yawx[2] = yawx[0];
            skipped = true;
            Press.setText(phrase[3]);
        }
    };
    private SensorManager mSensorManager;
    private MediaPlayer clickSound;
    String digitalFont = "fonts/DIGITALDREAMFATNARROW.ttf";

    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    final String cameraHeightText = "Camera Height is used when measuring with the camera. The Camera Height is the distance from the bottom of your device to the camera lens.\n\n";
    final String cameraAngleText = "Camera Angle is used when measuring with the camera. The Camera Angle is the angle between what's directly in front of the camera and what's shown at the top or bottom of what's visible on your screen.\n\n";
    final String cameraByHeightText1 = "Enter the height of the object then hold your phone so that the bottom boundary line is at the bottom of the object and the top boundary line is at the top of the object. Then, press to set Camera ";
    final String cameraByHeightText2 = ". Make sure the bottom of your phone is at the same altitude as the bottom of the object.";
    final String cameraByLengthText1 = "Enter the length of the object then hold your phone so that the bottom boundary line is at the nearer end of the object and the top boundary line is at the further end of the object. Then, press to set Camera ";
    final String cameraByLengthText2 = ". Make sure the bottom of your phone is at the same altitude as the object.";
    final String cameraByFromYouText1 = "Enter the distance from you and hold your phone so that the boundary line is at the point you're measuring the distance to. Then, press to set Camera ";
    final String cameraByFromYouText2 = ". Make sure the bottom of your phone is at the same altitude as the distance you're measuring to.";
    final String cameraByLevelSurfaceText = "Hold your phone so the camera is at the same altitude as a level surface. Tilt your phone so the level surface is shown at the top boundary line. Then, press to set Camera Angle.";

    final String eye1Uses = "Eye Height #1 is needed for most measurements. The only exceptions are Measure From Two Positions and Measure Using Camera.\n";
    final String eye2Uses = "Eye Height #2 is needed for Measure From Two Heights.\n";
    final String strideUses = "Stride Distance is used in Measure From Two Positions, with the Stride Distance being the distance between the two positions.\n";
    final String inclineUses = "Ground Incline is useful for measuring when you're not on level ground but using a method that normally requires you to be on level ground (Measure Length, Measure Height, Measure Using Camera). In order for the measurements to be accurate, the incline must be uniform.\n";
    final String offsetUses = "Pitch Offset is useful if you attach targets to your phone for more accurate aiming.\n";
    final String deleteText = "\nPressing DELETE will delete the selected saved value.";
    final String saveText2 = "\nIf you enter a name in the Save As field, you can load the saved value later using that name.";

    String stringForm = "";

    int boundarySelected = 0;
    int boundaryLeft;
    int boundaryRight;
    int boundaryTop;
    int boundaryBottom;
    int boundaryLeftMax;
    int boundaryTopMax;
    int boundaryRightMax;
    int boundaryBottomMax;
    int minimumSpace;
    int clickOffset = 0;

    double dp;
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    boolean testing = false;

    // camera variables
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    // In App Purchases
    private static final String TAG = "TapeMeasure";
    IabHelper mHelper;
    boolean purchases[] = {false};
    String items[] = {"com.mathgeniusguide.tapemeasure.fullversion"};
    int currentItem = 0;

    // Class Functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_screen);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        dp = 1.0 / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        accuracy = pref.getInt("precision", 100);
        accuracy2 = accuracy;
        unit = pref.getString("unit", "in");
        eye1 = (double) pref.getFloat("eye1", 60);
        eye2 = (double) pref.getFloat("eye2", 30);
        stride = (double) pref.getFloat("stride", 36);
        cameraHeight = (double) pref.getFloat("cameraHeight", (float) 5);
        cameraAngle = (double) pref.getFloat("cameraAngle", (float) 30);
        incline = (double) pref.getFloat("incline", 0);
        inclineYaw = (double) pref.getFloat("inclineYaw", 0);
        offset = (double) pref.getFloat("offset", 0);
        saveAll();
        if (unit.equals("cm")) {
            eye1 *= 2.54;
            eye2 *= 2.54;
            stride *= 2.54;
            cameraHeight *= 2.54;
        }

        if (!testing) {
            viewGone(R.id.checklist, R.id.measureTest);
        }

        viewGone(R.id.consume);

        // In App Purchases
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjWhGY7qg8PSQ8U+NH78S8xeLzle9E6oK4iUy5EAP1SNAfiJeWgHwYRasgq13MvKzm6+aFMs5xTozFgQRcpIBjaWlMdgmDb5yuwKBBUv0lbLxZTTZphh9JRVs5A+FKN95NhR+23u3yl8XvSGSF0a99YLQ8AwtstUk6NPOswWCVAAg3iK6TP+fiIirDMsFmeSTccnsfnQu5Gj90urisJdEhE5Nlc41mI+gdt2e9ZJaNKT28Sin61A4h+R0XPUpzSdcB5Lldy2vjR8ijSRt54tjkJwAXlQ8Kov7aG0DzsXGGowdNV/QqnJBocP/HArcemjGSMEdp6DysEWa24ucs/32eQIDAQAB";

        mHelper = new com.mathgeniusguide.tapemeasure.util.IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new com.mathgeniusguide.tapemeasure.util.IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(com.mathgeniusguide.tapemeasure.util.IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });
    }

    // Camera Functions
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            back(false);
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            back(false);
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
            back(false);
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            back(false);
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void cameraBoundaryListener() {
        ImageView cameraBoundary;
        boundaryLeft = 25;
        boundaryRight = 320;
        boundaryTop = 25;
        boundaryBottom = 315;
        boundaryTopMax = 25;
        boundaryLeftMax = 25;
        boundaryRightMax = 320;
        boundaryBottomMax = 315;
        minimumSpace = 15;
        clickOffset = 0;
        cameraBoundary = (ImageView) findViewById(R.id.cameraLeft);
        cameraBoundary.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clickOffset = boundaryLeft - (int) (event.getRawX() * dp);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int newX = (int) (event.getRawX() * dp) + clickOffset;
                    if (newX < boundaryRight - minimumSpace && newX > boundaryLeftMax) {
                        boundaryLeft = newX;
                        setViewMargins(view, boundaryLeft, 0, 0, 0);
                    }
                }
                return true;
            }
        });
        cameraBoundary = (ImageView) findViewById(R.id.cameraRight);
        cameraBoundary.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clickOffset = boundaryRight - (int) (event.getRawX() * dp);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int newX = (int) (event.getRawX() * dp) + clickOffset;
                    if (newX > boundaryLeft + minimumSpace && newX < boundaryRightMax) {
                        boundaryRight = newX;
                        setViewMargins(view, boundaryRight, 0, 0, 0);
                    }
                }
                return true;
            }
        });
        cameraBoundary = (ImageView) findViewById(R.id.cameraTop);
        cameraBoundary.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clickOffset = boundaryTop - (int) (event.getRawY() * dp);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int newY = (int) (event.getRawY() * dp) + clickOffset;
                    if (newY < boundaryBottom - minimumSpace && newY > boundaryTopMax) {
                        boundaryTop = newY;
                        setViewMargins(view, 0, boundaryTop, 0, 0);
                    }
                }
                return true;
            }
        });
        cameraBoundary = (ImageView) findViewById(R.id.cameraBottom);
        cameraBoundary.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clickOffset = boundaryBottom - (int) (event.getRawY() * dp);
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int newY = (int) (event.getRawY() * dp) + clickOffset;
                    if (newY > boundaryTop + minimumSpace && newY < boundaryBottomMax) {
                        boundaryBottom = newY;
                        setViewMargins(view, 0, boundaryBottom, 0, 0);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                back(false);
                showToast("Sorry. You can't use the Camera without granting the Camera permission.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        sensorSwitch(Sensor.TYPE_ACCELEROMETER, accUsing);
        sensorSwitch(Sensor.TYPE_MAGNETIC_FIELD, magUsing);
        if (type == "camera") {
            if (textureView.isAvailable()) {
                openCamera();
            } else {
                textureView.setSurfaceTextureListener(textureListener);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorSwitch(Sensor.TYPE_ACCELEROMETER, false);
        sensorSwitch(Sensor.TYPE_MAGNETIC_FIELD, false);
        stopBackgroundThread();
    }

    // Math Functions
    public double dsin(double theta) {
        theta *= Math.PI / 180;
        return Math.sin(theta);
    }

    public double dcos(double theta) {
        theta *= Math.PI / 180;
        return Math.cos(theta);
    }

    public double dtan(double theta) {
        theta *= Math.PI / 180;
        return Math.tan(theta);
    }

    public double darcsin(double val) {
        double theta = Math.asin(val);
        return (theta * 180 / Math.PI);
    }

    public double darccos(double val) {
        double theta = Math.acos(val);
        return (theta * 180 / Math.PI);
    }

    public double darctan(double val) {
        double theta = Math.atan(val);
        return (theta * 180 / Math.PI);
    }

    public double darctan2(double yval, double xval) {
        double theta = Math.atan2(yval, xval);
        return theta * 180 / Math.PI;
    }

    public float lerp(float num1, float num2, float num3) {
        return num2 * num3 + num1 * (1 - num3);
    }

    public double angle_difference(double num1, double num2) {
        if (num1 > num2 + 180) {
            num2 += 360;
        }
        if (num2 > num1 + 180) {
            num1 += 360;
        }
        return num2 - num1;
    }

    public double angle_midpoint(double num1, double num2) {
        return ((num1 + angle_difference(num1, num2) / 2) % 360);
    }

    public boolean chance(double num) {
        return (num > Math.random());
    }

    public boolean isBetween(double num1, double num2, double num3) {
        return ((num1 >= num2 && num2 >= num3) || (num1 <= num2 && num2 <= num3));
    }

    public String angleToDirection(double angle) {
        switch ((int) Math.floor(angle / 45)) {
            case 0:
                return (String.format("%1$.2f", angle) + "° East of North");
            case 1:
                return (String.format("%1$.2f", 90 - angle) + "° North of East");
            case 2:
                return (String.format("%1$.2f", angle - 90) + "° South of East");
            case 3:
                return (String.format("%1$.2f", 180 - angle) + "° East of South");
            case 4:
                return (String.format("%1$.2f", angle - 180) + "° West of South");
            case 5:
                return (String.format("%1$.2f", 270 - angle) + "° South of West");
            case 6:
                return (String.format("%1$.2f", angle - 270) + "° North of West");
            case 7:
                return (String.format("%1$.2f", 360 - angle) + "° West of North");
            default:
                return "error";
        }
    }

    public double round(double num, double significance) {
        return Math.round(num / significance) * significance;
    }

    // Setup Functions
    public void setUpSaveSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
        List<String> list = new ArrayList<String>();
        list.add("Load From:");

        SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {"name", "target"};
        Cursor cursor = db.query("settings", projection, null, null, null, null, "name ASC");

        String targetText = "";
        switch (target) {
            case "eye1":
            case "eye2": {
                targetText = "eye";
                break;
            }
            default: {
                targetText = target;
            }
        }
        int i;
        for (i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if (targetText.equals(cursor.getString(cursor.getColumnIndex("target")))) {
                list.add(cursor.getString(cursor.getColumnIndex("name")));
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        cursor.close();
        db.close();
        mDbHelper.close();
    }

    public void setUpTestSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.formulaTestSpinner);
        List<String> list = new ArrayList<String>();
        list.add("Values Only");
        list.add("Height: Same Incline Direction");
        list.add("Height: Magnetic Sensor");
        list.add("Length: From You");
        list.add("Length: Parallel Inside");
        list.add("Length: Parallel Inside Skipped");
        list.add("Length: Parallel Outside");
        list.add("Length: Parallel Outside Skipped");
        list.add("Length: Perpendicular Inside");
        list.add("Length: Perpendicular Outside");
        list.add("Length: Hypotenuse");
        list.add("Length: Hypotenuse Skipped");
        list.add("Length: Free");
        list.add("Length: Free Skipped");
        list.add("Length: Surface");
        list.add("2Ht: From You");
        list.add("2Ht: Parallel Inside");
        list.add("2Ht: Parallel Outside");
        list.add("2Ht: Perpendicular Inside");
        list.add("2Ht: Perpendicular Outside");
        list.add("2Ht: Hypotenuse");
        list.add("2Ht: Free");
        list.add("2Pos: Parallel");
        list.add("2Pos: Perpendicular");
        list.add("2Pos: Free");
        list.add("2Pos: Object Positions");
        list.add("Angle: Vertical Only");
        list.add("Angle: Free");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void setUpCameraSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.cameraSpinner);
        List<String> list = new ArrayList<String>();
        list.add("From You");
        list.add("Length");
        list.add("Height");
        list.add("Width");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void setBoundariesVisible(boolean left, boolean right, boolean top, boolean bottom) {
        View viewLeft = findViewById(R.id.cameraLeft);
        View viewRight = findViewById(R.id.cameraRight);
        View viewTop = findViewById(R.id.cameraTop);
        View viewBottom = findViewById(R.id.cameraBottom);
        if (left) {
            viewLeft.setVisibility(View.VISIBLE);
        } else {
            setViewMargins(viewLeft, boundaryLeftMax, 0, 0, 0);
            viewLeft.setVisibility(View.GONE);
            boundaryLeft = boundaryLeftMax;
        }
        if (right) {
            viewRight.setVisibility(View.VISIBLE);
        } else {
            setViewMargins(viewRight, boundaryRightMax, 0, 0, 0);
            viewRight.setVisibility(View.GONE);
            boundaryRight = boundaryRightMax;
        }
        if (top) {
            viewTop.setVisibility(View.VISIBLE);
        } else {
            setViewMargins(viewTop, 0, boundaryTopMax, 0, 0);
            viewTop.setVisibility(View.GONE);
            boundaryTop = boundaryTopMax;
        }
        if (bottom) {
            viewBottom.setVisibility(View.VISIBLE);
        } else {
            setViewMargins(viewBottom, 0, boundaryBottomMax, 0, 0);
            viewBottom.setVisibility(View.GONE);
            boundaryBottom = boundaryBottomMax;
        }
    }

    public int viewId(String string) {
        return this.getResources().getIdentifier(string, "id", this.getPackageName());
    }

    public View findViewByIdString(String string) {
        return findViewById(viewId(string));
    }

    public void setUsing(boolean acc, boolean mag) {
        accUsing = acc;
        magUsing = mag;
        clickAlarmAcc = -1;
        clickAlarmMag = -1;
        sensorSwitch(Sensor.TYPE_ACCELEROMETER, acc);
        sensorSwitch(Sensor.TYPE_MAGNETIC_FIELD, mag);
    }

    public void sensorSwitch(int sensorType, boolean isOn) {
        if (isOn) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(sensorType),
                    sensorType == Sensor.TYPE_ACCELEROMETER ? 10000 : 20000);
        } else {
            mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(sensorType));
        }
    }

    public void maxStep(int num) {
        int i;
        for (i = 0; i < 8; i++) {
            if (i < num) {
                pitchx[i] = 400;
            } else {
                pitchx[i] = 402;
            }
        }
    }

    public void skip(int num) {
        pitchx[num] = 401;
    }

    public void gray(int... views) {
        if (!purchases[0]) {
            for (int id : views) {
                View view = findViewById(id);
                view.setBackgroundColor(Color.parseColor("#404080"));
                ((Button) view).setTextColor(Color.parseColor("#404040"));
            }
        }
    }

    public void makeDigital(int... val) {
        int i;
        Typeface digital = Typeface.createFromAsset(getAssets(), digitalFont);
        for (i = 0; i < val.length; i++) {
            TextView view = (TextView) findViewById(val[i]);
            view.setTypeface(digital);
            view.setTextSize(25);
        }
    }

    public void makeDigitalEditText(int... val) {
        int i;
        Typeface digital = Typeface.createFromAsset(getAssets(), digitalFont);
        for (i = 0; i < val.length; i++) {
            EditText view = (EditText) findViewById(val[i]);
            view.setTypeface(digital);
            view.setTextSize(25);
        }
    }

    public void setUpTestCalibration() {
        int i;

        EditText eye1Whole = (EditText) findViewById(R.id.eye1Whole);
        EditText eye1Decimal = (EditText) findViewById(R.id.eye1Decimal);
        EditText eye2Whole = (EditText) findViewById(R.id.eye2Whole);
        EditText eye2Decimal = (EditText) findViewById(R.id.eye2Decimal);
        EditText strideWhole = (EditText) findViewById(R.id.strideWhole);
        EditText strideDecimal = (EditText) findViewById(R.id.strideDecimal);
        EditText phoneWhole = (EditText) findViewById(R.id.phoneWhole);
        EditText phoneDecimal = (EditText) findViewById(R.id.phoneDecimal);
        EditText offsetWhole = (EditText) findViewById(R.id.offsetWhole);
        EditText offsetDecimal = (EditText) findViewById(R.id.offsetDecimal);
        EditText inclinePitchWhole = (EditText) findViewById(R.id.inclinePitchWhole);
        EditText inclinePitchDecimal = (EditText) findViewById(R.id.inclinePitchDecimal);
        EditText inclineYawWhole = (EditText) findViewById(R.id.inclineYawWhole);
        EditText inclineYawDecimal = (EditText) findViewById(R.id.inclineYawDecimal);
        TextView offsetSign = (TextView) findViewById(R.id.offsetSign);
        TextView inclineSign = (TextView) findViewById(R.id.inclineSign);

        eye1Whole.setText(String.format("%1$.0f", Math.floor(eye1)));
        eye1Decimal.setText(String.format("%01d", (int) Math.floor(eye1 * 10) % 10));
        eye2Whole.setText(String.format("%1$.0f", Math.floor(eye2)));
        eye2Decimal.setText(String.format("%01d", (int) Math.floor(eye2 * 10) % 10));
        strideWhole.setText(String.format("%1$.0f", Math.floor(stride)));
        strideDecimal.setText(String.format("%01d", (int) Math.floor(stride * 10) % 10));
        phoneWhole.setText(String.format("%1$.0f", Math.floor(cameraHeight)));
        phoneDecimal.setText(String.format("%02d", (int) Math.floor(cameraHeight * 100) % 100));
        offsetWhole.setText(String.format("%1$.0f", Math.floor(Math.abs(offset))));
        offsetDecimal.setText(String.format("%04d", (int) Math.floor(Math.abs(offset) * 10000) % 10000));
        inclinePitchWhole.setText(String.format("%1$.0f", Math.floor(Math.abs(incline))));
        inclinePitchDecimal.setText(String.format("%04d", (int) Math.floor(Math.abs(incline) * 10000) % 10000));
        inclineYawWhole.setText(String.format("%1$.0f", Math.floor(inclineYaw)));
        inclineYawDecimal.setText(String.format("%04d", (int) Math.floor(inclineYaw * 10000) % 10000));

        if (offset < 0) {
            offsetSign.setText("   -");
        }

        if (incline < 0) {
            inclineSign.setText("   -");
        }

        for (i = 0; i < 4; i++) {
            TextView unitView = (TextView) findViewByIdString("unit" + i);
            unitView.setText(unit);
        }
    }

    public void executeTestCalibration() {
        eye1 = getWholeValue(R.id.eye1Whole) + getDecimalValue(R.id.eye1Decimal);
        eye2 = getWholeValue(R.id.eye2Whole) + getDecimalValue(R.id.eye2Decimal);
        stride = getWholeValue(R.id.strideWhole) + getDecimalValue(R.id.strideDecimal);
        offset = getWholeValue(R.id.offsetWhole) + getDecimalValue(R.id.offsetDecimal);
        incline = getWholeValue(R.id.inclinePitchWhole) + getDecimalValue(R.id.inclinePitchDecimal);
        inclineYaw = getWholeValue(R.id.inclineYawWhole) + getDecimalValue(R.id.inclineYawDecimal);

        TextView offsetSign = (TextView) findViewById(R.id.offsetSign);
        TextView inclineSign = (TextView) findViewById(R.id.inclineSign);

        if (offsetSign.getText() == "   -") {
            offset *= -1;
        }
        if (inclineSign.getText() == "   -") {
            incline *= -1;
        }

        saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
        saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
        saveFloat("stride", stride / (unit.equals("cm") ? 2.54 : 1));
        saveFloat("cameraHeight", cameraHeight / (unit.equals("cm") ? 2.54 : 1));
        saveFloat("offset", offset);
        saveFloat("incline", incline);
        saveFloat("inclineYaw", inclineYaw);
    }

    public void displayCM() {
        if (unit.equals("cm")) {
            TextView inchlabelh = (TextView) findViewById(R.id.inchlabelh);
            TextView inchlabeld = (TextView) findViewById(R.id.inchlabeld);
            TextView inchlabelw = (TextView) findViewById(R.id.inchlabelw);
            TextView inchlabell = (TextView) findViewById(R.id.inchlabell);
            TextView inchlabels = (TextView) findViewById(R.id.inchlabels);
            LinearLayout footlabel = (LinearLayout) findViewById(R.id.footlabel);
            LinearLayout footnumber = (LinearLayout) findViewById(R.id.footnumber);
            inchlabelh.setText("cm");
            inchlabeld.setText("cm");
            inchlabelw.setText("cm");
            inchlabell.setText("cm");
            inchlabels.setText("cm");
            footlabel.setVisibility(View.INVISIBLE);
            footnumber.setVisibility(View.INVISIBLE);
        }
    }

    public void standardMeasure() {
        setContentView(R.layout.measure_xyz);
        setFixable();
        showAdjustment(View.GONE);
        showAngle(View.GONE);
        makeDigital(R.id.heightinch, R.id.depthinch, R.id.widthinch, R.id.lengthinch, R.id.slantinch, R.id.angleadjust, R.id.anglemeasure, R.id.heightfoot, R.id.depthfoot, R.id.widthfoot, R.id.lengthfoot, R.id.slantfoot);
        displayCM();

        resetNoAd(findViewById(R.id.reset));
        Button Press = (Button) findViewById(R.id.press);
        Press.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (currentStep % 2 == 0) {
                        if (currentStep == 0 && pitchx[1] == 401 && pitchx[4] == 402) {
                            oneSecond.start();
                        }
                        nextStep();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    oneSecond.cancel();
                    if (currentStep % 2 == 1 && currentStep != 9) {
                        nextStep();
                    }
                }
                return true;
            }
        });
        Press.setText(phrase[0]);
        setUsing(true, position == "free");
    }

    public void standardCalibrate() {
        Button Press = (Button) findViewById(R.id.press);
        if (Press != null) {
            Press.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (currentStep % 2 == 0) {
                            if (currentStep == 0 && pitchx[1] == 401 && pitchx[4] == 402 && (getTypedNumber() != 0 || goal != "calibrate")) {
                                oneSecond.start();
                            }
                            nextStep();
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        oneSecond.cancel();
                        if (currentStep % 2 == 1) {
                            nextStep();
                        }
                    }
                    return true;
                }
            });
            Press.setText(phrase[0]);
        }
    }

    public void setFixable() {
        fixable[0] = false;
        fixable[1] = false;
        fixable[2] = false;
        if (type != "length" && type != "height" && type != "angle" && facing != "from you") {
            fixable[0] = true;
            if ((facing == "parallel" && position == "outside") || (facing == "object positions")) {
                fixable[1] = true;
            }
            if (facing == "free") {
                fixable[1] = true;
                fixable[2] = true;
            }
        }
        LinearLayout removeHeightLabel = (LinearLayout) findViewById(R.id.removeHeightLabel);
        LinearLayout removeDepthLabel = (LinearLayout) findViewById(R.id.removeDepthLabel);
        LinearLayout removeWidthLabel = (LinearLayout) findViewById(R.id.removeWidthLabel);
        Button fix = (Button) findViewById(R.id.fix);
        removeHeightLabel.setVisibility(fixable[0] ? View.VISIBLE : View.GONE);
        removeDepthLabel.setVisibility(fixable[1] ? View.VISIBLE : View.GONE);
        removeWidthLabel.setVisibility(fixable[2] ? View.VISIBLE : View.GONE);
        fix.setVisibility((fixable[0] || fixable[1] || fixable[2]) ? View.VISIBLE : View.GONE);
    }

    public int setSign(int value, int place) {
        value %= (int) Math.pow(2, place + 1);
        if (value >= (int) Math.pow(2, place)) {
            return 1;
        } else {
            return -1;
        }
    }

    public void previewSet(View view) {
        EditText typeScreen = (EditText) findViewById(R.id.typeScreen);
        if (!(typeScreen.getText().toString().isEmpty())) {
            accuracy = Integer.parseInt(typeScreen.getText().toString());
            typeScreen.setHint(Integer.toString(accuracy));
        }
    }

    public void setTitle(String title) {
        TextView titlebar = (TextView) findViewById(R.id.title);
        titlebar.setText(title);
    }

    public void setTitleSize(float size) {
        TextView titlebar = (TextView) findViewById(R.id.title);
        titlebar.setTextSize(size);
    }

    public String get_title() {
        TextView titlebar = (TextView) findViewById(R.id.title);
        return (String) titlebar.getText();
    }

    public void setPhrase(String... values) {
        for (int i = 0; i < 9; i++) {
            if (i < values.length * 2 && i % 2 == 0) {
                phrase[i] = values[i / 2];
            } else {
                phrase[i] = "";
            }
        }
        Button Press = (Button) findViewById(R.id.press);
        if (Press != null) {
            Press.setText(phrase[currentStep]);
        }
    }

    public double defaultAngle() {
        switch (position) {
            case "inside":
                return 180;
            case "outside":
                return 0;
            case "hypotenuse":
                return 90;
            default:
                return 0;
        }
    }

    public void viewVisible(int... values) {
        int i;
        View view;
        for (i = 0; i < values.length; i++) {
            view = findViewById(values[i]);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void viewGone(int... values) {
        int i;
        View view;
        for (i = 0; i < values.length; i++) {
            view = findViewById(values[i]);
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    public void setViewMargins(View view, int left, int top, int right, int bottom) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.setMargins((int) (left / dp), (int) (top / dp), (int) (right / dp), (int) (bottom / dp));
        view.setLayoutParams(params);
    }

    // Sensor Functions
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            clickAlarmAcc = Math.max(-1, clickAlarmAcc - 1);
            gData = event.values.clone();
            float valuefix = (float) Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);
            pitchActual = (float) Math.asin(event.values[1] / valuefix) * 180 / (float) Math.PI;
            if (event.values[2] < 0) {
                pitchActual = 180 * Math.signum(pitchActual) - pitchActual;
            }
            float newvalue = lerp(pitch, pitchActual, (float) 1 / (float) accuracy);
            if (Math.signum(pitch - newvalue) == verticalErrorSide) {
                timeacc--;
                if (timeacc <= 0) {
                    pitch = pitchActual;
                    timeacc = fixtime;
                    clickAlarmAcc = fixtime + 2 * accuracy;
                } else {
                    pitch = newvalue;
                }
            } else {
                timeacc = fixtime;
                verticalErrorSide = (int) Math.signum(pitch - newvalue);
            }
            if (goal == "measure" && type == "camera" && imageDimension != null) {
                TextView heightinch = (TextView) findViewById(R.id.heightinch);
                TextView heightfoot = (TextView) findViewById(R.id.heightfoot);
                heightResult = round(cameraMeasure(), .1);
                if (unit.equals("in")) {
                    heightinch.setText(String.format("%1$.1f", Math.abs(heightResult) % 12));
                    heightfoot.setText(String.format("%1$.0f", Math.signum(heightResult) * Math.floor(Math.abs(heightResult) / 12)));
                } else {
                    heightinch.setText(String.format("%1$.1f", heightResult));
                }
            }
        }
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            clickAlarmMag = Math.max(-1, clickAlarmMag - 2);
            mData = event.values.clone();
            if (SensorManager.getRotationMatrix(rMat, iMat, gData, mData)) {
                yawActual = SensorManager.getOrientation(rMat, orientation)[0] * 180 / Math.PI;
            }
            if (Math.abs(pitch) > 90) {
                yawActual += 180;
            }
            if (yawActual < 0) {
                yawActual += 360;
            }
            double newvalue = yaw += angle_difference(yaw, yawActual) * (double) 1 / (double) accuracy;
            if (Math.signum(angle_difference(yaw, yawActual)) == magneticErrorSide) {
                timemag -= 2;
                if (timemag <= 0) {
                    yaw = yawActual;
                    timemag = fixtime;
                    clickAlarmMag = fixtime + 2 * accuracy;
                } else {
                    yaw = newvalue;
                }
                if (yaw < 0) {
                    yaw += 360;
                }
                if (yaw >= 360) {
                    yaw -= 360;
                }
            } else {
                timemag = fixtime;
                magneticErrorSide = (int) Math.signum(angle_difference(yaw, yawActual));
            }
        }

        if (drawPitch) {
            TextView pitchNumber = (TextView) findViewById(R.id.pitchNumber);
            TextView pitchAvgNumber = (TextView) findViewById(R.id.pitchAvgNumber);
            pitchNumber.setText(String.format("%1$.3f", pitchActual));
            pitchAvgNumber.setText(String.format("%1$.3f", pitch));
        }
        if (drawYaw) {
            TextView yawNumber = (TextView) findViewById(R.id.yawNumber);
            TextView yawAvgNumber = (TextView) findViewById(R.id.yawAvgNumber);
            yawNumber.setText(String.format("%1$.3f", yawActual));
            yawAvgNumber.setText(String.format("%1$.3f", yaw));
        }

        if (Math.max(clickAlarmAcc, clickAlarmMag) == 0 && target + " " + type != "incline enter" && type != "camera") {
            if (clickSound == null) {
                clickSound = MediaPlayer.create(this, R.raw.click);
            }
            clickSound.start();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    // On Click Functions
    public void nextStep() {
        Button Press;
        if (findViewById(R.id.press) != null) {
            Press = (Button) findViewById(R.id.press);
        } else {
            Press = (Button) findViewById(R.id.calculate);
        }
        if (goal == "calibrate") {
            if (getTypedNumber() == 0 && target != "incline" && target != "offset" && type != "surface" && type != "load" && type != "2h") {
                currentStep = 0;
                pitchx[0] = 400;
                return;
            }
        }
        if (currentStep != 10) {
            pitchx[currentStep] = pitch - (target == "angle" ? 0 : offset);
            yawx[currentStep] = yaw;
            currentStep++;
            while (pitchx[currentStep] == 401) {
                currentStep++;
            }
            Press.setText(phrase[currentStep]);
        }
        if (pitchx[currentStep] == 402 && currentStep != 9) {
            if (goal == "calibrate") {
                boolean saveable;
                String saveName = "";
                String saveText = "";
                String targetText = "";
                switch (target) {
                    case "eye1":
                    case "eye2": {
                        targetText = "Eye Height";
                        break;
                    }
                    case "stride": {
                        targetText = "Stride Distance";
                        break;
                    }
                    case "phone": {
                        targetText = "Phone Height";
                        break;
                    }
                }
                EditText saveNameView = (EditText) findViewById(R.id.saveName);
                if (saveNameView == null) {
                    saveable = false;
                } else {
                    saveName = saveNameView.getText().toString();
                    if (saveName.isEmpty()) {
                        saveable = false;
                    } else {
                        saveable = true;
                        saveText = "\n" + (nameAvailable(saveName) ? "Saved as \"" + saveName + "\"." : "Error saving. " + targetText + " name \"" + saveName + "\" is already used.");
                    }
                }

                if (getTypedNumber() != 0 || type != "enter" || target == "offset" || target == "incline") {
                    switch (type + " " + target) {
                        case "enter precision": {
                            accuracy2 = (int) getTypedNumber();
                            showToast("Precision set to " + accuracy2 + ".");
                            saveInt("precision", accuracy2);
                            back(false);
                            break;
                        }
                        case "enter eye1": {
                            eye1 = getTypedNumber();
                            showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + "." + saveText);
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "enter eye2": {
                            eye2 = getTypedNumber();
                            showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + "." + saveText);
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "enter stride": {
                            stride = getTypedNumber();
                            showToast("Stride Distance set to " + String.format("%1$.1f", stride) + " " + unit + "." + saveText);
                            saveFloat("stride", stride / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "stride", stride / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "enter cameraHeight": {
                            cameraHeight = getTypedNumber();
                            showToast("Camera Height set to " + String.format("%1$.2f", cameraHeight) + " " + unit + ".");
                            saveFloat("cameraHeight", cameraHeight / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "enter cameraAngle": {
                            cameraAngle = getTypedNumber();
                            showToast("Camera Angle set to " + String.format("%1$.4f", cameraAngle) + "°");
                            saveFloat("cameraAngle", cameraAngle);
                            back(false);
                            break;
                        }
                        case "height cameraAngle":
                        case "length cameraAngle":
                        case "from you cameraAngle": {
                            cameraAngle = calculateCameraAngle();
                            showToast("Camera Angle set to " + String.format("%1$.4f", cameraAngle) + "°");
                            saveFloat("cameraAngle", cameraAngle);
                            back(false);
                            break;
                        }
                        case "height cameraHeight":
                        case "length cameraHeight":
                        case "from you cameraHeight": {
                            cameraHeight = calculateCameraHeight();
                            showToast("Camera Height set to " + String.format("%1$.2f", cameraHeight) + " " + unit + ".");
                            saveFloat("cameraHeight", cameraHeight / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "surface cameraAngle": {
                            cameraAngle = 90 - pitch;
                            showToast("Camera Angle set to " + String.format("%1$.4f", cameraAngle) + "°");
                            saveFloat("cameraAngle", cameraAngle);
                            back(false);
                            break;
                        }
                        case "enter offset": {
                            offset = getTypedNumber();
                            TextView sign = (TextView) findViewById(plusminus);
                            if (sign.getText() == "   -") {
                                offset *= -1;
                            }
                            showToast("Pitch Offset set to " + String.format("%1$.4f", offset) + "°.");
                            saveFloat("offset", offset);
                            back(false);
                            break;
                        }
                        case "load eye1": {
                            Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
                            if (spinner.getSelectedItem().toString() != "Load From:") {
                                eye1 = savedValue();
                                showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + ".");
                                back(false);
                            } else {
                                currentStep = 0;
                                Press.setText(phrase[0]);
                            }
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            break;
                        }
                        case "load eye2": {
                            Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
                            if (spinner.getSelectedItem().toString() != "Load From:") {
                                eye2 = savedValue();
                                showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + ".");
                                back(false);
                            } else {
                                currentStep = 0;
                                Press.setText(phrase[0]);
                            }
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            break;
                        }
                        case "load stride": {
                            Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
                            if (spinner.getSelectedItem().toString() != "Load From:") {
                                stride = savedValue();
                                showToast("Stride Distance set to " + String.format("%1$.1f", stride) + " " + unit + ".");
                                back(false);
                            } else {
                                currentStep = 0;
                                Press.setText(phrase[0]);
                            }
                            saveFloat("stride", stride / (unit.equals("cm") ? 2.54 : 1));
                            break;
                        }
                        case "auto offset": {
                            offset = pitch;
                            showToast("Pitch Offset set to " + String.format("%1$.4f", offset) + "°.");
                            saveFloat("offset", offset);
                            back(false);
                            break;
                        }
                        case "enter incline": {
                            inclineYaw = yaw;
                            incline = getTypedNumber();
                            TextView sign = (TextView) findViewById(plusminus);
                            if (sign.getText() == "   -") {
                                incline *= -1;
                            }
                            showToast("Ground Incline set to " + String.format("%1$.4f", incline) + "°.\nFacing " + angleToDirection(inclineYaw) + ".");
                            saveFloat("incline", incline);
                            saveFloat("inclineYaw", inclineYaw);
                            back(false);
                            break;
                        }
                        case "auto incline": {
                            inclineYaw = yaw;
                            incline = pitch;
                            showToast("Ground Incline set to " + String.format("%1$.4f", incline) + "°.\nFacing " + angleToDirection(inclineYaw) + ".");
                            saveFloat("incline", incline);
                            saveFloat("inclineYaw", inclineYaw);
                            back(false);
                            break;
                        }
                        case "height incline": {
                            inclineYaw = angle_midpoint(yawx[2], yawx[3]);
                            incline = heightToIncline(pitchx[0], pitchx[2], pitchx[3]);
                            showToast("Ground Incline set to " + String.format("%1$.4f", incline) + "°.\nFacing " + angleToDirection(inclineYaw) + ".");
                            saveFloat("incline", incline);
                            saveFloat("inclineYaw", inclineYaw);
                            back(false);
                            break;
                        }
                        case "2h incline": {
                            inclineYaw = angle_midpoint(yawx[0], yawx[1]);
                            incline = m2hToIncline(pitchx[0], pitchx[1]);
                            showToast("Ground Incline set to " + String.format("%1$.4f", incline) + "°.\nFacing " + angleToDirection(inclineYaw) + ".");
                            saveFloat("incline", incline);
                            saveFloat("inclineYaw", inclineYaw);
                            back(false);
                            break;
                        }
                        case "from you incline": {
                            inclineYaw = yaw;
                            incline = youToIncline(pitchx[0]);
                            showToast("Ground Incline set to " + String.format("%1$.4f", incline) + "°.\nFacing " + angleToDirection(inclineYaw) + ".");
                            saveFloat("incline", incline);
                            saveFloat("inclineYaw", inclineYaw);
                            back(false);
                            break;
                        }
                        case "height eye1": {
                            eye1 = heightToEye(pitchx[0], pitchx[2], pitchx[3]);
                            showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + "." + saveText);
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "height eye2": {
                            eye2 = heightToEye(pitchx[0], pitchx[2], pitchx[3]);
                            showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + "." + saveText);
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "2h eye1": {
                            eye1 = m2hToEye(pitchx[0], pitchx[1], pitchx[2]);
                            showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + "." + saveText);
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "2h eye2": {
                            eye2 = m2hToEye(pitchx[0], pitchx[1], pitchx[2]);
                            showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + "." + saveText);
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                    }
                    switch (type + " " + position + " " + target) {
                        case "length from you eye1": {
                            eye1 = youToEye(pitchx[0]);
                            showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + "." + saveText);
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "length inside eye1":
                        case "length outside eye1":
                        case "length hypotenuse eye1":
                        case "length free eye1": {
                            eye1 = freeToEye(pitchx[0], pitchx[2], pitchx[3], (position == "free") ? angle_difference(yawx[3], angle_midpoint(yawx[0], yawx[2])) : defaultAngle());
                            showToast("Eye Height #1 set to " + String.format("%1$.1f", eye1) + " " + unit + "." + saveText);
                            saveFloat("eye1", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye1 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "length from you eye2": {
                            eye2 = youToEye(pitchx[0]);
                            showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + "." + saveText);
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                        case "length inside eye2":
                        case "length outside eye2":
                        case "length hypotenuse eye2":
                        case "length free eye2": {
                            eye2 = freeToEye(pitchx[0], pitchx[2], pitchx[3], (position == "free") ? angle_difference(yawx[3], angle_midpoint(yawx[0], yawx[2])) : defaultAngle());
                            showToast("Eye Height #2 set to " + String.format("%1$.1f", eye2) + " " + unit + "." + saveText);
                            saveFloat("eye2", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            saveRow(saveName, "eye", eye2 / (unit.equals("cm") ? 2.54 : 1));
                            back(false);
                            break;
                        }
                    }
                }
            }
            if (goal == "measure") {
                if (type == "height") {
                    eyeToHeight(pitchx[0], pitchx[2], pitchx[3], position == "free" ? angle_difference(inclineYaw, angle_midpoint(yawx[2], yawx[3])) : 0);
                    currentStep = 9;
                    showResult();
                }
                switch (type + " " + facing + " " + position) {
                    case "length parallel inside":
                    case "length parallel outside":
                    case "length hypotenuse hypotenuse":
                    case "length free free": {
                        eyeToLengthFree(pitchx[0], pitchx[2], pitchx[3], (position == "free") ? angle_midpoint(yawx[0], yawx[2]) : inclineYaw, (position == "free") ? yawx[3] : defaultAngle() + inclineYaw);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "length perpendicular inside":
                    case "length perpendicular outside": {
                        eyeToLengthPerpendicular(pitchx[0], pitchx[1], pitchx[2], pitchx[3], (position == "inside"));
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "length from you from you": {
                        eyeToLengthYou(pitchx[0]);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "length surface surface": {
                        eyeToSurface(pitchx[0], pitchx[1]);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2h parallel inside":
                    case "2h parallel outside":
                    case "2h hypotenuse hypotenuse":
                    case "2h free free": {
                        eyeTo2hFree(pitchx[0], pitchx[1], pitchx[2], pitchx[3], (position == "free") ? angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3])) : defaultAngle(), false);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2h perpendicular inside":
                    case "2h perpendicular outside": {
                        eyeTo2hPerpendicular(pitchx[0], pitchx[2], pitchx[3], pitchx[4], pitchx[6], pitchx[7], (position == "inside"), false);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2h from you from you": {
                        eyeTo2hYou(pitchx[0], pitchx[1]);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                }
                switch (type + " " + facing) {
                    case "2p parallel": {
                        eyeTo2pParallel(pitchx[0], pitchx[1], pitchx[2], pitchx[3], false);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2p perpendicular": {
                        eyeTo2pPerpendicular(pitchx[0], pitchx[1], pitchx[2], pitchx[3], false);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2p free": {
                        eyeTo2pFree(pitchx[0], pitchx[1], pitchx[4], pitchx[5], angle_difference(yawx[2], yawx[0]), angle_difference(yawx[2], yawx[1]), angle_difference(yawx[2], yawx[4]), angle_difference(yawx[2], yawx[5]), false);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "2p object positions": {
                        eyeTo2pObjectPositions(pitchx[0], pitchx[1], pitchx[2], pitchx[3], "not fixing");
                        currentStep = 9;
                        showResult();
                        break;
                    }
                    case "angle parallel":
                    case "angle free": {
                        eyeToAngle(pitchx[0], position == "free" ? yawx[0] : 0, pitchx[1], position == "free" ? yawx[1] : 0);
                        currentStep = 9;
                        showResult();
                        break;
                    }
                }
                LinearLayout fixList = (LinearLayout) findViewById(R.id.fixList);
                fixList.setVisibility(View.VISIBLE);

                if (fixable[0] || fixable[1] || fixable[2]) {
                    Button Fix = (Button) findViewById(R.id.fix);
                    Fix.setVisibility(View.VISIBLE);
                }
                if (findViewById(R.id.reset) != null) {
                    Button Reset = (Button) findViewById(R.id.reset);
                    Reset.setVisibility(View.VISIBLE);
                    Press.setVisibility(View.GONE);
                }

                sensorSwitch(Sensor.TYPE_ACCELEROMETER, false);
                sensorSwitch(Sensor.TYPE_MAGNETIC_FIELD, false);
            }
        }
    }

    public double getTypedNumber() {
        if (goal != "calibrate" || type == "load") {
            return 0;
        }
        EditText typeScreen = (EditText) findViewById(R.id.typeScreen);
        String numString = typeScreen.getText().toString();
        double num = 0;
        double num2 = 0;
        if (!numString.isEmpty()) {
            num = Integer.parseInt(numString);
        }
        if (target == "precision") {
            return num;
        } else {
            EditText typeScreen2 = (EditText) findViewById(R.id.typeScreen2);
            String numString2 = typeScreen2.getText().toString();
            if (!numString2.isEmpty()) {
                num2 = Integer.parseInt(numString2);
            }
            num /= Math.pow(10, (double) numString.length());
            return (num + num2);
        }
    }

    public double getWholeValue(int id) {
        EditText view = (EditText) findViewById(id);
        if (view.getText().toString().isEmpty()) {
            return 0;
        }
        return (double) Integer.parseInt(view.getText().toString());
    }

    public double getDecimalValue(int id) {
        EditText view = (EditText) findViewById(id);
        if (view.getText().toString().isEmpty()) {
            return 0;
        }
        double fullnum = Integer.parseInt(view.getText().toString());
        double numlength = view.getText().length();
        return fullnum / Math.pow(10, numlength);
    }

    public void reset(View view) {
        resetNoAd(view);
        if (!purchases[0] && chance(.1)) {
            Intent intent = new Intent(this, OnlineAdActivity.class);
            startActivity(intent);
        }
    }

    public void resetNoAd(View view) {
        LinearLayout fixList = (LinearLayout) findViewById(R.id.fixList);
        fixList.setVisibility(View.GONE);
        Button Fix = (Button) findViewById(R.id.fix);
        Fix.setVisibility(View.GONE);
        Button Press = (Button) findViewById(R.id.press);
        Press.setVisibility(View.VISIBLE);
        currentStep = 0;
        int i;
        for (i = 0; i < 8; i++) {
            if (pitchx[i] < 400) {
                pitchx[i] = 400;
            }
        }
        skipped = false;
        CheckBox removeHeightBox = (CheckBox) findViewById(R.id.removeHeightBox);
        CheckBox removeWidthBox = (CheckBox) findViewById(R.id.removeWidthBox);
        CheckBox removeDepthBox = (CheckBox) findViewById(R.id.removeDepthBox);
        removeHeightBox.setChecked(false);
        removeWidthBox.setChecked(false);
        removeDepthBox.setChecked(false);
        view.setVisibility(View.GONE);
        heightResult = 0;
        depthResult = 0;
        widthResult = 0;
        lengthResult = 0;
        slantResult = 0;
        angleResult = 0;
        adjustmentResult = 0;
        showResult();
        Press.setText(phrase[0]);
        sensorSwitch(Sensor.TYPE_ACCELEROMETER, accUsing);
        sensorSwitch(Sensor.TYPE_MAGNETIC_FIELD, magUsing);
    }

    public void changeUnit(View view) {
        if (unit.equals("in")) {
            unit = "cm";
            eye1 *= 2.54;
            eye2 *= 2.54;
            stride *= 2.54;
            cameraHeight *= 2.54;
        } else {
            unit = "in";
            eye1 /= 2.54;
            eye2 /= 2.54;
            stride /= 2.54;
            cameraHeight /= 2.54;
        }
        ((Button) view).setText(unit);
        TextView unit2 = (TextView) findViewById(R.id.unit2);
        TextView currentValue = (TextView) findViewById(R.id.currentValue);
        if (unit2 != null) {
            unit2.setText(unit);
        }
        double newVal = 0;
        if (currentValue != null) {
            switch (target) {
                case "eye1": {
                    newVal = eye1;
                    break;
                }
                case "eye2": {
                    newVal = eye2;
                    break;
                }
                case "stride": {
                    newVal = stride;
                    break;
                }
                case "cameraHeight": {
                    newVal = cameraHeight;
                    break;
                }
            }
            currentValue.setText(String.format(target == "cameraHeight" ? "%1$.2f" : "%1$.1f", newVal));
        }
        saveString("unit", unit);
    }

    public void plusMinus(View view) {
        if (((TextView) view).getText() == "  +") {
            ((TextView) view).setText("   -");
        } else {
            ((TextView) view).setText("  +");
        }
    }

    public void back(boolean showAd) {
        closeCamera();
        setContentView(R.layout.start_screen);
        if (purchases[0]) {
            viewGone(R.id.fullVersion);
        }
        if (!testing) {
            viewGone(R.id.checklist, R.id.measureTest);
        }
        if (!purchases[0] || !testing) {
            viewGone(R.id.consume);
        }

        setUsing(false, false);
        negativeAllowed = false;
        accuracy = accuracy2;
        drawPitch = false;
        drawYaw = false;
        currentStep = 0;
        int i;
        for (i = 0; i < 9; i++) {
            pitchx[i] = 402;
        }
        heightResult = 0;
        depthResult = 0;
        widthResult = 0;
        lengthResult = 0;
        slantResult = 0;
        angleResult = 0;
        adjustmentResult = 0;

        if (!purchases[0] && showAd && chance(.3)) {
            Intent intent = new Intent(this, OnlineAdActivity.class);
            startActivity(intent);
        }
    }

    public void nextScreen(View view) {
        switch (view.getId()) {
            case (R.id.bugReporting): {
                setContentView(R.layout.bug_reporting);
                break;
            }
            case (R.id.consume): {
                consumeItem(0);
                break;
            }
            case (R.id.sendEmail): {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jffitch@mathgeniusguide.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Tape Measure App Bug");
                try {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    showToast("There are no email clients installed.");
                }
                break;
            }
            case (R.id.checklist): {
                Intent intent = new Intent(this, ChecklistActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.examples):
            case (R.id.examples2): {
                Intent intent = new Intent(this, ExamplesActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.requirements): {
                Intent intent = new Intent(this, RequirementsActivity.class);
                startActivity(intent);
                break;
            }
            case (R.id.back): {
                back(true);
                break;
            }
            case (R.id.calibrate): {
                goal = "calibrate";
                setContentView(R.layout.calibrate);
                gray(R.id.incline, R.id.eye2, R.id.stride);
                break;
            }
            case (R.id.measureTest): {
                int i;
                goal = "measure";
                setContentView(R.layout.measure_test);
                setUpTestSpinner();
                makeDigital(R.id.heightinch, depthinch, widthinch, lengthinch, slantinch, R.id.angleadjust, R.id.anglemeasure, heightfoot, depthfoot, widthfoot, lengthfoot, slantfoot);
                displayCM();
                for (i = 0; i < 8; i++) {
                    makeDigitalEditText(viewId("pitch" + i + "Decimal"), viewId("pitch" + i + "Whole"), viewId("yaw" + i + "Decimal"), viewId("yaw" + i + "Whole"));
                }
                makeDigitalEditText(R.id.eye1Whole, R.id.eye1Decimal, R.id.eye2Whole, R.id.eye2Decimal, R.id.strideWhole, R.id.strideDecimal, R.id.phoneWhole, R.id.phoneDecimal, R.id.offsetWhole, R.id.offsetDecimal, R.id.inclinePitchWhole, R.id.inclinePitchDecimal, R.id.inclineYawWhole, R.id.inclineYawDecimal);
                setUpTestCalibration();
                break;
            }
            case (R.id.measure): {
                goal = "measure";
                if (!purchases[0]) {
                    incline = 0;
                }
                setContentView(R.layout.measure);
                gray(R.id.measure2h, R.id.measure2p);
                break;
            }
            case (R.id.instructions): {
                setContentView(R.layout.instructions);
                break;
            }
            case (R.id.fullVersion): {
                setContentView(R.layout.full_version);
                break;
            }
            case (R.id.fullVersion2): {
                purchaseItem(0);
                break;
            }
            case (R.id.precision): {
                type = "enter";
                target = "precision";
                setContentView(R.layout.set_precision);
                makeDigital(R.id.yawAvgNumber, R.id.yawNumber, R.id.pitchAvgNumber, R.id.pitchNumber);
                makeDigitalEditText(R.id.typeScreen);
                EditText typeScreen = (EditText) findViewById(R.id.typeScreen);
                accuracy2 = accuracy;
                typeScreen.setHint(Integer.toString(accuracy));
                drawPitch = true;
                drawYaw = true;
                setPhrase("The \"Pitch\" and \"Yaw\" values show the values received by your phone's sensors. The \"Pitch Avg\" and \"Yaw Avg\" values show the values that will be used when measuring.\nA higher precision means the average values will change slower, making the measurements more accurate but requiring you to stay still for a longer period of time.\nAfter entering desired Precision value, press to set Precision. Press the \"SET\" button to see the preview.");
                maxStep(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.stride): {
                if (!purchases[0]) {
                    showToast("Stride Distance is only available in the Full Version.");
                    break;
                }
                target = "stride";
                setContentView(R.layout.enter_or_load);
                break;
            }
            case (R.id.cameraAngle): {
                target = "cameraAngle";
                stringForm = "Angle";
                setContentView(R.layout.camera_angle);
                break;
            }
            case (R.id.cameraHeight): {
                target = "cameraHeight";
                stringForm = "Height";
                setContentView(R.layout.camera_angle);
                viewGone(R.id.cameraByLevelSurface);
                ((TextView) findViewById(R.id.cameraEnter)).setText("Enter Camera Height");
                setTitle("Set Camera Height");
                break;
            }
            case (R.id.cameraEnter): {
                type = "enter";
                if (target == "cameraAngle") {
                    setContentView(R.layout.set_angle);
                    TextView currentValue = (TextView) findViewById(R.id.currentValue);
                    currentValue.setText(String.format("%1$.4f", cameraAngle));
                    viewGone(plusminus);
                } else {
                    setContentView(R.layout.enter_cameraheight);
                    TextView currentValue = (TextView) findViewById(R.id.currentValue);
                    currentValue.setText(String.format("%1$.2f", cameraHeight));
                    Button unitView = (Button) findViewById(R.id.unit);
                    unitView.setText(unit);
                    TextView unit2 = (TextView) findViewById(R.id.unit2);
                    unit2.setText(unit);
                }
                makeDigital(R.id.currentValue);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Camera " + stringForm + "\nBy Entering " + stringForm);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Camera\n" + stringForm + ":");
                setPhrase((target == "cameraAngle" ? cameraAngleText : cameraHeightText) + "After entering desired Camera " + stringForm + ", press to set Camera " + stringForm + ".");
                maxStep(1);
                standardCalibrate();
                break;
            }
            case (R.id.cameraByHeight): {
                type = "height";
                position = "outside";
                facing = "parallel";
                setContentView(R.layout.set_cameraangle);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Camera " + stringForm + "\nBy Object Height");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Height:");
                setPhrase((target == "cameraAngle" ? cameraAngleText : cameraHeightText) + cameraByHeightText1 + stringForm + cameraByHeightText2);
                maxStep(1);
                standardCalibrate();
                textureView = (TextureView) findViewById(R.id.cameraPreview);
                textureView.setSurfaceTextureListener(textureListener);
                setUsing(true, false);
                break;
            }
            case (R.id.cameraByLength): {
                type = "length";
                position = "outside";
                facing = "parallel";
                setContentView(R.layout.set_cameraangle);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Camera " + stringForm + "\nBy Object Length");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Length:");
                setPhrase((target == "cameraAngle" ? cameraAngleText : cameraHeightText) + cameraByLengthText1 + stringForm + cameraByLengthText2);
                maxStep(1);
                standardCalibrate();
                textureView = (TextureView) findViewById(R.id.cameraPreview);
                textureView.setSurfaceTextureListener(textureListener);
                setUsing(true, false);
                break;
            }
            case (R.id.cameraByFromYou): {
                type = "from you";
                position = "from you";
                facing = "from you";
                setContentView(R.layout.set_cameraangle);
                viewGone(R.id.cameraBottom);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Camera " + stringForm + "\nBy Distance From You");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Distance:");
                setPhrase((target == "cameraAngle" ? cameraAngleText : cameraHeightText) + cameraByFromYouText1 + stringForm + cameraByFromYouText2);
                maxStep(1);
                standardCalibrate();
                textureView = (TextureView) findViewById(R.id.cameraPreview);
                textureView.setSurfaceTextureListener(textureListener);
                setUsing(true, false);
                break;
            }
            case (R.id.cameraByLevelSurface): {
                type = "surface";
                setContentView(R.layout.set_cameraangle);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Camera Angle\nBy Level Surface");
                viewGone(R.id.enterRow, R.id.settingLabel, R.id.cameraBottom);
                setPhrase(cameraAngleText + cameraByLevelSurfaceText);
                maxStep(1);
                standardCalibrate();
                textureView = (TextureView) findViewById(R.id.cameraPreview);
                textureView.setSurfaceTextureListener(textureListener);
                setUsing(true, false);
                break;
            }
            case (R.id.enter): {
                type = "enter";
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Stride Distance");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView unit2 = (TextView) findViewById(R.id.unit2);
                unit2.setText(unit);
                TextView currentValue = (TextView) findViewById(R.id.currentValue);
                currentValue.setText(String.format("%1$.1f", stride));
                makeDigital(R.id.currentValue);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Stride\nDistance:");
                setPhrase(strideUses + "After entering desired Stride Distance value, press to set Stride Distance." + saveText2);
                maxStep(1);
                standardCalibrate();
                break;
            }
            case (R.id.loadSaved): {
                type = "load";
                setContentView(R.layout.load_saved);
                switch (target) {
                    case "eye1": {
                        setTitle("Loading Saved\nEye Height #1");
                        setPhrase(eye1Uses + "After selecting which Eye Height #1 value to load, press to set Eye Height #1." + deleteText);
                        standardCalibrate();
                        break;
                    }
                    case "eye2": {
                        setTitle("Loading Saved\nEye Height #2");
                        setPhrase(eye2Uses + "After selecting which Eye Height #2 value to load, press to set Eye Height #2." + deleteText);
                        standardCalibrate();
                        break;
                    }
                    case "stride": {
                        setTitle("Loading Saved\nStride Distance");
                        setPhrase(strideUses + "After selecting which Stride Distance value to load, press to set Stride Distance." + deleteText);
                        standardCalibrate();
                        break;
                    }
                }
                maxStep(1);
                setUpSaveSpinner();
                break;
            }
            case (R.id.eyeEnter): {
                type = "enter";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Entering Eye Height");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView unit2 = (TextView) findViewById(R.id.unit2);
                unit2.setText(unit);
                TextView currentValue = (TextView) findViewById(R.id.currentValue);
                makeDigital(R.id.currentValue);
                if (eyeset == 1) {
                    currentValue.setText(String.format("%1$.1f", eye1));
                }
                if (eyeset == 2) {
                    currentValue.setText(String.format("%1$.1f", eye2));
                }
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "After entering desired Eye Height #" + eyeset + " value, press to set Eye Height #" + eyeset + "." + saveText2);
                maxStep(1);
                standardCalibrate();
                break;
            }
            case (R.id.eyeHeight): {
                type = "height";
                position = "outside";
                facing = "parallel";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Object Height");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nHeight:");
                viewGone(R.id.currentRow);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "After entering desired Object Height value, aim at the ground below the object, then press and release. If the object is on the ground, you may instead aim at the ground and press and hold, then aim at the top of the object and release." + saveText2, "Aim at the bottom of the object and press and hold, then aim at the top of the object and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.eyeInside): {
                type = "length";
                position = "inside";
                facing = "parallel";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Object Length Inside");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nLength:");
                viewGone(R.id.currentRow);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "For this to work, one of the object's end points must be directly in front of you, and the other must be directly behind you. After entering desired Object Length value, aim at the ground below the first end point, then press and release. If the object is on the ground, you may instead aim at the first end point and press and hold, then aim at the second end point and release." + saveText2, "Aim at the first end point and press and hold, then aim aim at the second end point and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.eyeHypotenuse): {
                type = "length";
                position = "hypotenuse";
                facing = "hypotenuse";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + " By\nObject Length Hypotenuse");
                setTitleSize(30);
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nLength:");
                RelativeLayout currentRow = (RelativeLayout) findViewById(R.id.currentRow);
                currentRow.setVisibility(View.GONE);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "For this to work, one of the object's end points must be directly in front of you, and the other must be directly to your left or right side. After entering desired Object Length value, aim at the ground below the first end point, then press and release. If the object is on the ground, you may instead aim at the first end point and press and hold, then aim at the second end point and release." + saveText2, "Aim at the first end point and press and hold, then aim aim at the second end point and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.eyeOutside): {
                type = "length";
                position = "outside";
                facing = "parallel";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Object Length Outside");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nLength:");
                viewGone(R.id.currentRow);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "For this to work, both of the object's end points must be directly in front of you. After entering desired Object Length value, aim at the ground below the first end point, then press and release. If the object is on the ground, you may instead aim at the first end point and press and hold, then aim at the second end point and release" + saveText2, "Aim at the first end point and press and hold, then aim aim at the second end point and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.eyeFree): {
                type = "length";
                position = "free";
                facing = "free";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Object Length Free");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nLength:");
                viewGone(R.id.currentRow);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "After entering desired Object Length value, aim at the ground below the first end point, then press and release. If the object is on the ground, you may instead aim at the first end point and press and hold, then aim at the second end point and release" + saveText2, "Aim at the first end point and press and hold, then aim aim at the second end point and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.eyeYou): {
                type = "length";
                position = "from you";
                facing = "from you";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Eye Height #" + eyeset + "\nBy Distance From You");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Distance:");
                viewGone(R.id.currentRow);
                setPhrase((eyeset == 1 ? eye1Uses : eye2Uses) + "After entering desired Distance From You, aim at the ground that distance away and press to set Eye Height #" + eyeset + "." + saveText2);
                maxStep(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.eye2h): {
                if (!purchases[0]) {
                    showToast("Setting Eye Height By Two Other Eye Heights is only available in the Full Version.");
                    break;
                }
                type = "2h";
                target = "eye" + eyeset;
                setContentView(R.layout.set_eyeheight);
                viewGone(R.id.typeScreenRow, R.id.settingLabel, R.id.currentRow);
                setTitle("Setting Eye Height #" + eyeset + " By\nTwo Other Eye Heights");
                setPhrase("You will need to have Eye Heights #1 and #2 set before using this method, which will change Eye Height #" + eyeset + ".\n" + (eyeset == 1 ? eye1Uses : eye2Uses) + "From your currently set Eye Height #1, aim at any point and press and hold. Then, move to your currently set Eye Height #2 and aim at the same point, and release." + saveText2, "From the desired new Eye Height #" + eyeset + ", aim at the same point, and press and release.");
                maxStep(3);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.offsetManual): {
                target = "offset";
                type = "enter";
                setContentView(R.layout.set_angle);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                TextView currentValue = (TextView) findViewById(R.id.currentValue);
                currentValue.setText(String.format("%1$.4f", offset));
                makeDigital(R.id.currentValue);
                setPhrase(offsetUses + "After entering desired Pitch Offset value, press to set Pitch Offset.");
                maxStep(1);
                standardCalibrate();
                break;
            }
            case (R.id.inclineManual): {
                target = "incline";
                type = "enter";
                setContentView(R.layout.set_angle);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Incline Angle\nManually");
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Incline\nAngle:");
                TextView currentValue = (TextView) findViewById(R.id.currentValue);
                currentValue.setText(String.format("%1$.4f", incline));
                makeDigital(R.id.currentValue);
                setPhrase(inclineUses + "After entering desired Ground Incline value, press to set Ground Incline.");
                maxStep(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.inclineAuto): {
                type = "auto";
                setContentView(R.layout.set_angle);
                viewGone(R.id.typeScreen, R.id.typeScreen2, R.id.settingLabel, R.id.degreeSign, R.id.decimalPoint, R.id.plusminus, R.id.currentRow);
                setTitle("Setting Incline Angle\nBy Phone On Ground");
                setPhrase("This method of setting the Ground Incline must only be used if the incline is uniform.\n" + inclineUses + "Place your phone on the ground facing the direction you'll be aiming when measuring, then press to set Ground Incline.");
                maxStep(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.incline2h): {
                type = "2h";
                setContentView(R.layout.set_angle);
                viewGone(R.id.typeScreen, R.id.typeScreen2, R.id.settingLabel, R.id.degreeSign, R.id.decimalPoint, R.id.plusminus);
                setTitle("Setting Incline Angle\nBy Two Eye Heights");
                viewGone(R.id.currentRow);
                setPhrase(inclineUses + "Make sure you have already set your desired Eye Height #1 and Eye Height #2. Aim at a point on the ground and press and hold, then move to Eye Height #2 and release.");
                maxStep(2);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.inclineFromYou): {
                type = "from you";
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Incline Angle\nBy Distance From You");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Distance:");
                viewGone(R.id.currentRow);
                setPhrase(inclineUses + "Make sure you have already set your desired Eye Height #1. After entering desired Distance From You, aim at a point on the ground that distance away facing the direction you'll be aiming when measuring, then press to set Ground Incline.");
                maxStep(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.inclineHeight): {
                type = "height";
                setContentView(R.layout.set_eyeheight);
                makeDigitalEditText(R.id.typeScreen, R.id.typeScreen2);
                setTitle("Setting Incline Angle By\nKnown Object Height");
                Button unitView = (Button) findViewById(R.id.unit);
                unitView.setText(unit);
                TextView settingLabel = (TextView) findViewById(R.id.settingLabel);
                settingLabel.setText("Object\nHeight:");
                viewGone(R.id.currentRow);
                setPhrase(inclineUses + "Make sure you have already set your desired Eye Height #1. After entering desired Object Height, aim at the point on the ground directly below the object, and press and release. If the object is on the ground, you may instead aim at the ground and press and hold, then aim at the top of the object and release.", "Aim at the bottom of the object and press and hold, then aim at the top of the object and release.");
                maxStep(4);
                skip(1);
                standardCalibrate();
                setUsing(true, true);
                break;
            }
            case (R.id.offsetAuto): {
                type = "auto";
                setContentView(R.layout.set_angle);
                viewGone(R.id.typeScreen, R.id.typeScreen2, R.id.settingLabel, R.id.degreeSign, R.id.decimalPoint, R.id.plusminus, R.id.currentRow);
                setTitle("Setting Pitch Offset\nAutomatically");
                setPhrase(offsetUses + "Aim at something the same height as your eyes, such as your eyes in a perfectly vertical mirror, then press to set Pitch Offset.");
                maxStep(1);
                standardCalibrate();
                setUsing(true, false);
                break;
            }
            case (R.id.measureLength): {
                goal = "measure";
                type = "length";
                setContentView(R.layout.measure_length);
                break;
            }
            case (R.id.measure2h): {
                if (!purchases[0]) {
                    showToast("Measure From Two Heights is only available in the Full Version.");
                    break;
                }
                goal = "measure";
                type = "2h";
                setContentView(R.layout.measure_2h);
                break;
            }
            case (R.id.measure2p): {
                if (!purchases[0]) {
                    showToast("Measure From Two Positions is only available in the Full Version.");
                    break;
                }
                goal = "measure";
                type = "2p";
                setContentView(R.layout.measure_2p);
                break;
            }
            case (R.id.measureAngle): {
                goal = "measure";
                type = "angle";
                setContentView(R.layout.measure_angle);
                break;
            }
            case (R.id.measureAngleVertical): {
                standardMeasure();
                facing = "parallel";
                position = "outside";
                showLength(View.GONE);
                showWidth(View.GONE);
                showDepth(View.GONE);
                showHeight(View.GONE);
                showSlant(View.GONE);
                showAngle(View.VISIBLE);
                setTitle("Measuring Angle\nVertical Only");
                setPhrase("You have chosen to only measure the angle in the vertical direction, so your phone will not detect any horizontal rotation.\n\nHold your phone so the side of your phone is parallel to one side of the angle, then press and hold. Rotate your phone so the side of your phone is now parallel to the other side of the angle, then release.");
                maxStep(2);
                setUsing(true, false);
                break;
            }
            case (R.id.measureAngleFree): {
                standardMeasure();
                facing = "free";
                position = "free";
                showLength(View.GONE);
                showWidth(View.GONE);
                showDepth(View.GONE);
                showHeight(View.GONE);
                showSlant(View.GONE);
                showAngle(View.VISIBLE);
                setTitle("Measuring Angle\nFree");
                setPhrase("Hold your phone so the side of your phone is parallel to one side of the angle, then press and hold. Rotate your phone so the side of your phone is now parallel to the other side of the angle, then release.");
                maxStep(2);
                setUsing(true, true);
                break;
            }
            case (R.id.eye1): {
                setContentView(R.layout.eye);
                gray(R.id.eye2h);
                TextView view1 = (TextView) findViewById(R.id.title);
                eyeset = 1;
                target = "eye1";
                view1.setText("Set Eye Height #1");
                break;
            }
            case (R.id.eye2): {
                if (!purchases[0]) {
                    showToast("Eye Height #2 is only available in the Full Version.");
                    break;
                }
                setContentView(R.layout.eye);
                TextView view1 = (TextView) findViewById(R.id.title);
                eyeset = 2;
                target = "eye2";
                view1.setText("Set Eye Height #2");
                break;
            }
            case (R.id.offset): {
                target = "offset";
                setContentView(R.layout.offset);
                break;
            }
            case (R.id.incline): {
                if (!purchases[0]) {
                    showToast("Ground Incline is only available in the Full Version.");
                    break;
                }
                target = "incline";
                setContentView(R.layout.incline);
                break;
            }
            case (R.id.measure2hParallelInside): {
                position = "inside";
                facing = "parallel";
                standardMeasure();
                showLength(View.GONE);
                showWidth(View.GONE);
                setTitle("Measuring 2Ht\nParallel Inside");
                setPhrase("For this to work, one of the object's end points must be directly in front of you, and the other must be directly behind you. Aim at the object's first point and press and hold, then aim at the second point and release.", "Move to Eye Height #2, and do the same as before. Aim at the object's first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hParallelOutside): {
                position = "outside";
                facing = "parallel";
                standardMeasure();
                showLength(View.GONE);
                showWidth(View.GONE);
                setTitle("Measuring 2Ht\nParallel Outside");
                setPhrase("For this to work, both of the object's end points must be directly in front of you. Aim at the object's first point and press and hold, then aim at the second point and release.", "Move to Eye Height #2, and do the same as before. Aim at the object's first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hHypotenuse): {
                position = "hypotenuse";
                facing = "hypotenuse";
                standardMeasure();
                setTitle("Measuring 2Ht\nHypotenuse");
                setPhrase("For this to work, one of the object's end points must be directly in front of you, and the other must be directly to either your left or right side. Aim at the object's first point and press and hold, then aim at the second point and release.", "Move to Eye Height #2, and do the same as before. Aim at the object's first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hPerpendicularInside): {
                position = "inside";
                facing = "perpendicular";
                standardMeasure();
                showLength(View.GONE);
                showDepth(View.GONE);
                setTitle("Measuring 2Ht\nPerpendicular Inside");
                setPhrase("For this to work, if you were to face perpendicular to the direction between the object's end points, you'd be facing a point between them. Aim at the Perpendicular Point (can be any height) and press and release.", "Aim at one of the object's end points and press and hold. Aim at the other end point and release.", "Switch to Eye Height #2 and do the same as before. Aim at the Perpendicular Point (this time, must be the same height as the previously chosen Perpendicular Point) and press and release.", "Aim at one of the object's end points and press and hold. Aim at the other end point and release.");
                maxStep(8);
                skip(1);
                skip(5);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hPerpendicularOutside): {
                position = "outside";
                facing = "perpendicular";
                standardMeasure();
                showLength(View.GONE);
                showDepth(View.GONE);
                setPhrase("For this to work, if you were to face perpendicular to the direction between the object's end points, you'd be facing a point outside of them. Aim at the Perpendicular Point (can be any height) and press and release.", "Aim at one of the object's end points and press and hold. Aim at the other end point and release.", "Switch to Eye Height #2 and do the same as before. Aim at the Perpendicular Point (this time, must be the same height as the previously chosen Perpendicular Point) and press and release.", "Aim at one of the object's end points and press and hold. Aim at the other end point and release.");
                maxStep(8);
                setTitle("Measuring 2Ht\nPerpendicular Outside");
                skip(1);
                skip(5);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hFromYou): {
                position = "from you";
                facing = "from you";
                standardMeasure();
                showWidth(View.GONE);
                showLength(View.GONE);
                setTitle("Measuring 2Ht\nFrom You");
                setPhrase("Aim at the point you're measuring the distance to and press and hold, then switch to Eye Height #2 and aim at the same point and release.\n\nThe Depth is the distance from you to the point, and the Height is the difference between the point's altitude and the Ground's altitude at your position, with the Ground being your set Eye Height below your eyes.");
                maxStep(2);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2hFree): {
                position = "free";
                facing = "free";
                standardMeasure();
                setTitle("Measuring\nTwo-Heights Free");
                setPhrase("Aim at the object's first point and press and hold, then aim at the second point and release.", "Move to Eye Height #2, and do the same as before. Aim at the object's first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, true);
                break;
            }
            case (R.id.measure2pStrideParallel): {
                facing = "parallel";
                standardMeasure();
                showLength(View.GONE);
                showWidth(View.GONE);
                setTitle("Measuring 2Pos\nStride Parallel");
                setPhrase("For this to work, from your first position, one of the object's end points must be directly in front of you, and the other must be directly either in front of or behind you. Also, after moving, the first point must remain in front of you, and the second point must remain on the same side of you (remain in front or remain behind). Aim at the first point and press and hold, then aim at the second point and release.", "Move a distance equal to your Stride Distance either directly toward or away from the first point, then do the same as before. Aim at the first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2pStridePerpendicular): {
                facing = "perpendicular";
                position = "inside";
                standardMeasure();
                showLength(View.GONE);
                showDepth(View.GONE);
                setPhrase("For this to work, from your first position, the object's first end point must be directly in front of you, and the second end point must be directly to the left or right side of the first end point. Aim at the first point and press and hold, then aim at the second point and release.", "Move a distance equal to your Stride Distance toward or away from the first point, then do the same as before. Aim at the first point and press and hold, then aim at the second point and release.");
                setTitle("Measuring 2Pos\nStride Perpendicular");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measure2pStrideFree): {
                facing = "free";
                position = "free";
                standardMeasure();
                setTitle("Measuring Two\nPositions Free");
                setPhrase("Aim at the object's first end point and press and hold, then aim at the second point and release.", "Aim at any height in the direction you'll be moving, then move a distance equal to your Stride Distance in that direction.", "Aim at the object's first end point and press and hold, then aim at the second point and release.");
                maxStep(6);
                skip(3);
                setUsing(true, true);
                break;
            }
            case (R.id.measure2pObjectPositions): {
                facing = "object positions";
                position = "inside";
                standardMeasure();
                showLength(View.GONE);
                showWidth(View.GONE);
                setTitle("Measuring 2Pos\nObject Positions");
                setPhrase("Unlike most methods of measuring from two positions, this method requires an Eye Height #1. Stand at the position of one of the object's end points. Aim at the ground at the other end point and press and hold, then aim at the end point and release.\n\nIf you're measuring a vertical object and plan on clicking Remove Depth, start at either the object's position or the Spectator Position. If you're at the object's position, aim at the ground at the Spectator Position and press and release. If you're at the Spectator Position, aim at the bottom of the object and press, then aim at the top of the object and release.", "Move to the position of the other end point, and do the same as before. Aim at the ground at the first end point and press and hold, then aim at the end point and release.\n\nIf you're measuring a vertical object and plan on clicking Remove Depth, move to the other position (move to the object's position if you were at the Spectator Position, and vice versa). If you're at the object's position, aim at the ground at the Spectator Position and press and release. If you're at the Spectator Position, aim at the bottom of the object and press, then aim at the top of the object and release. If you're using this option, be sure to click Remove Depth after measuring.");
                maxStep(4);
                negativeAllowed = true;
                setUsing(true, false);
                break;
            }
            case (R.id.measureHeight): {
                type = "height";
                position = "outside";
                facing = "outside";
                if (isBetween(-.00004, incline, .00004)) {
                    standardMeasure();
                    showDepth(View.GONE);
                    showLength(View.GONE);
                    showWidth(View.GONE);
                    showSlant(View.GONE);
                    setTitle("Measuring\nHeight");
                    setPhrase("Aim at the ground below the object, then press and release. If the object is on the ground, you may instead aim at the bottom of the object and press and hold, then aim at the top of the object and release.", "Aim at the bottom of the object and press and hold, then aim at the top of the object and release.");
                    maxStep(4);
                    skip(1);
                } else {
                    setContentView(R.layout.measure_height);
                    ((TextView) findViewById(R.id.measureHeightWarning)).setText("Because you have the Ground Incline set to something other than 0, you need to specify the direction you'll be facing relative to the direction of the Incline. If you choose \"Assume Same Incline Direction\", the device will assume you're facing the same direction that you were facing when you set the Ground Incline (" + angleToDirection(inclineYaw) + "). If you are not facing the same direction as you were when you set the Ground Incline, you should choose \"Use Magnetic Sensor To Determine Incline Direction\".\n\n");
                }
                setUsing(true, false);
                break;
            }
            case (R.id.measureHeightMagneticSensor): {
                position = "free";
                facing = "free";
                setUsing(true, true);
            }
            case (R.id.measureHeightInclineDirection): {
                standardMeasure();
                showDepth(View.GONE);
                showLength(View.GONE);
                showWidth(View.GONE);
                showSlant(View.GONE);
                setTitle("Measuring\nHeight");
                setPhrase("Aim at the ground below the object, then press and release. If the object is on the ground, you may instead aim at the bottom of the object and press and hold, then aim at the top of the object and release.", "Aim at the bottom of the object and press and hold, then aim at the top of the object and release.");
                maxStep(4);
                skip(1);
                break;
            }
            case (R.id.measureLengthParallelInside): {
                position = "inside";
                facing = "parallel";
                standardMeasure();
                showAdjustment(View.GONE);
                showDepth(View.GONE);
                showWidth(View.GONE);
                if (incline == 0) {
                    showHeight(View.GONE);
                    showSlant(View.GONE);
                }
                setTitle("Measuring Length\nParallel Inside");
                setPhrase("For this to work, one of the object's end points must be directly in front of you, and the other must be directly behind you. Aim at the ground below the object's first end point, then press and release. If the object is on the ground, you may instead aim at the first point and press and hold, then aim at the second point and release.", "Aim at the first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                skip(1);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthHypotenuse): {
                position = "hypotenuse";
                facing = "hypotenuse";
                standardMeasure();
                showAdjustment(View.GONE);
                if (incline == 0) {
                    showHeight(View.GONE);
                    showSlant(View.GONE);
                }
                setTitle("Measuring Length\nHypotenuse");
                setPhrase("For this to work, one of the object's end points must be directly in front of you, and the other must be directly to your left or right side. Aim at the ground below the object's first end point, then press and release. If the object is on the ground, you may instead aim at the first point and press and hold, then aim at the second point and release.", "Aim at the first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                skip(1);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthParallelOutside): {
                position = "outside";
                facing = "parallel";
                standardMeasure();
                showAdjustment(View.GONE);
                showDepth(View.GONE);
                showWidth(View.GONE);
                if (incline == 0) {
                    showHeight(View.GONE);
                    showSlant(View.GONE);
                }
                setTitle("Measuring Length\nParallel Outside");
                setPhrase("For this to work, both of the object's end points must be directly in front of you. Aim at the ground below the object's first end point, then press and release. If the object is on the ground, you may instead aim at the first point and press and hold, then aim at the second point and release.", "Aim at the first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                skip(1);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthPerpendicularInside): {
                position = "inside";
                facing = "perpendicular";
                standardMeasure();
                showAdjustment(View.GONE);
                showDepth(View.GONE);
                showHeight(View.GONE);
                showWidth(View.GONE);
                showSlant(View.GONE);
                setTitle("Measuring Length\nPerpendicular Inside");
                setPhrase("For this to work, if you were to face perpendicular to the direction between the object's end points, you'd be facing a point between them. That point is the Perpendicular point. Aim at the ground below the Perpendicular Point and press and hold, then aim at the Perpendicular Point and release.", "Aim at the object's first end point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthPerpendicularOutside): {
                position = "outside";
                facing = "perpendicular";
                standardMeasure();
                showAdjustment(View.GONE);
                showDepth(View.GONE);
                showHeight(View.GONE);
                showWidth(View.GONE);
                showSlant(View.GONE);
                setTitle("Measuring Length\nPerpendicular Outside");
                setPhrase("For this to work, if you were to face perpendicular to the direction between the object's end points, you'd be facing a point outside of them. That point is the Perpendicular point. Aim at the ground below the Perpendicular Point and press and hold, then aim at the Perpendicular Point and release.", "Aim at the object's first end point and press and hold, then aim at the second point and release.");
                maxStep(4);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthFromYou): {
                position = "from you";
                facing = "from you";
                standardMeasure();
                showAdjustment(View.GONE);
                showDepth(View.GONE);
                showHeight(View.GONE);
                showWidth(View.GONE);
                showSlant(View.GONE);
                setTitle("Measuring\nDistance From You");
                setPhrase("Aim at a point on the ground, then press to calculate the distance from you.");
                maxStep(1);
                setUsing(true, false);
                break;
            }
            case (R.id.measureLengthFree): {
                position = "free";
                facing = "free";
                standardMeasure();
                showAdjustment(View.GONE);
                if (incline == 0) {
                    showHeight(View.GONE);
                    showSlant(View.GONE);
                }
                setTitle("Measuring\nLength Free");
                setPhrase("Aim at the ground below the object's first end point, then press and release. If the object is on the ground, you may instead aim at the first point and press and hold, then aim at the second point and release.", "Aim at the first point and press and hold, then aim at the second point and release.");
                maxStep(4);
                skip(1);
                setUsing(true, true);
                break;
            }
            case (R.id.measureCamera): {
                type = "camera";
                setContentView(R.layout.measure_camera);
                displayCM();
                textureView = (TextureView) findViewById(R.id.cameraPreview);
                textureView.setSurfaceTextureListener(textureListener);
                makeDigital(R.id.heightinch, R.id.heightfoot);
                setUsing(true, false);
                setUpCameraSpinner();
                cameraBoundaryListener();
                break;
            }
        }
    }

    public void formulaTest(View view) {
        executeTestCalibration();
        setPitchAndYaw();
        measureChosen(view);
    }

    // Eye Height Setting Functions
    public double heightToEye(double pitchGround, double pitch1, double pitch2) {
        return Math.abs(getTypedNumber() * dtan(pitchGround) / (dtan(pitch2) - dtan(pitch1)));
    }

    public double freeToEye(double pitchGround, double pitch1, double pitch2, double yawDiff) {
        double tanRatio = dtan(pitch1) / dtan(pitch2);
        double distance = getTypedNumber() / Math.sqrt(1 + tanRatio * tanRatio - 2 * tanRatio * dcos(yawDiff));
        return Math.abs(distance * dtan(pitchGround));
    }

    public double youToEye(double pitchGround) {
        return Math.abs(getTypedNumber() * dtan(pitchGround));
    }

    public double m2hToEye(double pitch1, double pitch2, double pitchNew) {
        double distance = Math.abs((eye1 - eye2) / (dtan(pitch1) - dtan(pitch2)));
        double pointHeight = eye1 + distance * dtan(pitch1);
        return Math.max(0, pointHeight - distance * dtan(pitchNew));
    }

    // Incline Setting Functions
    public double youToIncline(double pitchGround) {
        double angleDefault = eye1 / getTypedNumber();
        return darctan(angleDefault + dtan(pitchGround));
    }

    public double heightToIncline(double pitchGround, double pitch1, double pitch2) {
        double distance = getTypedNumber() / (dtan(pitch2) - dtan(pitch1));
        double angleDefault = eye1 / distance;
        return darctan(angleDefault + dtan(pitchGround));
    }

    public double m2hToIncline(double pitch1, double pitch2) {
        double distance = Math.abs((eye1 - eye2) / (dtan(pitch1) - dtan(pitch2)));
        double heightGround = distance * dtan(pitch1) + eye1;
        return darctan(heightGround / distance);
    }

    // Camera Setting And Measuring Functions
    public double calculateCameraAngle() {
        switch (type) {
            case "height": {
                double height = getTypedNumber();
                return darctan(height * dsin(pitch) / (cameraHeight * dtan(pitch) - height * dcos(pitch)));
            }
            case "length": {
                double length = getTypedNumber();
                return darctan(length * dcos(pitch) / (cameraHeight * dtan(pitch) + length * dsin(pitch)));
            }
            case "from you": {
                return darctan(1 / dsin(pitch) * (dcos(pitch) - cameraHeight / getTypedNumber()));
            }
            default:
                return 0;
        }
    }

    public double calculateCameraHeight() {
        switch (type) {
            case "height": {
                double distance = getTypedNumber() * dsin(pitch + cameraAngle) / dsin(cameraAngle);
                return distance / dtan(pitch);
            }
            case "length": {
                double distance = getTypedNumber() * dcos(pitch + cameraAngle) / dsin(cameraAngle);
                return distance / dtan(pitch);
            }
            case "from you": {
                return getTypedNumber() * dcos(cameraAngle + pitch - incline) / dcos(cameraAngle);
            }
            default:
                return 0;
        }
    }

    public double cameraMeasure() {
        double returnValue = 0;
        double angleTop = 90 + darctan(dtan(cameraAngle) * (2 * (double) (boundaryTop - boundaryBottomMax) / (double) (boundaryTopMax - boundaryBottomMax) - 1));
        double angleBottom = 90 + darctan(dtan(cameraAngle) * (2 * (double) (boundaryBottom - boundaryBottomMax) / (double) (boundaryTopMax - boundaryBottomMax) - 1));
        double pixelsToLeft = (boundaryLeft - (boundaryRightMax + boundaryLeftMax) / 2) / (double) imageDimension.getWidth() * imageDimension.getHeight();
        double pixelsToRight = (boundaryRight - (boundaryRightMax + boundaryLeftMax) / 2) / (double) imageDimension.getWidth() * imageDimension.getHeight();
        ;
        double angleLeft = darctan(dtan(cameraAngle) * 2 * pixelsToLeft / (double) (boundaryBottomMax - boundaryTopMax));
        double angleRight = darctan(dtan(cameraAngle) * 2 * pixelsToRight / (double) (boundaryBottomMax - boundaryTopMax));
        Spinner spinner = (Spinner) findViewById(R.id.cameraSpinner);
        switch (spinner.getSelectedItem().toString()) {
            case "From You": {
                setBoundariesVisible(false, false, true, false);
                returnValue = cameraHeight * dsin(angleTop) / dsin(angleTop + pitch - incline);
                break;
            }
            case "Length": {
                double length1 = cameraHeight * dsin(angleTop) / dsin(angleTop + pitch - incline);
                double length2 = cameraHeight * dsin(angleBottom) / dsin(angleBottom + pitch - incline);
                returnValue = length1 - length2;
                setBoundariesVisible(false, false, true, true);
                break;
            }
            case "Height": {
                double length = cameraHeight * dsin(pitch - incline) / dsin(angleBottom + pitch - incline);
                returnValue = -length * dsin(angleTop - angleBottom) / dcos(angleTop + pitch);
                setBoundariesVisible(false, false, true, true);
                break;
            }
            case "Width": {
                double distance = cameraHeight / (1 / dtan(angleTop) + 1 / dtan(pitch - incline));
                double distanceLeft = distance * dtan(angleLeft);
                double distanceRight = distance * dtan(angleRight);
                returnValue = distanceRight - distanceLeft;
                setBoundariesVisible(true, true, true, false);
                break;
            }
        }
        if ((returnValue > 1199.9 && unit.equals("in")) || returnValue > 2999.9 && unit.equals("cm")) {
            return (unit.equals("in") ? 1199.9 : 2999.9);
        } else {
            return (returnValue > 0 ? returnValue : 0);
        }
    }

    // Measuring Functions
    public void eyeToHeight(double pitchGround, double pitch1, double pitch2, double yawDiff) {
        double distance = eye1 / (dtan(pitchGround) - dtan(incline) * dcos(yawDiff));
        double low = distance * dtan(pitch1);
        double high = distance * dtan(pitch2);
        heightResult = high - low;
    }

    public void eyeToLengthFree(double pitchGround, double pitch1, double pitch2, double yaw1, double yaw2) {
        double x2, y1, y2, z1, z2, xy2, incline1, incline2, zGround, yawDiff;
        incline1 = incline * dcos(inclineYaw - yaw1);
        y1 = eye1 / (dtan(incline1) - dtan(pitchGround));
        zGround = y1 * dtan(incline1);
        yawDiff = angle_difference(yaw1, yaw2);
        if (skipped) {
            incline2 = incline * dcos(inclineYaw - yaw2);
            xy2 = eye1 / (dtan(incline2) - dtan(pitch2));
            x2 = xy2 * dsin(yawDiff);
            y2 = xy2 * dcos(yawDiff);
            z1 = zGround;
            z2 = Math.sqrt(x2 * x2 + y2 * y2) * dtan(incline2);
        } else {
            z1 = y1 * dtan(pitch1) + eye1;
            z2 = z1;
            xy2 = (z2 - eye1) / dtan(pitch2);
            x2 = xy2 * dsin(yawDiff);
            y2 = xy2 * dcos(yawDiff);
        }
        widthResult = x2;
        depthResult = y2 - y1;
        heightResult = z2 - z1;
    }

    public void eyeToLengthPerpendicular(double pitchGround, double pitchPerp, double pitch1, double pitch2, boolean isInside) {
        double distance = Math.abs(eye1 / (dtan(pitchGround) - dtan(incline)));
        double newheight = dtan(pitchPerp) * distance;
        double distance1 = Math.max(Math.abs(newheight / dtan(pitch1)), distance);
        double distance2 = Math.max(Math.abs(newheight / dtan(pitch2)), distance);

        double side1 = Math.sqrt(distance1 * distance1 - distance * distance);
        double side2 = Math.sqrt(distance2 * distance2 - distance * distance);
        if (isInside) {
            widthResult = side1 + side2;
        } else {
            widthResult = side1 - side2;
        }
    }

    public void eyeToLengthYou(double pitchGround) {
        depthResult = eye1 / (dtan(pitchGround) - dtan(incline));
    }

    public void eyeTo2hFree(double pitchP1H1, double pitchP2H1, double pitchP1H2, double pitchP2H2, double yawDiff, boolean isFixing) {
        double angle1 = dtan(pitchP1H1) - dtan(pitchP1H2);
        double angle2 = dtan(pitchP2H1) - dtan(pitchP2H2);
        double heightDiff = eye1 - eye2;
        double distance1 = Math.abs(heightDiff / angle1);
        double distance2 = Math.abs(heightDiff / angle2);
        double height1 = distance1 * dtan(pitchP1H1);
        double height2 = distance2 * dtan(pitchP2H1);
        if (isFixing) {
            heightDisplay = height1 - height2;
            depthDisplay = distance1 - distance2 * dcos(yawDiff);
            widthDisplay = distance2 * dsin(yawDiff);
        } else {
            heightResult = height1 - height2;
            depthResult = distance1 - distance2 * dcos(yawDiff);
            widthResult = distance2 * dsin(yawDiff);
        }
    }

    public void eyeTo2hPerpendicular(double pitchPerpH1, double pitchP1H1, double pitchP2H1, double pitchPerpH2, double pitchP1H2, double pitchP2H2, boolean isInside, boolean isFixing) {
        double angle1 = dtan(pitchP1H2) - dtan(pitchP1H1);
        double angle2 = dtan(pitchP2H2) - dtan(pitchP2H1);
        double angle3 = dtan(pitchPerpH2) - dtan(pitchPerpH1);
        double heightDiff = eye1 - eye2;
        double distance = Math.abs(heightDiff / angle3);
        double distance1 = Math.max(Math.abs(heightDiff / angle1), distance);
        double distance2 = Math.max(Math.abs(heightDiff / angle2), distance);
        double height1 = distance1 * dtan(pitchP1H1);
        double height2 = distance2 * dtan(pitchP2H1);

        double side1 = Math.sqrt(distance1 * distance1 - distance * distance);
        double side2 = Math.sqrt(distance2 * distance2 - distance * distance);

        if (isFixing) {
            heightDisplay = height1 - height2;
            if (isInside) {
                widthDisplay = side1 + side2;
            } else {
                widthDisplay = side1 - side2;
            }
        } else {
            heightResult = height1 - height2;
            if (isInside) {
                widthResult = side1 + side2;
            } else {
                widthResult = side1 - side2;
            }
        }
    }

    public void eyeTo2hYou(double pitchH1, double pitchH2) {
        double angleDiff = dtan(pitchH2) - dtan(pitchH1);
        double heightDiff = eye1 - eye2;
        depthResult = heightDiff / angleDiff;
        heightResult = eye1 + depthResult * dtan(pitchH1);
    }

    public void eyeTo2pParallel(double pitchP1H1, double pitchP2H1, double pitchP1H2, double pitchP2H2, boolean isFixing) {
        double height1 = stride * dsin(pitchP1H1) * dsin(pitchP1H2) / dsin(pitchP1H2 - pitchP1H1);
        double height2 = stride * dsin(pitchP2H1) * dsin(pitchP2H2) / dsin(pitchP2H2 - pitchP2H1);
        double distance1 = stride * dsin(pitchP1H1) * dcos(pitchP1H2) / dsin(pitchP1H2 - pitchP1H1);
        double distance2 = stride * dsin(pitchP2H1) * dcos(pitchP2H2) / dsin(pitchP2H2 - pitchP2H1);
        if (isFixing) {
            heightDisplay = height1 * Math.signum(distance1) - height2 * Math.signum(distance2);
            depthDisplay = distance1 - distance2;
        } else {
            heightResult = height1 * Math.signum(distance1) - height2 * Math.signum(distance2);
            depthResult = distance1 - distance2;
            LinearLayout removeDepthLabel = (LinearLayout) findViewById(R.id.removeDepthLabel);
            if (Math.signum(distance1) == Math.signum(distance2)) {
                fixable[1] = true;
                position = "outside";
                removeDepthLabel.setVisibility(View.VISIBLE);
            } else {
                position = "inside";
                fixable[1] = false;
                removeDepthLabel.setVisibility(View.GONE);
            }
        }
    }

    public void eyeTo2pPerpendicular(double pitchP1H1, double pitchP2H1, double pitchP1H2, double pitchP2H2, boolean isFixing) {
        if (Math.abs(pitchP1H1) > Math.abs(pitchP1H2)) {
            double temp = pitchP1H2;
            pitchP1H2 = pitchP1H1;
            pitchP1H1 = temp;
            temp = pitchP2H2;
            pitchP2H2 = pitchP2H1;
            pitchP2H1 = temp;
        }
        double distance = stride * dsin(pitchP1H1) * dcos(pitchP1H2) / dsin(pitchP1H2 - pitchP1H1);
        double height1 = stride * dsin(pitchP1H1) * dsin(pitchP1H2) / dsin(pitchP1H2 - pitchP1H1);
        if (isFixing) {
            widthDisplay = Math.sqrt(Math.max(0, (distance * distance * dtan(pitchP2H2) * dtan(pitchP2H2) - (stride + distance) * (stride + distance) * dtan(pitchP2H1) * dtan(pitchP2H1)) / (dtan(pitchP2H1) * dtan(pitchP2H1) - dtan(pitchP2H2) * dtan(pitchP2H2))));
            double height2 = dtan(pitchP2H2) * Math.sqrt(widthDisplay * widthDisplay + distance * distance);
            heightDisplay = height1 - height2;
        } else {
            widthResult = Math.sqrt(Math.max(0, (distance * distance * dtan(pitchP2H2) * dtan(pitchP2H2) - (stride + distance) * (stride + distance) * dtan(pitchP2H1) * dtan(pitchP2H1)) / (dtan(pitchP2H1) * dtan(pitchP2H1) - dtan(pitchP2H2) * dtan(pitchP2H2))));
            double height2 = dtan(pitchP2H2) * Math.sqrt(widthResult * widthResult + distance * distance);
            heightResult = height1 - height2;
        }
    }

    public void eyeTo2pFree(double pitchP1H1, double pitchP2H1, double pitchP1H2, double pitchP2H2, double yawP1H1, double yawP2H1, double yawP1H2, double yawP2H2, boolean isFixing) {
        if (Math.signum(pitchP1H1 * pitchP1H2) == -1 || Math.signum(pitchP2H1 * pitchP2H2) == -1) {
            if (isFixing) {
                heightDisplay = 0;
                widthDisplay = 0;
                depthDisplay = 0;
                return;
            } else {
                heightResult = 0;
                widthResult = 0;
                depthResult = 0;
                return;
            }
        }

        double pitchRatio = dtan(pitchP1H2) / dtan(pitchP1H1);
        double circleRadius = pitchRatio / (pitchRatio * pitchRatio - 1);
        double circleCenter = pitchRatio * circleRadius;
        double x1 = 0;
        double y1 = 0;
        double dir = 0;
        double z1 = 0;
        if (Math.signum(yawP1H1) == Math.signum(yawP1H2) && Math.abs(yawP1H1) > 1 && Math.abs(yawP1H2) > 1 && Math.abs(yawP1H1) < 179 && Math.abs(yawP1H2) < 179) {
            x1 = dsin(yawP1H2) * dsin(yawP1H1) / dsin(yawP1H2 - yawP1H1);
            y1 = dsin(yawP1H2) * dcos(yawP1H1) / dsin(yawP1H2 - yawP1H1);
            dir = darctan2(y1 - circleCenter, x1);
            x1 = dcos(dir) * Math.abs(circleRadius);
            y1 = dsin(dir) * Math.abs(circleRadius) + circleCenter;
        } else {
            yawP1H1 = Math.abs(yawP1H1) > 90 ? 180 : 0;
            yawP1H2 = Math.abs(yawP1H2) > 90 ? 180 : 0;
            switch ((pitchRatio > 1) ? ("H2 " + (int) yawP1H2) : ("H1 " + (int) yawP1H1)) {
                case "H2 0": {
                    y1 = circleCenter + Math.abs(circleRadius);
                    break;
                }
                case "H2 180": {
                    y1 = circleCenter - Math.abs(circleRadius);
                    break;
                }
                case "H1 0": {
                    y1 = circleCenter + Math.abs(circleRadius);
                    break;
                }
                case "H1 180": {
                    y1 = circleCenter - Math.abs(circleRadius);
                    break;
                }
            }
        }
        z1 = Math.sqrt(x1 * x1 + y1 * y1) * dtan(pitchP1H1);

        pitchRatio = dtan(pitchP2H2) / dtan(pitchP2H1);
        circleRadius = pitchRatio / (pitchRatio * pitchRatio - 1);
        circleCenter = pitchRatio * circleRadius;
        double x2 = 0;
        double y2 = 0;
        dir = 0;
        double z2 = 0;
        if (Math.signum(yawP2H1) == Math.signum(yawP2H2) && Math.abs(yawP2H1) > 1 && Math.abs(yawP2H2) > 1 && Math.abs(yawP2H1) < 179 && Math.abs(yawP2H2) < 179) {
            x2 = dsin(yawP2H2) * dsin(yawP2H1) / dsin(yawP2H2 - yawP2H1);
            y2 = dsin(yawP2H2) * dcos(yawP2H1) / dsin(yawP2H2 - yawP2H1);
            dir = darctan2(y2 - circleCenter, x2);
            x2 = dcos(dir) * Math.abs(circleRadius);
            y2 = dsin(dir) * Math.abs(circleRadius) + circleCenter;
        } else {
            yawP2H1 = Math.abs(yawP2H1) > 90 ? 180 : 0;
            yawP2H2 = Math.abs(yawP2H2) > 90 ? 180 : 0;
            switch ((pitchRatio > 1) ? ("H2 " + (int) yawP2H2) : ("H1 " + (int) yawP2H1)) {
                case "H2 0": {
                    y2 = circleCenter + Math.abs(circleRadius);
                    break;
                }
                case "H2 180": {
                    y2 = circleCenter - Math.abs(circleRadius);
                    break;
                }
                case "H1 0": {
                    y2 = circleCenter + Math.abs(circleRadius);
                    break;
                }
                case "H1 180": {
                    y2 = circleCenter - Math.abs(circleRadius);
                    break;
                }
            }
        }
        z2 = Math.sqrt(x2 * x2 + y2 * y2) * dtan(pitchP2H1);

        if (isFixing) {
            heightDisplay = stride * (z2 - z1);
            depthDisplay = stride * (y2 - y1);
            widthDisplay = stride * (x2 - x1);
        } else {
            heightResult = stride * (z2 - z1);
            depthResult = stride * (y2 - y1);
            widthResult = stride * (x2 - x1);
        }

    }

    public void eyeTo2pObjectPositions(double pitchGround1, double pitchPoint1, double pitchGround2, double pitchPoint2, String fixing) {
        double heightGround = eye1 * (dtan(pitchGround1) - dtan(pitchGround2)) / (dtan(pitchGround1) + dtan(pitchGround2));
        double depth = Math.abs((eye1 + heightGround) / dtan(pitchGround1));
        double height1 = eye1 + heightGround + depth * dtan(pitchPoint1);
        double height2 = eye1 - heightGround + depth * dtan(pitchPoint2);

        switch (fixing) {
            case "remove height": {
                depthDisplay = depth;
                heightDisplay = height1 - height2 - heightGround;
                break;
            }
            case "remove depth": {
                depthDisplay = 0;
                heightDisplay = Math.abs(height1) > Math.abs(height2) ? Math.abs(height1) : Math.abs(height2);
                break;
            }
            case "not fixing": {
                depthResult = depth;
                heightResult = height1 - height2 - heightGround;
                break;
            }
        }
    }

    public void eyeToAngle(double pitch1, double yaw1, double pitch2, double yaw2) {
        double x1, y1, z1, x2, y2, z2, dotProduct;

        x1 = dcos(pitch1) * dcos(yaw1);
        y1 = dcos(pitch1) * dsin(yaw1);
        z1 = dsin(pitch1);

        x2 = dcos(pitch2) * dcos(yaw2);
        y2 = dcos(pitch2) * dsin(yaw2);
        z2 = dsin(pitch2);

        dotProduct = x1 * x2 + y1 * y2 + z1 * z2;
        angleResult = darccos(dotProduct);
    }

    public void eyeToSurface(double pitch1, double pitch2) {
        pitch1 -= incline;
        pitch2 -= incline;
        depthResult = cameraHeight * (dcos(pitch1) - dsin(pitch1) * (dcos(pitch1) - dcos(pitch2)) / (dsin(pitch1) - dsin(pitch2)));
    }

    public void measureFix(View view) {
        showResult();
        double theta = 0;
        int i;
        int j;
        int signs[] = {-1, -1, -1, -1, -1, -1, -1, -1};
        CheckBox removeHeightBox = (CheckBox) findViewById(R.id.removeHeightBox);
        CheckBox removeDepthBox = (CheckBox) findViewById(R.id.removeDepthBox);
        CheckBox removeWidthBox = (CheckBox) findViewById(R.id.removeWidthBox);
        boolean heightChecked = removeHeightBox.isChecked();
        boolean depthChecked = removeDepthBox.isChecked();
        boolean widthChecked = removeWidthBox.isChecked();
        int checkCombined = (heightChecked ? 1 : 0) + (depthChecked ? 2 : 0) + (widthChecked ? 4 : 0);
        if (checkCombined == 0) {
            showResult();
            return;
        }
        switch (type + " " + facing + " " + position + " " + checkCombined) {
            case "2h parallel outside 3":
            case "2h free free 7":
            case "2p parallel outside 3":
            case "2p free free 7":
            case "2p object positions inside 3": {
                return;
            }
            case "2h parallel inside 1":
            case "2h parallel outside 1":
            case "2h hypotenuse hypotenuse 1":
            case "2h free free 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], (position == "free") ? angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3])) : defaultAngle(), true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p parallel inside 1":
            case "2p parallel outside 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pParallel(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2h parallel outside 2":
            case "2h free free 2": {
                while (Math.signum(depthDisplay) == Math.signum(depthResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], ((position == "free") ? angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3])) : 0), true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult)) {
                                i = 64;
                                j = 6;
                                depthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p parallel outside 2": {
                while (Math.signum(depthDisplay) == Math.signum(depthResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pParallel(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult)) {
                                i = 64;
                                j = 6;
                                depthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2h perpendicular inside 1":
            case "2h perpendicular outside 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 64; i++) {
                        for (j = 0; j < 6; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hPerpendicular(pitchx[0] + theta * signs[0], pitchx[2] + theta * signs[1], pitchx[3] + theta * signs[2], pitchx[4] + theta * signs[3], pitchx[6] + theta * signs[4], pitchx[7] + theta * signs[5], (position == "inside"), true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p perpendicular inside 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pPerpendicular(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p object positions inside 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pObjectPositions(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], "remove height");
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p object positions inside 2": {
                eyeTo2pObjectPositions(pitchx[0], pitchx[1], pitchx[2], pitchx[3], "remove depth");
                break;
            }
            case "2h free free 4": {
                eyeTo2hFree(pitchx[0], pitchx[1], pitchx[2], pitchx[3], (Math.abs(angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3]))) > 90) ? 180 : 0, true);
                break;
            }
            case "2h free free 3": {
                while ((Math.signum(depthDisplay) == Math.signum(depthResult) || Math.signum(heightDisplay) == Math.signum(heightResult)) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 32; i++) {
                        for (j = 0; j < 5; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3])) + 3 * theta * signs[4], true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult) && Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                depthDisplay = 0;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2h free free 5": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], (Math.abs(angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3]))) > 90) ? 180 : 0, true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 64;
                                j = 6;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2h free free 6": {
                while (Math.signum(depthDisplay) == Math.signum(depthResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 16; i++) {
                        for (j = 0; j < 4; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2hFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[2] + theta * signs[2], pitchx[3] + theta * signs[3], (Math.abs(angle_difference(angle_midpoint(yawx[0], yawx[2]), angle_midpoint(yawx[1], yawx[3]))) > 90) ? 180 : 0, true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult)) {
                                i = 64;
                                j = 6;
                                depthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 1": {
                while (Math.signum(heightDisplay) == Math.signum(heightResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 256;
                                j = 8;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 2": {
                while (Math.signum(depthDisplay) == Math.signum(depthResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult)) {
                                i = 256;
                                j = 8;
                                depthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 3": {
                while ((Math.signum(depthDisplay) == Math.signum(depthResult) || Math.signum(heightDisplay) == Math.signum(heightResult)) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult) && Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 256;
                                j = 8;
                                depthDisplay = 0;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 4": {
                while (Math.signum(widthDisplay) == Math.signum(widthResult) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(widthDisplay) != Math.signum(widthResult)) {
                                i = 256;
                                j = 8;
                                widthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 5": {
                while ((Math.signum(widthDisplay) == Math.signum(widthResult) || Math.signum(heightDisplay) == Math.signum(heightResult)) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(widthDisplay) != Math.signum(widthResult) && Math.signum(heightDisplay) != Math.signum(heightResult)) {
                                i = 256;
                                j = 8;
                                widthDisplay = 0;
                                heightDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
            case "2p free free 6": {
                while ((Math.signum(depthDisplay) == Math.signum(depthResult) || Math.signum(widthDisplay) == Math.signum(widthResult)) && theta < 9.9999) {
                    theta += .0001;
                    for (i = 0; i < 256; i++) {
                        for (j = 0; j < 8; j++) {
                            signs[j] = setSign(i, j);
                            eyeTo2pFree(pitchx[0] + theta * signs[0], pitchx[1] + theta * signs[1], pitchx[4] + theta * signs[2], pitchx[5] + theta * signs[3], angle_difference(yawx[2], yawx[0]) + 3 * theta * signs[4], angle_difference(yawx[2], yawx[1]) + 3 * theta * signs[5], angle_difference(yawx[2], yawx[4] + 3 * theta * signs[6]), angle_difference(yawx[2], yawx[5]) + 3 * theta * signs[7], true);
                            if (Math.signum(depthDisplay) != Math.signum(depthResult) && Math.signum(widthDisplay) != Math.signum(widthResult)) {
                                i = 256;
                                j = 8;
                                depthDisplay = 0;
                                widthDisplay = 0;
                            }
                        }
                    }
                }
                break;
            }
        }
        if (theta >= 9.9999) {
            showToast("Angle adjustment too large.");
        } else {
            adjustmentDisplay = theta;
            showDisplay();
        }
    }

    // Formula Test Functions
    public void setPitchAndYaw() {
        int i;
        double pitchWholeVal, pitchDecimalVal, yawWholeVal, yawDecimalVal;
        EditText pitchWhole, pitchDecimal, yawWhole, yawDecimal;
        TextView pitchSign;
        for (i = 0; i < 8; i++) {
            pitchSign = (TextView) findViewByIdString("pitch" + i + "Sign");
            pitchWhole = (EditText) findViewByIdString("pitch" + i + "Whole");
            pitchDecimal = (EditText) findViewByIdString("pitch" + i + "Decimal");
            yawWhole = (EditText) findViewByIdString("yaw" + i + "Whole");
            yawDecimal = (EditText) findViewByIdString("yaw" + i + "Decimal");
            pitchWholeVal = (double) (pitchWhole.getText().toString().isEmpty() ? 0 : Integer.parseInt(pitchWhole.getText().toString()));
            pitchDecimalVal = (double) (pitchDecimal.getText().toString().isEmpty() ? 0 : Integer.parseInt(pitchDecimal.getText().toString()) / Math.pow(10, pitchDecimal.getText().length()));
            yawWholeVal = (double) (yawWhole.getText().toString().isEmpty() ? 0 : Integer.parseInt(yawWhole.getText().toString()));
            yawDecimalVal = (double) (yawDecimal.getText().toString().isEmpty() ? 0 : Integer.parseInt(yawDecimal.getText().toString()) / Math.pow(10, yawDecimal.getText().length()));
            pitchx[i] = pitchWholeVal + pitchDecimalVal;
            yawx[i] = yawWholeVal + yawDecimalVal;
            if (pitchSign.getText() == "   -") {
                pitchx[i] *= -1;
            }
            pitchx[i] -= offset;
        }
    }

    public void measureChosen(View view) {
        heightResult = 0;
        depthResult = 0;
        widthResult = 0;
        adjustmentResult = 0;
        angleResult = 0;

        int i;
        TextView stepView;
        Spinner spinner = (Spinner) findViewById(R.id.formulaTestSpinner);
        switch (spinner.getSelectedItem().toString()) {
            case "Values Only": {
                for (i = 0; i < 8; i++) {
                    stepView = (TextView) findViewByIdString("step" + i);
                    stepView.setText("Step " + i + ": (" + String.format("%1$.2f", pitchx[i]) + ", " + String.format("%1$.2f", yawx[i]) + ")");
                }
                return;
            }
            case "Height: Same Incline Direction": {
                type = "height";
                facing = "parallel";
                position = "outside";
                break;
            }
            case "Height: Magnetic Sensor": {
                type = "height";
                facing = "free";
                position = "free";
                break;
            }
            case "Length: From You": {
                type = "length";
                facing = "from you";
                position = "from you";
                break;
            }
            case "Length: Parallel Inside": {
                type = "length";
                facing = "parallel";
                position = "inside";
                skipped = false;
                break;
            }
            case "Length: Parallel Inside Skipped": {
                type = "length";
                facing = "parallel";
                position = "inside";
                skipped = true;
                break;
            }
            case "Length: Parallel Outside": {
                type = "length";
                facing = "parallel";
                position = "outside";
                skipped = false;
                break;
            }
            case "Length: Parallel Outside Skipped": {
                type = "length";
                facing = "parallel";
                position = "outside";
                skipped = true;
                break;
            }
            case "Length: Perpendicular Inside": {
                type = "length";
                facing = "perpendicular";
                position = "inside";
                break;
            }
            case "Length: Perpendicular Outside": {
                type = "length";
                facing = "perpendicular";
                position = "outside";
                break;
            }
            case "Length: Hypotenuse": {
                type = "length";
                facing = "hypotenuse";
                position = "hypotenuse";
                skipped = false;
                break;
            }
            case "Length: Hypotenuse Skipped": {
                type = "length";
                facing = "hypotenuse";
                position = "hypotenuse";
                skipped = true;
                break;
            }
            case "Length: Free": {
                type = "length";
                facing = "free";
                position = "free";
                skipped = false;
                break;
            }
            case "Length: Free Skipped": {
                type = "length";
                facing = "free";
                position = "free";
                skipped = true;
                break;
            }
            case "Length: Surface": {
                type = "length";
                facing = "surface";
                position = "surface";
                break;
            }
            case "2Ht: From You": {
                type = "2h";
                facing = "from you";
                position = "from you";
                break;
            }
            case "2Ht: Parallel Inside": {
                type = "2h";
                facing = "parallel";
                position = "inside";
                break;
            }
            case "2Ht: Parallel Outside": {
                type = "2h";
                facing = "parallel";
                position = "outside";
                break;
            }
            case "2Ht: Perpendicular Inside": {
                type = "2h";
                facing = "perpendicular";
                position = "inside";
                break;
            }
            case "2Ht: Perpendicular Outside": {
                type = "2h";
                facing = "perpendicular";
                position = "outside";
                break;
            }
            case "2Ht: Hypotenuse": {
                type = "2h";
                facing = "hypotenuse";
                position = "hypotenuse";
                break;
            }
            case "2Ht: Free": {
                type = "2h";
                facing = "free";
                position = "free";
                break;
            }
            case "2Pos: Parallel": {
                type = "2p";
                facing = "parallel";
                break;
            }
            case "2Pos: Perpendicular": {
                type = "2p";
                facing = "perpendicular";
                position = "inside";
                break;
            }
            case "2Pos: Free": {
                type = "2p";
                facing = "free";
                position = "free";
                break;
            }
            case "2Pos: Object Positions": {
                type = "2p";
                facing = "object positions";
                position = "inside";
                break;
            }
            case "Angle: Vertical Only": {
                type = "angle";
                facing = "parallel";
                position = "outside";
                break;
            }
            case "Angle: Free": {
                type = "angle";
                facing = "free";
                position = "free";
                break;
            }
        }
        for (i = 0; i < 8; i++) {
            stepView = (TextView) findViewByIdString("step" + i);
            stepView.setText("Step " + i);
        }
        setFixable();
        currentStep = 10;
        nextStep();
    }

    // Display Functions
    public void showToast(String string) {
        Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG);
        toast.show();
    }

    public void showResult() {
        lengthResult = Math.sqrt(widthResult * widthResult + depthResult * depthResult);
        slantResult = Math.sqrt(lengthResult * lengthResult + heightResult * heightResult);
        heightDisplay = heightResult;
        depthDisplay = depthResult;
        widthDisplay = widthResult;
        adjustmentDisplay = adjustmentResult;
        angleDisplay = angleResult;
        showDisplay();
    }

    public void showDisplay() {
        lengthDisplay = Math.sqrt(widthDisplay * widthDisplay + depthDisplay * depthDisplay);
        slantDisplay = Math.sqrt(lengthDisplay * lengthDisplay + heightDisplay * heightDisplay);

        heightDisplay = round(heightDisplay, .1);
        widthDisplay = round(widthDisplay, .1);
        depthDisplay = round(depthDisplay, .1);
        lengthDisplay = round(lengthDisplay, .1);
        slantDisplay = round(slantDisplay, .1);

        TextView heightinch = (TextView) findViewById(R.id.heightinch);
        TextView depthinch = (TextView) findViewById(R.id.depthinch);
        TextView widthinch = (TextView) findViewById(R.id.widthinch);
        TextView lengthinch = (TextView) findViewById(R.id.lengthinch);
        TextView slantinch = (TextView) findViewById(R.id.slantinch);
        TextView angleadjust = (TextView) findViewById(R.id.angleadjust);
        TextView anglemeasure = (TextView) findViewById(R.id.anglemeasure);
        TextView heightfoot = (TextView) findViewById(R.id.heightfoot);
        TextView depthfoot = (TextView) findViewById(R.id.depthfoot);
        TextView widthfoot = (TextView) findViewById(R.id.widthfoot);
        TextView lengthfoot = (TextView) findViewById(R.id.lengthfoot);
        TextView slantfoot = (TextView) findViewById(R.id.slantfoot);
        if (unit.equals("in")) {
            heightinch.setText(String.format("%1$.1f", Math.abs(heightDisplay) % 12));
            if (negativeAllowed) {
                heightfoot.setText(String.format("%1$.0f", Math.signum(heightDisplay) * Math.floor(Math.abs(heightDisplay) / 12)));
            } else {
                heightfoot.setText(String.format("%1$.0f", Math.floor(Math.abs(heightDisplay) / 12)));
            }
            depthinch.setText(String.format("%1$.1f", Math.abs(depthDisplay) % 12));
            depthfoot.setText(String.format("%1$.0f", Math.floor(Math.abs(depthDisplay) / 12)));
            widthinch.setText(String.format("%1$.1f", Math.abs(widthDisplay) % 12));
            widthfoot.setText(String.format("%1$.0f", Math.floor(Math.abs(widthDisplay) / 12)));
            lengthinch.setText(String.format("%1$.1f", lengthDisplay % 12));
            lengthfoot.setText(String.format("%1$.0f", Math.floor(lengthDisplay / 12)));
            slantinch.setText(String.format("%1$.1f", slantDisplay % 12));
            slantfoot.setText(String.format("%1$.0f", Math.floor(slantDisplay / 12)));
        } else {
            if (negativeAllowed) {
                heightinch.setText(String.format("%1$.1f", heightDisplay));
            } else {
                heightinch.setText(String.format("%1$.1f", Math.abs(heightDisplay)));
            }
            depthinch.setText(String.format("%1$.1f", Math.abs(depthDisplay)));
            widthinch.setText(String.format("%1$.1f", Math.abs(widthDisplay)));
            lengthinch.setText(String.format("%1$.1f", lengthDisplay));
            slantinch.setText(String.format("%1$.1f", slantDisplay));
        }
        angleadjust.setText(String.format("%1$.4f", adjustmentDisplay));
        showAdjustment((adjustmentDisplay == 0) ? View.GONE : View.VISIBLE);
        anglemeasure.setText(String.format("%1$.2f", angleDisplay));
    }

    public void showHeight(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.heightlabel);
        TextView tv2 = (TextView) findViewById(heightfoot);
        TextView tv3 = (TextView) findViewById(R.id.heightinch);
        TextView tv4 = (TextView) findViewById(R.id.footlabelh);
        TextView tv5 = (TextView) findViewById(R.id.inchlabelh);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
        tv4.setVisibility(isVisible);
        tv5.setVisibility(isVisible);
    }

    public void showLength(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.lengthlabel);
        TextView tv2 = (TextView) findViewById(lengthfoot);
        TextView tv3 = (TextView) findViewById(lengthinch);
        TextView tv4 = (TextView) findViewById(R.id.footlabell);
        TextView tv5 = (TextView) findViewById(R.id.inchlabell);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
        tv4.setVisibility(isVisible);
        tv5.setVisibility(isVisible);
    }

    public void showWidth(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.widthlabel);
        TextView tv2 = (TextView) findViewById(widthfoot);
        TextView tv3 = (TextView) findViewById(widthinch);
        TextView tv4 = (TextView) findViewById(R.id.footlabelw);
        TextView tv5 = (TextView) findViewById(R.id.inchlabelw);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
        tv4.setVisibility(isVisible);
        tv5.setVisibility(isVisible);
    }

    public void showDepth(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.depthlabel);
        TextView tv2 = (TextView) findViewById(depthfoot);
        TextView tv3 = (TextView) findViewById(depthinch);
        TextView tv4 = (TextView) findViewById(R.id.footlabeld);
        TextView tv5 = (TextView) findViewById(R.id.inchlabeld);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
        tv4.setVisibility(isVisible);
        tv5.setVisibility(isVisible);
    }

    public void showSlant(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.slantlabel);
        TextView tv2 = (TextView) findViewById(slantfoot);
        TextView tv3 = (TextView) findViewById(slantinch);
        TextView tv4 = (TextView) findViewById(R.id.footlabels);
        TextView tv5 = (TextView) findViewById(inchlabels);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
        tv4.setVisibility(isVisible);
        tv5.setVisibility(isVisible);
    }

    public void showAdjustment(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.anglelabel);
        TextView tv2 = (TextView) findViewById(R.id.angleadjust);
        TextView tv3 = (TextView) findViewById(R.id.adjlabel);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
    }

    public void showAngle(int isVisible) {
        TextView tv1 = (TextView) findViewById(R.id.angle1label);
        TextView tv2 = (TextView) findViewById(R.id.anglemeasure);
        TextView tv3 = (TextView) findViewById(R.id.anglabel);
        tv1.setVisibility(isVisible);
        tv2.setVisibility(isVisible);
        tv3.setVisibility(isVisible);
    }

    // Saving Functions
    public void execSQLshort(String string) {
        SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(string);
        db.close();
        mDbHelper.close();
    }

    public void saveRow(String name, String target, double value) {
        if (unit.equals("cm")) {
            value /= 2.54;
        }
        if (nameAvailable(name)) {
            SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("target", target);
            values.put("value", value);
            db.insert("settings", null, values);
            db.close();
            mDbHelper.close();
        }
    }

    public double savedValue() {
        Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
        SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String targetText = "";
        switch (target) {
            case "eye1":
            case "eye2": {
                targetText = "eye";
                break;
            }
            default:
                targetText = target;
        }

        Cursor cursor = db.rawQuery("SELECT * FROM settings WHERE name = '" + spinner.getSelectedItem().toString() + "' AND target = '" + targetText + "'", null);
        cursor.moveToFirst();
        double returnVal = cursor.getDouble(cursor.getColumnIndex("value")) * (unit.equals("cm") ? 2.54 : 1);
        cursor.close();
        return returnVal;
    }

    public boolean nameAvailable(String name) {
        if (name.isEmpty()) {
            return false;
        }
        if (name == "Load From:") {
            return false;
        }
        SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {"name", "target", "value"};
        Cursor cursor = db.query("settings", projection, null, null, null, null, null);

        String targetText = "";
        switch (target) {
            case "eye1":
            case "eye2": {
                targetText = "eye";
                break;
            }
            default:
                targetText = target;
        }

        int i = 0;
        for (i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            if (name.equals(cursor.getString(cursor.getColumnIndex("name"))) && targetText.equals(cursor.getString(cursor.getColumnIndex("target")))) {
                return false;
            }
        }
        return true;
    }

    public void saveAll() {
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("precision", accuracy);
        editor.putString("unit", unit);
        editor.putFloat("eye1", (float) eye1);
        editor.putFloat("eye2", (float) eye2);
        editor.putFloat("stride", (float) stride);
        editor.putFloat("cameraHeight", (float) cameraHeight);
        editor.putFloat("cameraAngle", (float) cameraAngle);
        editor.putFloat("offset", (float) offset);
        editor.putFloat("incline", (float) incline);
        editor.commit();
    }

    public void saveInt(String key, int value) {
        Context context = MainActivity.this;
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void saveFloat(String key, double value) {
        Context context = MainActivity.this;
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, (float) value);
        editor.commit();
    }

    public void saveString(String key, String value) {
        Context context = MainActivity.this;
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void deleteSelected(View view) {
        Spinner spinner = (Spinner) findViewById(R.id.saveSpinner);
        if (String.valueOf(spinner.getSelectedItem()) == "Load From:") {
            return;
        }
        SettingsDbHelper mDbHelper = new SettingsDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String targetText = "";
        switch (target) {
            case "eye1":
            case "eye2": {
                targetText = "eye";
                break;
            }
            default:
                targetText = target;
        }

        db.execSQL("DELETE FROM settings WHERE target = '" + targetText + "' AND name = '" + String.valueOf(spinner.getSelectedItem()) + "'");
        setUpSaveSpinner();

        db.close();
        mDbHelper.close();
    }

    // IAP Functions
    public void purchaseItem(int itemNumber) {
        mHelper.launchPurchaseFlow(this, items[itemNumber], 10001, mPurchaseFinishedListener, "mypurchasetoken");
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // handle error here
            } else {
                // does the user have the full version?
                purchases[0] = inventory.hasPurchase(items[0]);
                back(false);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(items[0])) {
                purchases[0] = true;
                back(false);
            }
        }
    };

    public void consumeItem(int itemNumber) {
        currentItem = itemNumber;
        mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if (result.isFailure()) {
                    // Handle failure
                } else {
                    mHelper.consumeAsync(inventory.getPurchase(items[currentItem]), mConsumeFinishedListener);
                }
            }
        });
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                purchases[0] = false;
                back(false);
            } else {
                // handle error
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
