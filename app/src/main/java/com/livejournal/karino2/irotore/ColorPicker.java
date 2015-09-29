package com.livejournal.karino2.irotore;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

/**
 * Created by karino on 9/6/15.
 */
public class ColorPicker {
    public static class WheelParam
    {
        public double WheelRad;
        public int WheelX;
        public int WheelY;

        public WheelParam()
        {
            WheelRad = 0;
            WheelX = WheelY = 0;
        }
    }


    // 色相位置 (前景)
    private double mWheelRad = 0;
    private int mWheelX = 0;
    private int mWheelY = 0;

    // ホイール幅
    private int mWheelH = 10;

    private boolean mHueChanging = false;
    private boolean mSVChanging = false;

    private Bitmap mColorWheel = null; // 色相環
    private Bitmap mColorGrad = null; // SV

    private int mHeight = 0;

    private int height() { return mHeight; }


    private boolean mSlimWheel = false;

    Paint huePaint = new Paint();

    public ColorPicker() {
        this(false);
    }
    public ColorPicker(boolean isSlimWheel) {
        mSlimWheel = isSlimWheel;
        huePaint.setAntiAlias(true);
    }

    private static int blend( int destcolor, int srcColor, int alpha )
    {
        int dr = (destcolor >> 16) & 255;
        int dg = (destcolor >> 8) & 255;
        int db = (destcolor >> 0) & 255;

        int sr = (srcColor >> 16) & 255;
        int sg = (srcColor >> 8) & 255;
        int sb = (srcColor >> 0) & 255;

        int alpha2 = 255 - alpha;
        int r = (sr * alpha2 + dr * alpha) / 255;
        int g = (sg * alpha2 + dg * alpha) / 255;
        int b = (sb * alpha2 + db * alpha) / 255;

        return 0xFF000000 + (r << 16) + (g << 8) + b;
    }

    private static Point nearestPos( Bitmap bmp, int color )
    {
        Point p = new Point();

        int len = 999999999;
        int sr = (color & 0x00FF0000) >> 16;
        int sg = (color & 0x0000FF00) >> 8;
        int sb = (color & 0x000000FF);

        for (int j=0; j<bmp.getHeight(); j++)
        {
            for (int i=0; i<bmp.getWidth(); i++)
            {
                int c = bmp.getPixel( i, j );
                int r = (c & 0x00FF0000) >> 16;
                int g = (c & 0x0000FF00) >> 8;
                int b = (c & 0x000000FF);
                int dif = (r - sr)*(r - sr) + (g - sg)*(g - sg) + (b - sb)*(b - sb);
                if (dif < len)
                {
                    p.x = i;
                    p.y = j;
                    len = dif;
                }
            }
        }

        return p;
    }


    public WheelParam swapWheelParam(WheelParam newParam)
    {
        WheelParam returnParam = new WheelParam();

        // 座標入れ替え
        double tmpd = mWheelRad;
        mWheelRad = newParam.WheelRad;
        returnParam.WheelRad = tmpd;

        int tmp = mWheelX;
        mWheelX = newParam.WheelX;
        returnParam.WheelX = tmp;

        tmp = mWheelY;
        mWheelY = newParam.WheelY;
        returnParam.WheelY = tmp;

        return returnParam;
    }

    public void setColor(int color)
    {
        float[] hsv = { 0, 0, 0 };
        Color.colorToHSV(color, hsv);
        mWheelRad = hsv[0] * 2 * Math.PI / 360;
        updateSV();

        // 一番近い色の位置を探す
        Point p = nearestPos(mColorGrad, color);
        mWheelX = p.x;
        mWheelY = p.y;

    }

    // 現在のHue位相
    public int currentHue()
    {
        double rad = mWheelRad;
        if (rad < 0) rad += Math.PI*2;

        float[] hsv = { (float)(360*rad / (Math.PI*2)), 1,1 };
        int c = Color.HSVToColor(hsv);
        return c;
    }

