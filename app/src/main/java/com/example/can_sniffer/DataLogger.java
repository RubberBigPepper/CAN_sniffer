package com.example.can_sniffer;

import android.location.Location;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

//класс просто будет сохранять данные
public class DataLogger {
    private Writer writer=null;
    private int index = 0;
    private String fileName="";

    public void startWriter(){//сгенерируем имя по умолчанию, используя текущую дату
        String filename=(new SimpleDateFormat("yyyyMMdd_HHmmss")).format(Calendar.getInstance().getTime());
        filename= Environment.getExternalStorageDirectory().getAbsolutePath()+"/can_"+filename+".csv";
        startWriter(filename);
    }

    public void startWriter(String filename){
        closeWriter();
        try {
            fileName=filename;
            writer = new FileWriter(fileName);
            index = 0;
            AppendHeadRow();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean isOpened(){
        return writer!=null;
    }

    public void closeWriter(){
        try {
            if (writer != null) {
                writer.close();
                writer = null;
                if(index==0)//ничего не записано-удалим файл
                    new File(fileName).delete();
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void writeData(int ID, int [] data, Location location){
        if (!isOpened())
            return;
        StringBuffer buffer=new StringBuffer();
        buffer.append(index);
        buffer.append(";");
        buffer.append(String.format("%02X;", ID));
        int n=0;
        for (int value:data){
            buffer.append(String.format("%d",value));
            buffer.append(";");
            n++;
        }
        while (n<8){
            n++;
            buffer.append(";");
        }
        buffer.append(makeLocationData(location));
        buffer.append("\n");
        try {
            writer.write(buffer.toString());
            index++;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private String makeLocationData(Location location){
        if (location==null)
            return ";;;;;;";
        StringBuffer buffer=new StringBuffer();
        buffer.append(location.getLatitude());
        buffer.append(";");
        buffer.append(location.getLongitude());
        buffer.append(";");
        buffer.append(location.getSpeed());
        buffer.append(";");
        buffer.append(location.getAltitude());
        buffer.append(";");
        buffer.append(location.getBearing());
        buffer.append(";");
        buffer.append(location.getTime());
        buffer.append(";");
        return buffer.toString();
    }

    public void AppendHeadRow(){
        StringBuffer buffer=new StringBuffer();
        buffer.append("index;ID;");
        for (int n=0;n<8;n++){
            buffer.append(String.format("data_%d;", n));
        }
        buffer.append("gps_lat;gps_lon;gps_speed;gps_alt;gps_bearing;time;");
        buffer.append("\n");
        try {
            writer.write(buffer.toString());
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
