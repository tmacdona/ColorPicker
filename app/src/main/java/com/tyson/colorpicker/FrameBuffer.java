package com.tyson.colorpicker;

import android.graphics.Color;

/**
 * Created by Tyson on 7/9/2017.
 * Frame buffer for LED array
 *
 * number bytes
 * 4 bytes per per pixel, C-GRB
 */

 class FrameBuffer {

    private byte[][] buffer = null;
    private int[] colorBuffer;
    private byte[] packetHeader = {(byte)0,(byte)0,(byte)0,(byte)16,(byte)-68,(byte)-59,(byte)-92,
            (byte)-68,(byte)0,(byte)0,(byte)0,(byte)16,(byte)0,(byte)0,(byte)0,(byte)9};
    private final int BYTES_PER_PIX = 3;

    FrameBuffer(int pixelCount){

        buffer = new byte[pixelCount][BYTES_PER_PIX];
    }

    void setColors(int[] colors){

        this.colorBuffer = colors;
    }


    byte[] getBufferByteArray(){

        int i = 0;
        PixelBuffer pixelBuffer;

        for (int color : colorBuffer){

            pixelBuffer = new PixelBuffer();

            pixelBuffer.setColor(color);

            buffer[i] = pixelBuffer.getPixel();

            i++;
        }


        return concatenateBuffer();
    }


    private byte[] concatenateBuffer(){

        byte[] output = new byte[(buffer.length * BYTES_PER_PIX) + packetHeader.length];

        System.arraycopy(packetHeader, 0, output, 0, packetHeader.length);
        int pos = packetHeader.length;


        for(byte[] pixel : buffer) {
            System.arraycopy(pixel, 0, output, pos, BYTES_PER_PIX);

            // incement the output end position
            pos += BYTES_PER_PIX;
        }

        return output;
    }




    private class PixelBuffer{
        byte[] data = new byte[BYTES_PER_PIX]; // 3 color components
        //final static int HEADER = 0;
        final static int GREEN = 0;
        final static int RED = 1;
        final static int BLUE = 2;

        private int green;
        private int red;
        private int blue;

        private void setGreen(int green){
            this.green = green;
            data[GREEN] = (byte)green;
        }

        private void setRed(int red){
            this.red = red;
            data[RED] = (byte)red;
        }

        private void setBlue(int blue){
            this.blue = blue;
            data[BLUE] = (byte)blue;
        }

        private void setColor(int color){

            /*setRed((color >> 16)  &0x0ff);
            setGreen((color >> 8) &0x0ff);
            setBlue((color)       &0x0ff);*/

            setRed(Color.red(color));
            setGreen(Color.green(color));
            setBlue(Color.blue(color));
        }

        //the amphorest is P9813, which is HGRB
        // this meathod creates the header
        private int getHeaderValue(){

            int f_b = 0x30 & (blue >> 2);
            int f_g = 0x0C & (green >> 4);
            int f_r = 0x03 & (red >> 6);
            return (0xff ^ f_b ^ f_g ^ f_r);
        }


        byte[] getPixel(){

            return data;
        }
    }
}