    public void updateHue( )
    {
        mColorWheel.eraseColor(0xFF000000);

        Canvas canvas2 = new Canvas( mColorWheel );
        huePaint.setColor(0xFFFFFFFF);
        RectF rect = new RectF( 0, 0, height(), height() );
        canvas2.drawOval( rect, huePaint );

        huePaint.setColor(0xFF000000);
        rect = new RectF( mWheelH , mWheelH, height() - mWheelH, height() - mWheelH );
        canvas2.drawOval( rect, huePaint );

        int wheelWidth = mColorWheel.getWidth();
        int[] pixels = new int[ wheelWidth ];
        int mx = mColorWheel.getWidth()/2;
        int my = mColorWheel.getHeight()/2;

        // Hue
        for (int j=0; j<mColorWheel.getHeight(); j++)
        {
            mColorWheel.getPixels( pixels, 0, wheelWidth, 0, j, wheelWidth, 1 );

            for (int i=0; i<mColorWheel.getWidth(); i++)
            {
                //int sc = mColorWheel.getPixel( i,  j );
                int sc = pixels[i];

                if (sc == 0xFF000000)
                {
                    // 完全透明部
                    sc = 0x00000000;
                    //mColorWheel.setPixel( i,  j, sc );
                    pixels[i] = sc;
                    continue;
                }

                // HSV
                double rad = Math.atan2( j-my, i-mx );
                if (rad < 0) rad += Math.PI*2;
                float[] f = { (float)(360*rad / (Math.PI*2)), 1,1 };
                int c = Color.HSVToColor( f );
                sc = sc << 24; // B -> Alpha
                c = c & 0x00FFFFFF;
                sc = sc | c;

                //mColorWheel.setPixel( i,  j, sc );
                pixels[i] = sc;
            }

            mColorWheel.setPixels( pixels, 0, wheelWidth, 0, j, wheelWidth, 1 );
        }
    }

    public void updateSV()
    {
        mColorGrad.eraseColor(0xFFFFFFFF);
        int hue = currentHue();

        int gradWidth = mColorGrad.getWidth();
        int[] pixels = new int[ gradWidth ];

        // 補完用
        for (int i=0; i<mColorGrad.getWidth(); i++)
        {
            // 最上列
            int a = 255 * i / mColorGrad.getWidth();
            int color = blend( hue, 0xFFFFFFFF, a );
            mColorGrad.setPixel( i, 0, color );
        }
        for (int i=0; i<mColorGrad.getHeight(); i++)
        {
            // 最左列
            int a = 255 * i / mColorGrad.getHeight();
            int color = blend( 0xFF000000, mColorGrad.getPixel( 0, 0 ), a );
            mColorGrad.setPixel( 0, i, color );
        }
        for (int i=0; i<mColorGrad.getHeight(); i++)
        {
            // 最右列
            int a = 255 * i / mColorGrad.getHeight();
            int x = mColorGrad.getWidth() - 1;
            int color = blend( 0xFF000000, mColorGrad.getPixel( x, 0 ), a );
            mColorGrad.setPixel( x, i, color );
        }

        // 補完
        for (int j=1; j<mColorGrad.getHeight(); j++)
        {
            int c2 = mColorGrad.getPixel( mColorGrad.getWidth()-1, j );
            int c1 = mColorGrad.getPixel( 0, j );

            mColorGrad.getPixels( pixels, 1, gradWidth, 1, j, gradWidth-2, 1 );
            for (int i=1; i<mColorGrad.getWidth()-1; i++)
            {
                int a = 255 * i / mColorGrad.getHeight();
                int color = blend( c2, c1, a );

                //mColorGrad.setPixel( i, j, color );
                pixels[i] = color;
            }
            mColorGrad.setPixels( pixels, 1, gradWidth, 1, j, gradWidth-2, 1 );
        }
    }

