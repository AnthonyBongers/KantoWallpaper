package com.example.anthonybongers.wallpaper;

import android.graphics.*;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.*;
import android.view.SurfaceHolder;
import org.json.*;
import java.io.InputStream;
import java.util.*;

public class TheWallpaperService extends WallpaperService {
    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new TheWallpaperEngine();
    }

    private class TheWallpaperEngine extends WallpaperService.Engine {
        private final int frameDuration = 16;

        private Handler handler = new Handler();
        private Bitmap image;
        private Paint paint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        private Runnable drawWallpaper = new Runnable() {
            public void run() {
                draw();
            }
        };

        PointF screenSize = new PointF();
        PointF offset = new PointF();
        PointF position = new PointF();
        PointF velocity = new PointF();

        float drawScale = 0.0f;

        // JSON VARS
        String imagePath;
        float speed = 1.0f;
        float viewport = 250.0f;
        Vector<PointF> nodes = new Vector<>();
        int nodeIndex = 0;
        int tint = 0x66000000;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            setOffsetNotificationsEnabled(false);
            setTouchEventsEnabled(false);
            surfaceHolder.setFormat(PixelFormat.RGB_565);
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            handler.removeCallbacks(drawWallpaper);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            DisplayMetrics dm = getResources().getDisplayMetrics();

            boolean changeMap = (!visible && screenSize.x == dm.widthPixels);

            screenSize.set(dm.widthPixels, dm.heightPixels);

            if (changeMap) loadMap();

            float aspect = Math.min(screenSize.x / screenSize.y, screenSize.y / screenSize.x);
            offset.set((viewport / 2.0f) * drawScale, ((aspect * viewport) / 2.0f) * drawScale);
            if (screenSize.x < screenSize.y) offset.set(offset.y, offset.x);

            if (visible) handler.post(drawWallpaper);
            else handler.removeCallbacks(drawWallpaper);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            onVisibilityChanged(true);
        }

        private void loadMap() {
            try {
                Random rand = new Random();

                InputStream is = getAssets().open("wallpapers.json");
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                is.close();

                JSONObject json = new JSONObject(new String(buffer, "UTF-8"));
                JSONArray jWallpapers = json.getJSONArray("wallpapers");

                JSONObject jRegion = (JSONObject)jWallpapers.get(rand.nextInt(jWallpapers.length()));
                if (isPreview()) jRegion = (JSONObject)jWallpapers.get(3);

                viewport = (float)jRegion.getInt("viewport");
                drawScale = Math.max(screenSize.x, screenSize.y) / viewport;

                speed = (float)jRegion.getDouble("speed") * drawScale;
                imagePath = jRegion.getString("image");

                JSONArray jNodes = jRegion.getJSONArray("nodes");

                nodes.clear();
                for (int i = 0; i < jNodes.length(); i++) {
                    JSONObject node = jNodes.getJSONObject(i);

                    nodes.add(new PointF((float)node.getDouble("x") * drawScale, (float)node.getDouble("y") * drawScale));
                }

                nodeIndex = rand.nextInt(nodes.size());
                if (rand.nextBoolean()) Collections.reverse(nodes);

                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inPreferredConfig = Bitmap.Config.RGB_565;
                InputStream istr = getAssets().open(imagePath);
                image = BitmapFactory.decodeStream(istr, new Rect(), op);
                image = Bitmap.createScaledBitmap(image, (int)(image.getWidth() * drawScale), (int)(image.getHeight() * drawScale), false);
                istr.close();

                Bitmap result = Bitmap.createBitmap(image.getWidth(), image.getHeight(), image.getConfig());
                Canvas c = new Canvas(result);
                c.drawBitmap(image, 0, 0, paint);
                paint.setColor(tint);
                c.drawRect(0.0f, 0.0f, image.getWidth(), image.getHeight(), paint);
                paint.setColor(0xFFFFFFFF);

                image = result;

                position.set(nodes.get(nodeIndex));
                calcNextPath();
            }
            catch (Exception ex) { }
        }

        private PointF nextWaypoint() {
            return nodes.get((nodeIndex + 1) % nodes.size());
        }

        private void calcNextPath() {
            PointF next = nextWaypoint();
            velocity.set(position.x - next.x, position.y - next.y);

            float length = velocity.length();
            velocity.set((velocity.x / length) * speed, (velocity.y / length) * speed);
        }

        private void draw() {
            if (!isVisible()) return;

            long begin = System.nanoTime();

            if (nodes.size() > 1) {
                PointF next = nextWaypoint();
                position.offset(-velocity.x, -velocity.y);
                boolean doneTween = PointF.length(position.x - next.x, position.y - next.y) < (0.95f * speed);

                if (doneTween) {
                    nodeIndex++;
                    position.set(next);
                    calcNextPath();
                }
            }

            Canvas canvas = getSurfaceHolder().lockCanvas();
            canvas.drawBitmap(image, offset.x - position.x, offset.y - position.y, paint);
            getSurfaceHolder().unlockCanvasAndPost(canvas);

            long end = Math.min((System.nanoTime() - begin) / 1000000, frameDuration);

            handler.removeCallbacks(drawWallpaper);
            handler.postDelayed(drawWallpaper, frameDuration - end);
        }
    }
}
