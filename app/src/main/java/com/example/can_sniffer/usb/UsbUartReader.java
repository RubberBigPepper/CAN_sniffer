package com.example.can_sniffer.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.can_sniffer.CAN.CANPacket;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.concurrent.Executors;

public class UsbUartReader extends BroadcastReceiver implements UsbArduinoCANParser.UsbArduinoCANParserListener {//класс будет читать данные с USB
    private static final int WRITE_WAIT_MILLIS = 1000;
    private final String TAG="UsbUartReader";
    private UsbManager usbManager=null;
    private UsbSerialPort serialPort=null;
    private SerialInputOutputManager usbIoManager=null;

    private static final String ACTION_USB_PERMISSION_BASE = "com.serenegiant.USB_PERMISSION.";
    private final String ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + hashCode();
    private PendingIntent mPermissionIntent = null;
    private int portNum=0;//по умолчанию нулевой порт будем использовать
    private int baudRate = 115200;//по умолчанию скорость порта
    private UsbArduinoCANParser usbArduinoParser=new UsbArduinoCANParser(this);

    public interface CANDataListener{
        void onCANDataReceived(CANPacket canPacket);
        void onStringReceived(String text);
        void onStatusListener(String text);
    };

    private CANDataListener listener=null;

    public void init(Context context, @NonNull CANDataListener listener){
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(this, filter);
        mPermissionIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_USB_PERMISSION), 0);

        this.listener=listener;
    }

    public void release(Context context){
        context.unregisterReceiver(this);
        disconnect();
        usbManager=null;
    }

    public void connect(){
        for(UsbDevice v : usbManager.getDeviceList().values()){
            connect(v.getDeviceId());
            break;
        }
    }

    public void connect(int deviceId) {
        UsbDevice device = null;
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            status("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            status("connection failed: not enough ports at device");
            return;
        }
        serialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && !usbManager.hasPermission(driver.getDevice())) {
            requestPermission(driver.getDevice());
            return;
        }
        if(usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice()))
                status("connection failed: permission denied");
            else
                status("connection failed: open failed");
            return;
        }

        try {
            serialPort.open(usbConnection);
            serialPort.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE);
            serialPort.setDTR(true); // for arduino, ...
            serialPort.setRTS(true);
            usbIoManager = new SerialInputOutputManager(serialPort, usbArduinoParser);
            //usbIoManager = new SerialInputOutputManager(serialPort, this.listener);
            Executors.newSingleThreadExecutor().submit(usbIoManager);
            status("connected");
        } catch (Exception e) {
            status("connection failed: " + e.getMessage());
            disconnect();
        }
    }

    private void disconnect() {
        if(usbIoManager != null)
            usbIoManager.stop();
        usbIoManager = null;
        try {
            if (serialPort!=null)
                serialPort.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        serialPort = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)){
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                if (device != null){
                    connect(device.getDeviceId());
                }
            }
            else{

            }
        }
    }

    public synchronized void requestPermission(final UsbDevice device) {
        if (device != null) {
            if (usbManager.hasPermission(device)) {
                connect(device.getDeviceId());
            } else {
                usbManager.requestPermission(device, mPermissionIntent);
            }
        }
    }

    private void status(String text){
        listener.onStatusListener(text);
        Log.e(TAG, text);
    }

    @Override
    public void onCANPackedReceived(CANPacket canPacket) {
        listener.onCANDataReceived(canPacket);
    }

    @Override
    public void onStringReceived(String text) {
        listener.onStringReceived(text);
    }

    private void parseCANPacket(int ID, int[] data){}
}