    public void updatePanel(Canvas canvas, int offsetX, int offsetY, Paint paint)
    {

        // 色相環合成
        canvas.drawBitmap( mColorWheel, offsetX, offsetY, paint );

        // SV合成
        int svx = height()/2 - mColorGrad.getWidth()/2;
        int svy = height()/2 - mColorGrad.getHeight()/2;
        canvas.drawBitmap(mColorGrad, offsetX+svx, offsetY+svy, paint);

        // 色相位置合成
        int gw = mColorGrad.getWidth()/2;
        int gh = mColorGrad.getHeight()/2;
        float px = (height()/2 - mWheelH/2) * (float)Math.cos( (float)mWheelRad );
        float py = (height()/2 - mWheelH/2) * (float)Math.sin( (float)mWheelRad );
        canvas.drawCircle( offsetX+svx + gw + px, offsetY+svy + gh + py, mWheelH/4, paint );

        // SV位置合成
        canvas.drawCircle( offsetX+svx + mWheelX, offsetY+svy + mWheelY, mWheelH/4, paint );
    }

    // 色相ホイールの中？
    private boolean insideHue( float x, float y )
    {
        x -= height()/2; // Hue中心からの座標
        y -= height()/2;

        double dist = x*x + y*y;
        if (dist != 0) dist = Math.sqrt( dist );

        int max = height()/2 + mWheelH/3; // 外は甘めに
        int min = height()/2 - mWheelH;
        if ((min <= dist) && (dist <= max)) return true;
        return false;
    }

    // SVグラデの中？
    private boolean insideSV( float x, float y )
    {
        x -= (height()/2 - mColorGrad.getWidth()/2); // SV座標に
        y -= (height()/2 - mColorGrad.getHeight()/2);

        if (x < 0) return false;
        if (y < 0) return false;
        if (x >= mColorGrad.getWidth()) return false;
        if (y >= mColorGrad.getHeight()) return false;
        return true;
    }

    private double getWheelRad( float x, float y )
    {
        float px = x - height()/2;
        float py = y - height()/2;

        return Math.atan2( py, px );
    }

    public void resize(int newHeight)
    {
        mHeight = newHeight;
        if(mSlimWheel) {
            mWheelH = (int) (0.5 * (mHeight / 4));
        } else {
            mWheelH = (int) (0.65 * (mHeight / 4));
        }

        int gw = (int)( (double)(height() - mWheelH*2) / Math.sqrt(2) );
        mColorWheel = Bitmap.createBitmap( height(), height(), Bitmap.Config.ARGB_8888 );
        mColorGrad = Bitmap.createBitmap( gw, gw, Bitmap.Config.ARGB_8888 );
        mWheelY = gw - 1;
    }

    public boolean onTouchDown( int ix, int iy ) {
        // 色範囲？
        if (insideHue( ix, iy ))
        {
            mHueChanging = true;
            mWheelRad = getWheelRad( ix, iy );
            updateSV();
            return true;
        }

        if (insideSV( ix, iy ))
        {
            mSVChanging = true;
            onTouchMove(ix, iy); // To Update mWheelX and mWheelY.
            return true;
        }
        return false;
    }

    public boolean onTouchMove( int ix, int iy ) {
        // 色Hue
        if (mHueChanging)
        {
            mWheelRad = getWheelRad( ix, iy );
            updateSV();
            return true;
        }

        // 色SV
        if (mSVChanging)
        {
            ix -= (height()/2 - mColorGrad.getWidth()/2); // SV座標に
            iy -= (height()/2 - mColorGrad.getHeight()/2);

            if (ix < 0) ix = 0;
            if (iy < 0) iy = 0;
            if (ix >= mColorGrad.getWidth()) ix = mColorGrad.getWidth() - 1;
            if (iy >= mColorGrad.getHeight()) iy = mColorGrad.getHeight() - 1;
            mWheelX = ix;
            mWheelY = iy;

            return true;
        }

        return false;

    }

    public int getChosenColor()
    {
        return mColorGrad.getPixel( mWheelX, mWheelY );
    }

    public void onTouchUp( int x, int y )
    {
        mHueChanging = false;
        mSVChanging = false;
    }

    public int getWidth() {
        return mColorWheel.getWidth();
    }

}
