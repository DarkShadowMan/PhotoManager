package com.example.photomanager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;

import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Intent;
import java.util.Date;
import android.database.Cursor;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.List;


public class MainActivity extends Activity implements OnClickListener {
    Button mBtnDateFrom, mBtnDateTo, GetList;
    EditText mDateFrom, mDateTo;
    ListView listView;


    private static List<ImageBean> image_list = new ArrayList<ImageBean>();
    public static ImageBean selectedImage;
    public static int selectedIndex = 0;
    private Cursor cursor;
    private int imageColumnIndex;

    private int mYearFrom, mMonthFrom, mDayFrom;
    private int mYearTo, mMonthTo, mDayTo;
    private Date dateFrom = new Date();
    private Date dateTo = new Date();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetList = (Button) findViewById(R.id.btnGetList);

        mDateFrom = (EditText) findViewById(R.id.txtDateFrom);
        mDateTo = (EditText) findViewById(R.id.txtDateTo);
        listView = (ListView) findViewById(R.id.listview);

        mBtnDateFrom = (Button) findViewById(R.id.btnCalendarFrom);
        mBtnDateTo = (Button) findViewById(R.id.btnCalendarTo);

        // Set the date fields to today
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = df.format(c.getTime());
        mDateFrom.setText(formattedDate);
        mDateTo.setText(formattedDate);


        mBtnDateFrom.setOnClickListener(this);
        mBtnDateTo.setOnClickListener(this);
        GetList.setOnClickListener(this);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                                    int position, long id) {
                /* used to clear the old image list here */
                //                if(image_list.length > 0) {
                //                    image_list.clear();
                //                }

                ImageListBean clickedMap = (ImageListBean) adapter.getItemAtPosition(position);
                System.out.println("THE MAP CLICKED: " + clickedMap);
                System.out.println("THE MAP CLICKED: " + clickedMap.getImageList());
                /* Set the current mMap. Current image and index will be set in displayactivity */
                GlobalList.getGlobalInstance().setCurrMap(clickedMap);
                Intent intent = new Intent(getBaseContext(), ShowActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == mBtnDateFrom) {

            // Process to get Current Date
            final Calendar c = Calendar.getInstance();
            mYearFrom = c.get(Calendar.YEAR);
            mMonthFrom = c.get(Calendar.MONTH);
            mDayFrom = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            String dateStr = monthOfYear + 1 + "-"
                                    + (dayOfMonth + 1) + "-" + year;
                            mDateFrom.setText(dateStr);
                            SimpleDateFormat  format = new SimpleDateFormat("MM-dd-yyyy");
                            try {
                                dateFrom = format.parse(dateStr);
                            } catch(ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }, mYearFrom, mMonthFrom, mDayFrom);
            dpd.show();
        }

        if (v == mBtnDateTo) {

            // Process to get Current Date
            final Calendar c = Calendar.getInstance();
            mYearTo = c.get(Calendar.YEAR);
            mMonthTo = c.get(Calendar.MONTH);
            mDayTo = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker Dialog
            DatePickerDialog dpd = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            // Display Selected date in textbox
                            String dateStr = monthOfYear + 1 + "-"
                                    + (dayOfMonth + 1) + "-" + year;
                            mDateTo.setText(dateStr);
                            SimpleDateFormat  format = new SimpleDateFormat("MM-dd-yyyy");
                            try {
                                dateTo = format.parse(dateStr);
                            } catch(ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, mYearTo, mMonthTo, mDayTo);
            dpd.show();
        }

        if (v == GetList) {
            // Get list of Images and print
            startMapActivity();
        }
    }

    private void getImageList()
    {
        System.out.println("GETTING LIST");
        String as[] = {
                "_data"
        };
        cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, as, null, null, null);

        imageColumnIndex = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToPosition(0);

        String path, timestamp;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date curDate;
        ExifInterface exif;

        /* clear image list here */
//        if(image_list.size() > 0) {
//            image_list.clear();
//        }
        int index = 0;
        /* can't do this until we can query with right dates.
            the current cursor count is all images
         */
//        image_list = new ImageBean[cursor.getCount()];

        ArrayList templist = new ArrayList();

        while(cursor.moveToNext())
        {
            path = cursor.getString(imageColumnIndex);
            try {
                exif = new ExifInterface(path);
                timestamp = exif.getAttribute(ExifInterface.TAG_DATETIME);
                if(timestamp != null) {
                    curDate = sdf.parse(timestamp);
                    if(curDate.after(dateFrom) && dateTo.after(curDate)) {
//                        image_list[index] = new ImageBean(path);
                        templist.add(new ImageBean(path));
                    }
                }
            } catch(IOException e) {

            } catch(ParseException e) {

            }
            index++;
        }

        // Current image list work around
        for(int i = 0; i < templist.size(); i++) {
            image_list.add((ImageBean) templist.get(i));
        }

        /* set the current mMap to the new mMap */
        ImageListBean map = new ImageListBean("", image_list, "");
        GlobalList.getGlobalInstance().setCurrMap(map);

        cursor.close();
    }

    private void startMapActivity() {
        getImageList();
        Intent intent = new Intent(getBaseContext(), ShowActivity.class);
//        intent.putExtra("IMAGE_LIST", image_list);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSavedLists((ListView) findViewById(R.id.listview));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getSavedLists(ListView listView) {
        List<ImageListBean> currMapList = new ArrayList<ImageListBean>();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String json2 = prefs.getString("test", null);

        if (json2 != null) {

            Type listType = new TypeToken<List<ImageListBean>>() {
            }.getType();
            List<ImageListBean> list = new Gson().fromJson(json2, listType);
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    ImageListBean map = list.get(i);
                    if (map != null) {
                        currMapList.add(map);
                    } else {
                    }
                }

            }
        }

        /* Set the global saved mMap list */
        GlobalList.getGlobalInstance().setCurrMapList(currMapList);

        /* Set the maplist into the array adapter */
        ArrayAdapter<ImageListBean> adapter = new ArrayAdapter<ImageListBean>(this,
                android.R.layout.simple_list_item_1, currMapList);
        listView.setAdapter(adapter);

    }
}