/*
    This content was created by Maximoff and is copyrighted against plagiarism.
    https://maximoff.su/
*/
package ru.maximoff.aurora.store;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class DeepLink extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = getIntent().getData();
        String format;
        if (uri.getPath().equals("/store/apps/dev")) {
            format = String.format("https://play.google.com/store/apps/dev?id=%s", uri.getQueryParameter("id"));
        } else {
            format = String.format("https://play.google.com/store/apps/details?id=%s", uri.getQueryParameter("id"));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.aurora.store", "com.aurora.store.MainActivity");
        intent.setData(Uri.parse(format));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
