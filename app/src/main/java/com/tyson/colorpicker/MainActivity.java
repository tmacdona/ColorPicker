package com.tyson.colorpicker;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.AlphaSlider;
import com.flask.colorpicker.slider.LightnessSlider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private int mColor;
    private DatagramSocket mSocket = null;
    private InetAddress mLocalAddress = null;
    private int mServerPort = 2112;
    private String mServerAddress = /*"ala-gws-lx3";*/ /*"192.168.2.237";*/ "esp-nodemcu3";

    private final int PIXEL_COUNT = 5;
    private int[] mColorBuffer = new int[PIXEL_COUNT];
    private int mColorBufferPointer = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int j = 0; j < PIXEL_COUNT; j++) {
            mColorBuffer[j] = 0;
        }

        final ColorPickerView colorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int i) {

                Log.d(TAG, "Color Changed: " + i);

                mColor = i;

                colorPickerView.setBackgroundColor(i);

                mColorBuffer[mColorBufferPointer] = i;
                mColorBufferPointer = (mColorBufferPointer+1)%PIXEL_COUNT;


                sendNewColor();
            }
        });
        LightnessSlider lightnessSlider = (LightnessSlider) findViewById(R.id.v_lightness_slider);
        lightnessSlider.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });
        AlphaSlider alphaSlider = (AlphaSlider) findViewById(R.id.v_alpha_slider);
    }

    private void sendNewColor() {

        if(mSocket == null){

            try {
                mSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();

                Log.e(TAG, "Cannot create socket: " + e.getMessage());

            }
        }

        if(mLocalAddress == null) {

            mLocalAddress = getInetAddressByName(mServerAddress);
        }


        FrameBuffer frameBuffer = new FrameBuffer(PIXEL_COUNT);
        frameBuffer.setColors(mColorBuffer);

        //String messageStr="Hello Android!";
        byte[] frame = frameBuffer.getBufferByteArray(); //hexStringToByteArray("00000011bcc5a4bc000000100000000900ff00ff00000000ff"); //messageStr.getBytes();


        byte[] frameMessage = prependFrameHeader(frame);

        byte[] packetMessage = setPacketMessage(frameMessage);

        if (mSocket != null && mLocalAddress != null) {
            final DatagramPacket p = new DatagramPacket(packetMessage, packetMessage.length, mLocalAddress, mServerPort);


            new Thread(new Runnable() {

                @Override
                public void run() {

                    //TODO run in background
                    try {
                        mSocket.send(p);
                        Log.d(TAG, "Packet sent: " + p.getData());

                    } catch (IOException e) {
                        e.printStackTrace();

                        Log.e(TAG, "Cannot create packet: " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    private byte[] setPacketMessage(byte[] input) {

        int numHeaderBytes = 8;
        byte[] output = new byte[numHeaderBytes + input.length];

        byte[] inputLength = ByteBuffer.allocate(4).putInt(input.length).array();
        System.arraycopy(inputLength, 0, output, 0, 4);

        System.arraycopy(input, 0, output, numHeaderBytes, input.length);

        return output;
    }

    private byte[] prependFrameHeader(byte[] input) {

        int numHeaderBytes = 8;
        byte[] output = new byte[numHeaderBytes + input.length];

        output[3] = 0x10;

        byte[] inputLength = ByteBuffer.allocate(4).putInt(input.length).array();
        System.arraycopy(inputLength, 0, output, 4, 4);


        System.arraycopy(input, 0, output, numHeaderBytes, input.length);

        return output;
    }

    public static InetAddress getInetAddressByName(String name)
    {
        AsyncTask<String, Void, InetAddress> task = new AsyncTask<String, Void, InetAddress>()
        {

            @Override
            protected InetAddress doInBackground(String... params)
            {
                try {
                    return InetAddress.getByName(params[0]);

                } catch (UnknownHostException e) {
                    Log.e(TAG, "Cannot create packet: " + e.getMessage());
                    return null;
                }
            }
        };

        try{
            return task.execute(name).get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            return null;
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        int j;
        for (int i = 0; i < len; i += 2) {

            j = (Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16);
            data[i / 2] = (byte)j;

            /*data[i/2] = Byte.decode("0xbc" *//*+ s.substring(i, i+2)*//*);*/
        }
        return data;
    }
}
