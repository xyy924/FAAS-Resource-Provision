package aliyunfc.FunctionsMonitor;


import aliyunfc.Util.Tools;
import com.aliyun.fc_open20210406.models.InvokeFunctionResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Monitor {

    private static com.aliyun.sls20201230.Client logsClient;
    public static DataTypeOfLog logResultParsing(InvokeFunctionResponse response, String functionName,int memsize, float cpusize) {
        String RequestId = response.headers.get("x-fc-request-id");
        String functionState = "Success";

        String billedDurationstr = response.headers.get("x-fc-invocation-duration");
        int billedDuration = Integer.valueOf(billedDurationstr);
        double duration = billedDuration;

        int memorySize = memsize;

        String maxMemoryUsedstr = response.headers.get("x-fc-max-memory-usage");
        double maxMemoryUsed = Double.valueOf(maxMemoryUsedstr);

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DataTypeOfLog dataTypeOfLog = new DataTypeOfLog(RequestId, duration, billedDuration, memorySize, maxMemoryUsed, functionState, functionName, dateFormat.format(date), cpusize);
        return dataTypeOfLog;
    }

    public static void getSLSLogs(String functionName, Integer startTime, Integer endTime, String APPName, int iter) throws Exception {
        Monitor.logsClient = Tools.getslsClient();

        String topicreq = "FCRequestMetrics:serverless-xyy/"+functionName;

        String query = String.format(
                  "* | SELECT functionName, durationMs, invocationStartTimestamp, requestId, memoryMB, memoryUsageMB "
                          + "FROM log WHERE __topic__ = '%s' LIMIT 250", topicreq);
//

        com.aliyun.sls20201230.models.GetLogsV2Headers getLogsV2Headers = new com.aliyun.sls20201230.models.GetLogsV2Headers()
                .setAcceptEncoding("gzip");
        com.aliyun.sls20201230.models.GetLogsV2Request getLogsV2Request = new com.aliyun.sls20201230.models.GetLogsV2Request()
                .setFrom(startTime)
                .setTo(endTime)
                .setQuery(query)
                .setReverse(true);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
                .setReadTimeout(100000)
                .setConnectTimeout(10000);
        com.aliyun.sls20201230.models.GetLogsV2Response response = logsClient.getLogsV2WithOptions("serverless-cn-hangzhou-6caba849-d28e-544e-86c9-4442724e1622",
                "default-logs", getLogsV2Request, getLogsV2Headers, runtime);
        List<Map<String, String>> resulsts = response.getBody().getData();

        ArrayList<DataTypeOfLog> invokeResults = new ArrayList<>();

        int counter = 0;
        for (Map<String, String> result : resulsts) {
            DataTypeOfLog dataOfLog = new DataTypeOfLog();
            for (Map.Entry<String, String> entry : result.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                switch (key) {
                    case "invocationStartTimestamp":
                        long timestamp = Long.parseLong(value);
                        long timestampInSeconds = timestamp / 1000;
                        dataOfLog.setUTCTimeStamp(String.valueOf(timestampInSeconds));

                    case "functionName":
                        dataOfLog.setFunctionName(value);
                        break;

                    case "requestId":
                        dataOfLog.setRequestedId(value);
                        break;

                    case "durationMs":
                        dataOfLog.setDuration(Double.parseDouble(value));
                        dataOfLog.setBilledDuration((int)Math.ceil(Double.parseDouble(value)));
                        break;

                    case "memoryMB":
                        Integer memSize = Integer.parseInt(value);
                        dataOfLog.setMemorySize(memSize);

                        float step=0.05F;
                        if(memSize>=2048) step=0.1F;
                        
                        dataOfLog.setCpusize(Tools.getMaxVcpu(memSize, step));
                        break;

                    case "memoryUsageMB":
                        dataOfLog.setMaxMemoryUsed(Double.parseDouble(value));
                        break;
                    default:
                        break;
                }
                dataOfLog.setFunctionState("Success");
                dataOfLog.setFunctionName(functionName);
            }
//            counter++;
//            if (counter % 2 == 0) {
//                invokeResults.add(dataOfLog);
//            }
            invokeResults.add(dataOfLog);
        }
            Tools.generateFunctionInvokeResult(invokeResults, "CloudWatchLog", APPName, iter);

    }

}
