package aliyunfc.StateMachineInvoker;


import java.util.Date;
import java.util.List;

import aliyunfc.Util.Tools;

public class Invoker {

    private static com.aliyun.fnf20190315.Client cfClient;
    public static void invokeStateMachine(String APPName, int repeatedTimes,String inputEvent) throws Exception {
        cfClient = Tools.getcfClient();
//        String inputEvent = Tools.getStateMachineExecutionEvent(APPName);
//        com.aliyun.fnf20190315.models.StartSyncExecutionRequest startSyncExecutionRequest = new com.aliyun.fnf20190315.models.StartSyncExecutionRequest()
//                .setFlowName(APPName)
//                .setInput(inputEvent)
//                .setExecutionName(APPName+"_"+repeatedTimes);
//        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
//                .setReadTimeout(100000)
//                .setConnectTimeout(10000);
//        com.aliyun.fnf20190315.models.StartSyncExecutionResponse response = cfClient.startSyncExecutionWithOptions(startSyncExecutionRequest, runtime);


        com.aliyun.fnf20190315.models.StartExecutionRequest startExecutionRequest = new com.aliyun.fnf20190315.models.StartExecutionRequest()
                .setFlowName(APPName)
                .setInput(inputEvent);
//                .setExecutionName(APPName+"_"+repeatedTimes);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
                .setReadTimeout(100000)
                .setConnectTimeout(40000)
                .setMaxAttempts(3);

        com.aliyun.fnf20190315.models.StartExecutionResponse response = cfClient.startExecutionWithOptions(startExecutionRequest, runtime);
        System.out.println("Iteration : "+ repeatedTimes+ ", " + APPName+" execution complete! Time: " + new Date());
    }

    public static void invokeStateMachine(String APPName, int repeatedTimes) throws Exception {
        cfClient = Tools.getcfClient();
        String inputEvent = Tools.getStateMachineExecutionEvent(APPName);
        com.aliyun.fnf20190315.models.StartExecutionRequest startExecutionRequest = new com.aliyun.fnf20190315.models.StartExecutionRequest()
                .setFlowName(APPName)
                .setInput(inputEvent);
//                .setExecutionName(APPName+"_"+repeatedTimes);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
                .setReadTimeout(100000)
                .setConnectTimeout(10000);

        com.aliyun.fnf20190315.models.StartExecutionResponse response = cfClient.startExecutionWithOptions(startExecutionRequest, runtime);
        System.out.println("Iteration : "+ repeatedTimes+ ", " + APPName+" execution complete! Time: " + new Date());
    }



}
