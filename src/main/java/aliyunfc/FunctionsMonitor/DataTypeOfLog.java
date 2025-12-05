package aliyunfc.FunctionsMonitor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataTypeOfLog {
    private String requestedId;
    private double duration;
    private int billedDuration;
    private int memorySize;
    private double maxMemoryUsed;
    private String functionState;
    private String functionName;
    private long UTCTimeStamp;
    private float cpusize;
    private float maxcpuUsed;

    public long getUTCTimeStamp() {return UTCTimeStamp;}

    public void setUTCTimeStamp(String UTCTimeStamp) {
        this.UTCTimeStamp = Long.valueOf(UTCTimeStamp);
//        try{
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date = dateFormat.parse(UTCTimeStamp);
//            this.UTCTimeStamp = date.getTime();
//        }catch (ParseException e){
//            e.printStackTrace();
//            System.exit(1);
//        }
    }

    public String getRequestedId() {
        return requestedId;
    }

    public void setRequestedId(String requestedId) {
        this.requestedId = requestedId;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getBilledDuration() {
        return billedDuration;
    }

    public void setBilledDuration(int billedDuration) {
        this.billedDuration = billedDuration;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    public float getCpusize() {
        return cpusize;
    }

    public void setCpusize(float cpusize) {
        this.cpusize = cpusize;
    }

    public float getmaxcpuUsed() {
        return maxcpuUsed;
    }

    public void setmaxcpuUsed(float maxcpuUsed) {
        this.maxcpuUsed = maxcpuUsed;
    }

    public double getMaxMemoryUsed() {
        return maxMemoryUsed;
    }

    public void setMaxMemoryUsed(double maxMemoryUsed) {
        this.maxMemoryUsed = maxMemoryUsed;
    }

    public String getFunctionState() {
        return functionState;
    }

    public void setFunctionState(String functionState) {
        this.functionState = functionState;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public DataTypeOfLog(String requestedId, double duration, int billedDuration, int memorySize, double maxMemoryUsed, String functionState, String functionName, String simpleFormatTime, float cpusize) {
        this.requestedId = requestedId;
        this.duration = duration;
        this.billedDuration = billedDuration;
        this.memorySize = memorySize;
        this.maxMemoryUsed = maxMemoryUsed;
        this.functionState = functionState;
        this.functionName = functionName;
        this.cpusize = cpusize;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = dateFormat.parse(simpleFormatTime);
            this.UTCTimeStamp = date.getTime();
        }catch (ParseException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public DataTypeOfLog() {
        this.requestedId = "null";
        this.duration = 0;
        this.billedDuration = 0;
        this.memorySize = 0;
        this.maxMemoryUsed = 0;
        this.functionState = "null";
        this.functionName = "null";
        this.UTCTimeStamp=0;
        this.cpusize = 0;
        this.maxcpuUsed = 0;
    }

    @Override
    public String toString() {
        return "functionName='" + functionName + '\'' +
                ", requestedId='" + requestedId + '\'' +
                ", duration=" + duration +
                " ms, billedDuration=" + billedDuration +
                " ms, memorySize=" + memorySize +
                " MB, maxMemoryUsed=" + maxMemoryUsed +
                "MB,cpusize="+ cpusize +
                " V, functionState='" + functionState + '\'';
    }
}
