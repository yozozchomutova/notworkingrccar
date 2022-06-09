package eu.yozozchomutova;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket btSocket = null;
    public static OutputStream outputStream;

    private SeekBar steeringSK, accelerationSK;
    private ImageView steeringIV, accelerationIV;
    private ConstraintLayout.LayoutParams accelerationIVP;

    private TextView delayingTXT;

    private ImageButton lightsToggle;
    private boolean lightsToggled = false;

    private static int steerCommandDelay, accelerateCommandDelay;
    private static int SCREEN_WIDTH, SCREEN_HEIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initiate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide top bar
        getSupportActionBar().hide();

        //Get display size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        //System.out.println(btAdapter.getBondedDevices());

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("20:15:03:27:58:01");

        steeringSK = findViewById(R.id.steeringSK);
        accelerationSK = findViewById(R.id.accelerationSK);

        steeringIV = findViewById(R.id.steeringIV);
        accelerationIV = findViewById(R.id.accelerationIV);
        accelerationIVP = (ConstraintLayout.LayoutParams) findViewById(R.id.accelerationIV).getLayoutParams();

        delayingTXT = findViewById(R.id.delayingTXT);

        lightsToggle = findViewById(R.id.lights);

        int counter = 0;
        do {
            try {
                btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                System.out.println(btSocket);
                btSocket.connect(); //TODO on failed
                System.out.println(btSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        } while (!btSocket.isConnected() && counter < 50);

        try {
            outputStream = btSocket.getOutputStream();
            outputStream.write(new byte[]{77, 77, 77});
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = null;
        try {
            inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());

            for (int i = 0; i < 26; i++) {

//                byte b = (byte) inputStream.read();
//                System.out.println((char) b);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        steeringSK.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) {} @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if (steerCommandDelay <= 0) {
                        steerCommandDelay = 10;

                        int rotation = (int)((progress-90)/1f);
                        outputStream.write(("ang;" + (rotation) + ";").getBytes());
                        steeringIV.setRotation(rotation);
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        });

        accelerationSK.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onStartTrackingTouch(SeekBar seekBar) {} @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    if (accelerateCommandDelay <= 0) {
                        accelerateCommandDelay = 10;

                        outputStream.write(("acc;" + progress + ";").getBytes());
                        accelerationIVP.matchConstraintPercentHeight = ((float) progress) / 256f;
                        accelerationIV.setLayoutParams(accelerationIVP);

                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        });

        lightsToggle.setOnClickListener(v -> {
            try {
                lightsToggled = !lightsToggled;
                outputStream.write(("lig;" + (lightsToggled ? "y" : "n") + ";").getBytes());

                //Change icon
                lightsToggle.setImageDrawable(lightsToggled ?
                        ContextCompat.getDrawable(this, android.R.drawable.presence_online) :
                        ContextCompat.getDrawable(this, android.R.drawable.presence_busy)
                );
            } catch (Exception e) {e.printStackTrace();}
        });

        Thread timerHandler = new Thread(() -> {
            while (true) {
                try { Thread.sleep(10); } catch (Exception e) {}
                steerCommandDelay--;
                accelerateCommandDelay--;

                runOnUiThread(() -> delayingTXT.setText("Steer delay: " + steerCommandDelay + "\nAccel delay: " + accelerateCommandDelay));

            }
        });
        timerHandler.start();

//        try {
//            btSocket.close();
//            System.out.println(btSocket.isConnected());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}