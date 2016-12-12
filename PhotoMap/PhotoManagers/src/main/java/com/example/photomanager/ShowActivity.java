package com.example.photomanager;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.net.Uri;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ShowActivity extends FragmentActivity implements MapDialog.MapDialogListener {
    TextView mText;
    Button mNextButton, mPrevButton, mSaveButton, mRemoveButton;
    ImageView mImageView;
    GoogleMap mMap;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        setGlobalImages();
        createMapView();
        setImage();

        mText = (TextView) findViewById(R.id.test);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mPrevButton = (Button) findViewById(R.id.prevButton);
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mRemoveButton = (Button) findViewById(R.id.removeButton);


        if(mNextButton == null) {

        } else {
            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextImage();
                    setImage();
                }
            });
        }

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevImage();
                setImage();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDialog();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ImageViewActivity.class);
                startActivity(intent);
            }
        });

        if(GlobalList.getGlobalInstance().getCurrMap().getImageList().size() > 0) {
            mText.setText(GlobalList.getGlobalInstance().getCurrImage().getImagePath());
        } else {
            mText.setText("NONE");
        }


    }

    public void nextImage() {
        if(GlobalList.getGlobalInstance().setNextImage()) {
            updateText();
        }
    }

    public void prevImage() {
        if(GlobalList.getGlobalInstance().setPrevImage()) {
            updateText();
        }
    }

    public void updateText() {
        mText.setText(GlobalList.getGlobalInstance().getCurrImage().getImagePath());
    }


    public void setImage() {
        if(GlobalList.getGlobalInstance().getCurrImage() != null) {

            mImageView = ((ImageView) findViewById(R.id.imageView));
            if (mImageView != null) {
                mImageView.setImageURI(Uri.parse(GlobalList.getGlobalInstance().getCurrImage().getImagePath()));
            }
        }
    }


    /* Add the current Map to the end of Global List. Then write to shared prefs */
    private void saveMapToPrefs() {
        List<ImageListBean> currMapList = GlobalList.getGlobalInstance().getCurrMapList();
        currMapList.add(GlobalList.getGlobalInstance().getCurrMap());

        Gson gson = new Gson();
        String json = gson.toJson(currMapList);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("test", json);
        editor.commit();
    }

    /* display the edit mMap name dialog */
    private void showEditDialog(){
        FragmentManager fm = getSupportFragmentManager();
        MapDialog mapNameDialog = MapDialog.newInstance("Set Map Name");
        mapNameDialog.show(fm, "fragment_map_name");
    }

    private void showRemoveDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle("Remove Image");

        // set dialog message
        alertDialogBuilder
                .setMessage("Are You Sure You Want To Remove Image?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        GlobalList.getGlobalInstance().removeImage();
                        updateText();
                        setImage();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /* This is called when 'OK' is pressed on AlertDialog when saving mMap */
    @Override
    public void onFinishMapDialog(String mapName, String mapDescription) {
        /* Check empty string is not working currently */
        if(mapName != null && mapName != " ") {
            GlobalList.getGlobalInstance().getCurrMap().setName(mapName);
        }
        else {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            GlobalList.getGlobalInstance().getCurrMap().setName(currentDateTimeString);
        }

        if(mapDescription != null && mapDescription != " ") {
            GlobalList.getGlobalInstance().getCurrMap().setDescription(mapDescription);
        }
        else {
            GlobalList.getGlobalInstance().getCurrMap().setDescription(" ");
        }

        saveMapToPrefs();
    }

    private void setGlobalImages() {
    ImageListBean currMap = GlobalList.getGlobalInstance().getCurrMap();
    if(!currMap.isEmpty()) {
        GlobalList.getGlobalInstance().setCurrImage(currMap.getImageList().get(0));
        GlobalList.getGlobalInstance().setCurrImageIndex(0);
    }
    }

    private void createMapView() {
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the mMap
         */
        try {
            if(null == mMap){
               mMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();
               GlobalList.getGlobalInstance().setMap(mMap);
                /**
                 * If the mMap is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == mMap) {
                }
            }
        } catch (NullPointerException exception){
        }

        /* Create marker list */
        List<Marker> markerList = new ArrayList<Marker>();
        List<ImageBean> list = GlobalList.getGlobalInstance().getCurrMap().getImageList();

        for (ImageBean img : list) {
            Marker newMark = GlobalList.getGlobalInstance().getMap().addMarker(new MarkerOptions().position(new LatLng(img.getLat(), img.getLng())));
            markerList.add(newMark);
        }

        GlobalList.getGlobalInstance().setMarkerList(markerList);
        if(list.size() > 0) {
            GlobalList.getGlobalInstance().getMap().animateCamera(CameraUpdateFactory.newLatLng(markerList.get(0).getPosition()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}