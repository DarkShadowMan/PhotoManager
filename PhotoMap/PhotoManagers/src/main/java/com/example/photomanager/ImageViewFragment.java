package com.example.photomanager;

import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by lwh on 2016/12/11.
 */

public class ImageViewFragment extends Fragment{
    private ImageView mImageView;

    @Override
    public void onCreate(Bundle onSavedInstanceState){
        super.onCreate(onSavedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceSate){
        View v = inflater.inflate(R.layout.fragment_image,container,false);
        mImageView = (ImageView)v.findViewById(R.id.Image);
        mImageView.setImageURI(Uri.parse(GlobalList.getGlobalInstance().getCurrImage().getImagePath()));
        return v;
    }
}
