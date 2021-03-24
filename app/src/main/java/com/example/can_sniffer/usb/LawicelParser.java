package com.example.can_sniffer.usb;

import android.util.Log;

import androidx.annotation.NonNull;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

//парсинг пакетов протокола Lawicel (CAN hacker)
public class LawicelParser implements SerialInputOutputManager.Listener {
    private final String TAG="LawicelListener";
    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
    StringBuffer stringBuffer=new StringBuffer();
    private int startIndex=-1;//откуда начало данных
    private int endIndex=-1;//где конец данных

    interface LawicelParserListener{
        void onPackedRecieved(int ID, int[] data);
        void onStringReceived(String text);
    };

    private LawicelParserListener listener=null;

    public LawicelParser(@NonNull LawicelParserListener listener){
        this.listener=listener;
    }

    @Override
    public void onNewData(byte[] data) {
        try {
            byteArrayOutputStream.write(data);

            parsePacket();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void parsePacket(){
        String rawText=stringBuffer.toString();
        String[] lines=rawText.split("\r");
        if (lines==null||lines.length==0)
            return;
        for(int n=0;n<lines.length-1;n++){//последняя строка недоформирована, ее пропускаем и отправим на дополнение
            if(!parseRow(lines[n])){
                Log.e(TAG, lines[n]);
                listener.onStringReceived(lines[n].replace("\n",""));
            }
        }
        stringBuffer.setLength(0);
        stringBuffer.append(lines[lines.length-1]);
    }

    @Override
    public void onRunError(Exception e) {
        listener.onStringReceived(e.getMessage());
    }

    private boolean parseRow(String row){
        int posColon=row.indexOf(':');
        if (posColon==-1) {
            return false;
        }
        String temp=row.substring(posColon+1);
        String [] fields=temp.split(",");//первый - это ID пакета, затем через запятую - его данные
        if(fields.length<=1)
            return false;
        int[] data=new int[fields.length-1];//int, чтобы не возиться со знаковым типом byte
        try {
            /*int ID = parseInt(fields[0]);
            for (int n = 1; n < fields.length; n++) {
                data[n-1]=parseInt(fields[n]);
            }
            listener.onPackedRecieved(ID, data);*/
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }



}
