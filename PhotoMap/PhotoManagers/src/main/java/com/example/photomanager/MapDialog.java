
package com.example.photomanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.LinearLayout;


public class MapDialog extends DialogFragment {

    public interface MapDialogListener {
        void onFinishMapDialog(String titleText, String descText);
    }

    public MapDialog() {

    }

    public static MapDialog newInstance(String title) {
        MapDialog frag = new MapDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Add Map Name And Description");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText titleText = new EditText(context);
        titleText.setHint("Map Name");
        layout.addView(titleText);

        final EditText descriptionText = new EditText(context);
        descriptionText.setHint("Map Description");
        layout.addView(descriptionText);

        alertDialogBuilder.setView(layout);

        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MapDialogListener listener = (MapDialogListener) getActivity();
                listener.onFinishMapDialog(titleText.getText().toString(), descriptionText.getText().toString());
                dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }
}