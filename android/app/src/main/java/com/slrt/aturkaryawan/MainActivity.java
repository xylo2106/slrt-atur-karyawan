package com.slrt.aturkaryawan;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Atur Karyawan – PT SLRT.
 * Membungkus app GitHub Pages dalam WebView, jadi setiap update app langsung terpakai
 * tanpa memasang APK baru. Data & token tersimpan di penyimpanan WebView milik APK ini.
 */
public class MainActivity extends Activity {

    static final String HOST = "xylo2106.github.io";
    static final String PATH = "/slrt-atur-karyawan";
    static final String HOME = "https://" + HOST + PATH + "/";
    static final int REQ_CAMERA = 7;

    private WebView web;
    private PermissionRequest pendingPermission;
    private volatile String currentUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setCacheMode(isOnline() ? WebSettings.LOAD_NO_CACHE : WebSettings.LOAD_CACHE_ELSE_NETWORK);
        s.setUserAgentString(s.getUserAgentString() + " AturKaryawanAPK/" + appVersion());
        CookieManager.getInstance().setAcceptCookie(true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (isOwnPage(u)) return false;
                openExternal(u);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                currentUrl = url == null ? "" : url;
                view.getSettings().setCacheMode(isOnline() ? WebSettings.LOAD_NO_CACHE : WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                currentUrl = url == null ? "" : url;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    view.loadDataWithBaseURL(null, offlinePage(), "text/html", "utf-8", null);
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean wantsCamera = false;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) wantsCamera = true;
                    }
                    Uri o = request.getOrigin();
                    if (!wantsCamera || o == null || !HOST.equals(o.getHost())) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                    } else {
                        pendingPermission = request;
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
                    }
                });
            }
        });

        web.addJavascriptInterface(new Bridge(), "AturAndroid");

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(startUrl(getIntent()));
        }
    }

    /** Dipanggil dari halaman app untuk menyimpan file (mis. rekap Excel) ke folder Download. */
    class Bridge {
        @JavascriptInterface
        public String saveFile(String name, String base64, String mime) {
            try {
                if (!isOwnPage(Uri.parse(currentUrl))) return "err:origin";
                String safe = name == null ? "file" : name.replaceAll("[^A-Za-z0-9._ -]", "_");
                if (!safe.matches("(?i).+\\.(xlsx|csv|pdf|txt)$")) return "err:type";
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                if (data.length == 0 || data.length > 25 * 1024 * 1024) return "err:size";
                final String type = mime == null || mime.isEmpty() ? "application/octet-stream" : mime;
                if (Build.VERSION.SDK_INT >= 29) {
                    ContentValues v = new ContentValues();
                    v.put(MediaStore.Downloads.DISPLAY_NAME, safe);
                    v.put(MediaStore.Downloads.MIME_TYPE, type);
                    v.put(MediaStore.Downloads.IS_PENDING, 1);
                    final Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                    if (uri == null) return "err:insert";
                    try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                        if (os == null) return "err:stream";
                        os.write(data);
                    }
                    v.clear();
                    v.put(MediaStore.Downloads.IS_PENDING, 0);
                    getContentResolver().update(uri, v, null, null);
                    runOnUiThread(() -> openSaved(uri, type, safe));
                    return "ok:" + safe;
                }
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) return "err:dir";
                File f = new File(dir, safe);
                try (FileOutputStream fo = new FileOutputStream(f)) { fo.write(data); }
                final String path = f.getAbsolutePath();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Tersimpan: " + path, Toast.LENGTH_LONG).show());
                return "ok:" + path;
            } catch (Exception e) {
                return "err:" + e.getClass().getSimpleName();
            }
        }

        /** Kirim teks pesan ke WeChat ("Kirim ke teman"). Teks juga sudah disalin oleh halaman. */
        @JavascriptInterface
        public String shareWeChat(String text) {
            if (!isOwnPage(Uri.parse(currentUrl))) return "err:origin";
            if (text == null || text.isEmpty() || text.length() > 20000) return "err:text";
            final String t = text;
            PackageManager pm = getPackageManager();
            try { pm.getPackageInfo(WX, 0); } catch (Exception e) { return "nowx"; }
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, t);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    i.setClassName(WX, WX + ".ui.tools.ShareImgUI");
                    startActivity(i);
                    return;
                } catch (Exception ignored) { }
                try {
                    i.setComponent(null);
                    i.setPackage(WX);
                    startActivity(i);
                    return;
                } catch (Exception ignored) { }
                try {
                    Intent l = getPackageManager().getLaunchIntentForPackage(WX);
                    if (l != null) {
                        startActivity(l);
                        Toast.makeText(MainActivity.this, "Pesan sudah disalin, tempel di obrolan WeChat", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception ignored) { }
            });
            return "ok";
        }
    }

    private static final String WX = "com.tencent.mm";

    private void openSaved(Uri uri, String type, String name) {
        Toast.makeText(this, "Tersimpan di folder Download: " + name, Toast.LENGTH_LONG).show();
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(uri, type);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Pasang aplikasi Excel / Google Sheets / WPS untuk membuka file ini", Toast.LENGTH_LONG).show();
        }
    }

    private String appVersion() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; }
        catch (Exception e) { return "1"; }
    }

    private static boolean isOwnPage(Uri u) {
        if (u == null) return false;
        String p = u.getPath() == null ? "" : u.getPath();
        return "https".equals(u.getScheme()) && HOST.equals(u.getHost()) && p.startsWith(PATH);
    }

    private String startUrl(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && isOwnPage(intent.getData())) {
            return intent.getData().toString();   // link penghubung dari QR (#hubung=...)
        }
        return HOME;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && isOwnPage(intent.getData())) {
            web.loadUrl(intent.getData().toString());
        }
    }

    private void openExternal(Uri u) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, u);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka link ini", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return true;
        }
    }

    private String offlinePage() {
        return "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"
             + "<style>body{font-family:sans-serif;display:flex;flex-direction:column;align-items:center;justify-content:center;"
             + "height:100vh;margin:0;background:#f7f8fa;color:#16181d;text-align:center;padding:24px;box-sizing:border-box}"
             + "h2{color:#13295e;margin:0 0 8px}p{color:#6b7080;margin:0 0 20px}"
             + "a{background:#13295e;color:#fff;text-decoration:none;padding:13px 22px;border-radius:12px;font-weight:700}</style></head>"
             + "<body><h2>Tidak ada koneksi</h2><p>App perlu internet saat pertama dibuka.<br>Periksa data seluler / Wi-Fi lalu coba lagi.</p>"
             + "<a href='" + HOME + "'>Coba lagi</a></body></html>";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (requestCode == REQ_CAMERA && pendingPermission != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                pendingPermission.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            } else {
                pendingPermission.deny();
            }
            pendingPermission = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
