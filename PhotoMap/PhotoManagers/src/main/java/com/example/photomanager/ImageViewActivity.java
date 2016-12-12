package com.example.photomanager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;


/**
 * Created by lwh on 2016/12/11.
 */

public class ImageViewActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.activity_frg);

        if (fragment == null) {
            fragment = new ImageViewFragment();
            fm.beginTransaction()
                    .add(R.id.activity_frg, fragment)
                    .commit();
        }
    }
}
