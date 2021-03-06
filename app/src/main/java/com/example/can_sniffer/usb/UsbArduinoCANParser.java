package com.example.can_sniffer.usb;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.can_sniffer.CAN.CANPacket;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

//класс будет парсить данные, приходящие от ардуино
public class UsbArduinoCANParser implements SerialInputOutputManager.Listener {
    private final String TAG="UsbArduinoCANParser";
    StringBuffer stringBuffer=new StringBuffer();
    private int startIndex=-1;//откуда начало данных
    private int endIndex=-1;//где конец данных

    interface UsbArduinoCANParserListener{
        void onCANPackedReceived(CANPacket canPacket);
        void onStringReceived(String text);
    };

    private UsbArduinoCANParserListener listener=null;

    public UsbArduinoCANParser(@NonNull UsbArduinoCANParserListener listener){
        this.listener=listener;
    }

   //ID:0xID,0xdata1,data2,data3,data4,data5,data6,data7,data8  - По количеству, может быть меньше

    @Override
    public void onNewData(byte[] data) {
        try {
            stringBuffer.append(new String(data));
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
        if (data.length<=8) {
            try {
                int ID = parseInt(fields[0]);
                for (int n = 1; n < fields.length; n++) {
                    data[n - 1] = parseInt(fields[n]);
                }
                CANPacket packet=CANPacket.parsePacket(ID, data);
                if(packet!=null) {
                    listener.onCANPackedReceived(packet);
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private int parseInt(String text){
        return Integer.parseInt(text.replace("0x",""), 16);
    }
}
