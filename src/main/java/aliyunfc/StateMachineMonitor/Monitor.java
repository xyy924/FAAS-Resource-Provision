package aliyunfc.StateMachineMonitor;

import aliyunfc.Util.Tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Monitor {
    private static com.aliyun.fnf20190315.Client cfclient;

    public static void getCloudFlowLogs(String APPName, String startRFC, String endRFC, int iter) throws Exception {
        Monitor.cfclient = Tools.getcfClient();

        ArrayList<String> timeExecutionend = new ArrayList<>();
        ArrayList<String> timeExecutionstart = new ArrayList<>();

        String nextToken = null;
        do {
            com.aliyun.fnf20190315.models.ListExecutionsRequest listExecutionsRequest = new com.aliyun.fnf20190315.models.ListExecutionsRequest()
                    .setFlowName(APPName)
                    .setLimit(100)
                    .setNextToken(nextToken)
                    .setStatus("Succeeded")
                    .setStartedTimeEnd(endRFC)
                    .setStartedTimeBegin(startRFC);
            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
                    .setReadTimeout(100000)
                    .setConnectTimeout(10000);

            com.aliyun.fnf20190315.models.ListExecutionsResponse listExecutionsResponse = cfclient.listExecutionsWithOptions(listExecutionsRequest, runtime);
            List<com.aliyun.fnf20190315.models.ListExecutionsResponseBody.ListExecutionsResponseBodyExecutions> executions = listExecutionsResponse.body.executions;
            nextToken = listExecutionsResponse.body.nextToken;

            for (com.aliyun.fnf20190315.models.ListExecutionsResponseBody.ListExecutionsResponseBodyExecutions execution : executions) {
                timeExecutionstart.add(execution.startedTime);
                timeExecutionend.add(execution.stoppedTime);
            }
        }
        while (nextToken!=null);
        Tools.generateTimeStampOfStateMachineInvokation(timeExecutionstart, timeExecutionend, APPName, iter);
    }
}
