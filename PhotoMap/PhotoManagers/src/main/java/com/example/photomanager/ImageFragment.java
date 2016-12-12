package com.example.photomanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class ImageFragment extends Fragment implements MapDialog.MapDialogListener {
    TextView text;
    Button nextButton, prevButton, saveButton, removeButton;
    View myFragmentView;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_display, container, false);
        text = (TextView) myFragmentView.findViewById(R.id.test);
        nextButton = (Button) myFragmentView.findViewById(R.id.nextButton);
        prevButton = (Button) myFragmentView.findViewById(R.id.prevButton);
        saveButton = (Button) myFragmentView.findViewById(R.id.saveButton);
        removeButton = (Button) myFragmentView.findViewById(R.id.removeButton);

        if(nextButton == null) {
        } else {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextImage();
                    setImage();
                }
            });
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevImage();
                setImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRemoveDialog();
            }
        });

        if(text != null) {
            if (GlobalList.getGlobalInstance().getCurrMap().getImageList().size() > 0) {
                if(GlobalList.getGlobalInstance().getCurrImage() == null) {
                }
                else {
                    text.setText(GlobalList.getGlobalInstance().getCurrImage().getImagePath());
                }
            } else {
                text.setText("NONE");
            }
        }

        setImage();

        return myFragmentView;
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
        text.setText(GlobalList.getGlobalInstance().getCurrImage().getImagePath());
    }


    public void setImage() {
        if(GlobalList.getGlobalInstance().getCurrImage() != null) {

            ImageView imageView = ((ImageView) myFragmentView.findViewById(R.id.imageView));
            if (imageView != null) {
                imageView.setImageURI(Uri.parse(GlobalList.getGlobalInstance().getCurrImage().getImagePath()));
            }
        }
    }

    /* display the edit mMap name dialog */
    private void showEditDialog(){
        FragmentManager fm = getFragmentManager();
        MapDialog mapNameDialog = MapDialog.newInstance("Set Map Name");
        mapNameDialog.show(fm, "fragment_map_name");
    }

    private void showRemoveDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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

    /* Add the current Map to the end of Global List. Then write to shared prefs */
    private void saveMapToPrefs() {
        List<ImageListBean> currMapList = GlobalList.getGlobalInstance().getCurrMapList();
        currMapList.add(GlobalList.getGlobalInstance().getCurrMap());

        Gson gson = new Gson();
        String json = gson.toJson(currMapList);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("test", json);
        editor.commit();
    }
}
