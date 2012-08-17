package com.threewisedroids.superc4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class BetterSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {

    class DrawingThread extends Thread {
        private final static long frameTime = 1000 / 30;

        @Override
        public void run() {
            long lastTime = SystemClock.uptimeMillis();
            while (running) {
                // Log.d(tag, "Evolving from " + this);
                long elapsed = SystemClock.uptimeMillis() - lastTime;
                lastTime += elapsed;
                if (elapsed < 10 * 1000) {
                    // First, evolve
                    onEvolve(elapsed);
                    // Then, draw
                    Canvas c = null;
                    try {
                        c = holder.lockCanvas(null);
                        if (c != null) {
                            synchronized (holder) {
                                onDraw(c);
                            }
                        }
                    } finally {
                        if (c != null) {
                            holder.unlockCanvasAndPost(c);
                        }
                    }
                }
                if (elapsed < frameTime) {
                    // And finally wait
                    SystemClock.sleep(frameTime - elapsed);
                }
            }
        }

    }

    boolean            running = false;

    SurfaceHolder      holder  = null;

    public final Point size    = new Point();

    DrawingThread      thread  = null;

    public BetterSurfaceView(final Context context) {
        super(context);

        init();
    }

    public BetterSurfaceView(final Context context, final AttributeSet set) {
        super(context, set);

        init();
    }

    public BetterSurfaceView(final Context context, final AttributeSet set,
            final int defStyle) {
        super(context, set, defStyle);

        init();
    }

    public void init() {
        holder = getHolder();
        holder.addCallback(this);

        onInit();
    }

    @Override
    public abstract void onDraw(Canvas c);

    /**
     * Convenience function to ease animation.
     * Override if needed.
     */
    public void onEvolve(final long elapsed) {
    }

    public void onInit() {
    }

    public void onResize() {
    }

    public void surfaceChanged(final SurfaceHolder holder, final int format,
            final int width, final int height) {
        size.set(width, height);
        onResize();
        // Log.d("Tag", "Screen size : " + size);
    }

    public void surfaceCreated(final SurfaceHolder holder) {
        // Starts rendering.
        // Log.d("test", "Surface Created");
        running = true;
        thread = new DrawingThread();
        thread.start();
    }

    public void surfaceDestroyed(final SurfaceHolder holder) {
        // Stops rendering
        running = false;
        try {
            if (thread != null)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}