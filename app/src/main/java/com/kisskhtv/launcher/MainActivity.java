package com.kisskhtv.launcher;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.view.ViewConfiguration;

public class MainActivity extends Activity {
    private static final String START_URL = "https://kisskh.do/";
    private FrameLayout root;
    private WebView web;
    private Pointer pointer;
    private View fullscreen;
    private WebChromeClient.CustomViewCallback fullscreenCallback;
    private boolean cursorEnabled = true;
    private float px = 0f, py = 0f;
    private long lastOkDown = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        immersive();
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setSupportZoom(false);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        web.getSettings().setSupportMultipleWindows(false);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();
                // HTTPS navigations stay inside the wrapper; refuse untrusted custom-scheme intents.
                return !("https".equalsIgnoreCase(scheme));
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreen != null) { callback.onCustomViewHidden(); return; }
                fullscreen = view;
                fullscreenCallback = callback;
                pointer.setVisibility(View.GONE);
                web.setVisibility(View.GONE);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
                root.addView(view, lp);
                immersive();
            }
            @Override public void onHideCustomView() { hideFullscreen(false); }
        });
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        pointer = new Pointer();
        root.addView(pointer, new FrameLayout.LayoutParams(-1, -1));
        pointer.setFocusable(false);
        pointer.setClickable(false);
        setContentView(root);
        root.post(() -> { px = root.getWidth() / 2f; py = root.getHeight() / 2f; pointer.invalidate(); });
        web.loadUrl(START_URL);
        web.requestFocus();
        Toast.makeText(this, "D-pad: cursor • OK: click • MENU: cursor mode • Long OK: toggle cursor", Toast.LENGTH_LONG).show();
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void hideFullscreen(boolean notify) {
        if (fullscreen == null) return;
        View v = fullscreen;
        fullscreen = null;
        if (v.getParent() instanceof ViewGroup) ((ViewGroup)v.getParent()).removeView(v);
        web.setVisibility(View.VISIBLE);
        pointer.setVisibility(cursorEnabled ? View.VISIBLE : View.GONE);
        WebChromeClient.CustomViewCallback cb = fullscreenCallback;
        fullscreenCallback = null;
        if (notify && cb != null) cb.onCustomViewHidden();
        immersive();
    }

    private void toggleCursor() {
        cursorEnabled = !cursorEnabled;
        pointer.setVisibility(cursorEnabled && fullscreen == null ? View.VISIBLE : View.GONE);
        Toast.makeText(this, cursorEnabled ? "Cursor enabled" : "Cursor disabled (native page navigation)", Toast.LENGTH_SHORT).show();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO) {
            if (event.getRepeatCount() == 0) toggleCursor();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE)
            return super.onKeyDown(keyCode, event);
        if (fullscreen != null) return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP) { web.scrollBy(0, -web.getHeight() * 3 / 4); return true; }
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) { web.scrollBy(0, web.getHeight() * 3 / 4); return true; }
        if (keyCode == KeyEvent.KEYCODE_F5) { web.reload(); return true; }
        if (!cursorEnabled) return super.onKeyDown(keyCode, event);
        float step = Math.max(18, getResources().getDisplayMetrics().density * (event.getRepeatCount() > 2 ? 45 : 26));
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: px -= step; break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: px += step; break;
            case KeyEvent.KEYCODE_DPAD_UP: py -= step; break;
            case KeyEvent.KEYCODE_DPAD_DOWN: py += step; break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
                if (event.getRepeatCount() == 0) lastOkDown = SystemClock.uptimeMillis();
                
                return true;
            default: return super.onKeyDown(keyCode, event);
        }
        px = Math.min(Math.max(px, 5), root.getWidth() - 5);
        py = Math.min(Math.max(py, 5), root.getHeight() - 5);
        pointer.invalidate();
        return true;
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (fullscreen == null && cursorEnabled && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
            if (lastOkDown > 0) {
                if (SystemClock.uptimeMillis() - lastOkDown >= ViewConfiguration.getLongPressTimeout()) toggleCursor();
                else clickAtPointer();
            }
            lastOkDown = 0;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void clickAtPointer() {
        long t = SystemClock.uptimeMillis();
        web.dispatchTouchEvent(MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, px, py, 0));
        web.dispatchTouchEvent(MotionEvent.obtain(t, t + 70, MotionEvent.ACTION_UP, px, py, 0));
    }

    @Override public void onBackPressed() {
        if (fullscreen != null) { hideFullscreen(true); return; }
        if (web != null && web.canGoBack()) { web.goBack(); return; }
        super.onBackPressed();
    }

    @Override protected void onPause() { super.onPause(); if (web != null) web.onPause(); }
    @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); immersive(); }
    @Override protected void onDestroy() {
        if (fullscreen != null) hideFullscreen(true);
        if (web != null) { root.removeView(web); web.destroy(); web = null; }
        super.onDestroy();
    }

    private class Pointer extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Pointer() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas canvas) {
            if (!cursorEnabled) return;
            paint.setStyle(Paint.Style.FILL); paint.setColor(Color.argb(115, 0, 0, 0));
            canvas.drawCircle(px + 2, py + 2, 17, paint);
            paint.setColor(Color.rgb(255, 206, 70)); canvas.drawCircle(px, py, 11, paint);
            paint.setColor(Color.BLACK); canvas.drawCircle(px, py, 4, paint);
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(2); paint.setColor(Color.WHITE);
            canvas.drawCircle(px, py, 12, paint); paint.setStyle(Paint.Style.FILL);
        }
    }
}
