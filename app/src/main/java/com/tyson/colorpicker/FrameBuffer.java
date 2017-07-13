package com.tyson.colorpicker;

import android.graphics.Color;

/**
 * Created by Tyson on 7/9/2017.
 * Frame buffer for LED array
 *
 * number bytes
 * 4 bytes per per pixel, C-GRB
 */

public class FrameBuffer {

    private final int mPixelSize;
    private PixelBuffer[] buffer;
    private int[] colorBuffer;

    public FrameBuffer(int pixelSize){

        this.mPixelSize = pixelSize;

        buffer = new PixelBuffer[pixelSize];
    }

    public void setColors(int[] colors){

        this.colorBuffer = colors;
    }


/*    public byte[] getBufferByteArray(){

        int i = 0;
        PixelBuffer pixelBuffer;

        for (int color : colorBuffer){

            pixelBuffer = new PixelBuffer();

        }

        return buffer;
    }*/







    public class PixelBuffer{
        byte[] data;
        final static int HEADER = 0;
        final static int GREEN = 1;
        final static int RED = 2;
        final static int BLUE = 3;

        private int green;
        private int red;
        private int blue;

        /*def pixelHeader (self, r, g, b):
       f_b = 0x30 & (b >> 2)
       f_g = 0x0C & (g >> 4)
       f_r = 0x03 & (r >> 6)
       return (0xff ^ f_b ^ f_g ^ f_r)*/

        public void setGreen(int green){
            this.green = green;
            data[GREEN] = (byte)green;
        }

        public void setRed(int red){
            this.red = red;
            data[RED] = (byte)red;
        }

        public void setBlue(int blue){
            this.blue = blue;
            data[BLUE] = (byte)blue;
        }

        public void setColor(int color){

            setRed((color >> 16)  &0x0ff);
            setGreen((color >> 8) &0x0ff);
            setBlue((color)       &0x0ff);
        }

        private int getHeaderValue(){

            int f_b = 0x30 & (blue >> 2);
            int f_g = 0x0C & (green >> 4);
            int f_r = 0x03 & (red >> 6);
            return (0xff ^ f_b ^ f_g ^ f_r);
        }


        public byte[] getPixel(){

            data[HEADER] = (byte)getHeaderValue();

            return data;
        }
    }
}
