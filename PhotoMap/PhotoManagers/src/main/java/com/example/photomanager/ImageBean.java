package com.example.photomanager;

import java.io.Serializable;
import android.media.ExifInterface;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;


import com.google.gson.annotations.SerializedName;


public class ImageBean implements Serializable {
    @SerializedName("image_path")
    private String imagePath;
    @SerializedName("lat")
    private Float lat;
    @SerializedName("lng")
    private Float lng;

    public ImageBean(String path) {
        imagePath = path;
        lat = 0.0f;
        lng = 0.0f;
        try {
            ExifInterface exif = new ExifInterface(path);
            if(exif != null) {
                LatLng latLng;

                String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                if((LATITUDE !=null)
                        && (LATITUDE_REF !=null)
                        && (LONGITUDE != null)
                        && (LONGITUDE_REF !=null)) {

                    if (LATITUDE_REF.equals("N")) {
                        lat = convertToDegree(LATITUDE);
                    } else {
                        lat = 0 - convertToDegree(LATITUDE);
                    }

                    if (LONGITUDE_REF.equals("E")) {
                        lng = convertToDegree(LONGITUDE);
                    } else {
                        lng = 0 - convertToDegree(LONGITUDE);
                    }

                }
                System.out.println("LATLNG SET TO: " + lat + "  " + lng);
            }else
            {
                System.out.println("WAS NULL");
//                latLng = new LatLng(0.0D, 0.0D);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public Float getLat() {
        return lat;
    }

    public Float getLng() {
        return lng;
    }

    /* http://android-er.blogspot.com/2010/01/convert-exif-gps-info-to-degree-format.html*/
    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    };
}
