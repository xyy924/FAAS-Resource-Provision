package aliyunfc.FunctionsInvoker;

import aliyunfc.FunctionsMonitor.Monitor;
import aliyunfc.Util.Tools;
import aliyunfc.FunctionsMonitor.DataTypeOfLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.aliyun.fc_open20210406.models.UpdateFunctionHeaders;
import com.aliyun.fc_open20210406.models.UpdateFunctionRequest;
import com.aliyun.tea.TeaException;

import static aliyunfc.Util.Tools.calculateVcpuOptions;
import static aliyunfc.Util.Tools.getMaxVcpu;

public class Invoker {
    public static ArrayList<Integer> availableMemory = new ArrayList<>();
//    public static ArrayList<Float>  availableCpu= new ArrayList<>();
    public static ArrayList<Thread> threads = new ArrayList<>();

    static {
        for (int size = 256; size <= 1024; size += 64) {
            Invoker.availableMemory.add(size);
        }
        for (int size = 1024; size < 2048; size += 64) {
            Invoker.availableMemory.add(size);
        }
        for (int size = 2048; size <= 4096; size += 64) {
            Invoker.availableMemory.add(size);
        }
    }

    public void invokeFunctions(String[] functionNames) {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Submit all tasks to the thread pool
        for (String functionName : functionNames) {
            executor.submit(new MyThread(functionName));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        /**
        for (int i = 0; i < functionNames.length; i++) {
            String functionName = functionNames[i];
            Thread aThread = new Thread(new MyThread(functionName));
            threads.add(aThread);
        }

        for(Thread aThread : threads){
            aThread.start();
            try {
                TimeUnit.HOURS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        for (Thread aThread : threads) {
            try {
                aThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
         **/

    }

    class MyThread implements Runnable {
        String functionName;
        MyThread(String functionName) {
            this.functionName = functionName;
        }
        @Override
        public void run() {
            com.aliyun.fc_open20210406.Client fcClient;
            String filePath = null;
            String TimePath = null;

            try {
                filePath = new File("").getCanonicalFile().getPath() + "/src/main/resources/aliyunfc_request_events/testEvent.txt";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String requestEvent = Tools.getFileContent(filePath);

            try {
                TimePath = new File("").getCanonicalFile() + "/src/main/resources/TimeRcord.txt";
                String content = functionName + "函数开始时间：" + Instant.now() + System.lineSeparator();
                Files.write(Paths.get(TimePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ArrayList<DataTypeOfLog> logVector = new ArrayList<>();
            Map<Integer, Integer> perfProfile = new TreeMap();
            Map<String, Integer> perfProfilecpu = new TreeMap();



            for (int memorySize : Invoker.availableMemory) {
                float step=0.05F;
                if(memorySize>=2048) step=0.1F;

                for(float cpuSize : calculateVcpuOptions(memorySize,step)) {
                    try {
                        fcClient = Tools.getfcClient();
                    } catch (Exception e) {
                        try {
                            TimePath = new File("").getCanonicalFile() + "/src/main/resources/TimeRcord.txt";
                            String content = functionName +  e + System.lineSeparator();
                            Files.write(Paths.get(TimePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                        catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        throw new RuntimeException(e);
                    }

                    int repeatedTimes = 50;  //函数重复执行次数


                    int[] billedDurations = new int[repeatedTimes];

                    UpdateFunctionHeaders updateFunctionHeaders = new com.aliyun.fc_open20210406.models.UpdateFunctionHeaders();
                    UpdateFunctionRequest updateFunctionRequest = new com.aliyun.fc_open20210406.models.UpdateFunctionRequest()
                            .setCpu(cpuSize)
                            .setMemorySize(memorySize);
                    com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions()
                            .setReadTimeout(100000)
                            .setConnectTimeout(10000);

                    com.aliyun.fc_open20210406.models.UpdateFunctionResponse ufresponse=null;
                    try {
                        ufresponse = fcClient.updateFunctionWithOptions("serverless-xyy", functionName, updateFunctionRequest, updateFunctionHeaders, runtime);
                    } catch (TeaException error) {
                        System.out.println(error.getMessage());
                        System.out.println(error.getData().get("Recommend"));
                        com.aliyun.teautil.Common.assertAsString(error.message);
                    } catch (Exception _error) {
                        TeaException error = new TeaException(_error.getMessage(), _error);
                        System.out.println(error.getMessage());
                        System.out.println(error.getData().get("Recommend"));
                        com.aliyun.teautil.Common.assertAsString(error.message);
                    }

//                    System.out.println("The state of updating memory size of " + functionName + ":" + ufresponse.getStatusCode());

                    DataTypeOfLog dataTypeOfLog = null;


                    for (int j = 0; j < repeatedTimes + 10; j++) {
                        try {
                            fcClient = Tools.getfcClient();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        com.aliyun.fc_open20210406.models.InvokeFunctionHeaders invokeFunctionHeaders = new com.aliyun.fc_open20210406.models.InvokeFunctionHeaders()
                                    .setXFcLogType("Tail");
                        com.aliyun.fc_open20210406.models.InvokeFunctionRequest invokeFunctionRequest = new com.aliyun.fc_open20210406.models.InvokeFunctionRequest()
                                    .setBody(com.aliyun.teautil.Common.toBytes(requestEvent));

                        com.aliyun.fc_open20210406.models.InvokeFunctionResponse ifresponse = null;
                        try {
                            ifresponse = fcClient.invokeFunctionWithOptions("serverless-xyy", functionName, invokeFunctionRequest, invokeFunctionHeaders, runtime);
                        } catch (TeaException error) {
                            System.out.println(error.getMessage());
                            System.out.println(error.getData().get("Recommend"));
                            com.aliyun.teautil.Common.assertAsString(error.message);
                        } catch (Exception _error) {
                            TeaException error = new TeaException(_error.getMessage(), _error);
                            System.out.println(error.getMessage());
                            System.out.println(error.getData().get("Recommend"));
                            com.aliyun.teautil.Common.assertAsString(error.message);
                        }

                        dataTypeOfLog = Monitor.logResultParsing(ifresponse, functionName, memorySize, cpuSize);
                        if (j >= 10) {
                            logVector.add(dataTypeOfLog);
                            billedDurations[j - 10] = dataTypeOfLog.getBilledDuration();
                        }

//                        if(j==repeatedTimes + 10){
//                            System.out.printf("Iteration: %d, %s worked, MemorySize: %d, CpuSize: %f, BilledDuration: %d ms, FunctionState: %s, Time: %s\n", (j + 1), dataTypeOfLog.getFunctionName(),
//                                    dataTypeOfLog.getMemorySize(), dataTypeOfLog.getCpusize(), dataTypeOfLog.getBilledDuration(), dataTypeOfLog.getFunctionState(), Instant.now());
//                        }

                    }



                    double count = 0;
                    for (int item : billedDurations)
                        count += item;
                    int avgbilledDuration = (int) (count / billedDurations.length + 0.5);

                    perfProfilecpu.put(memorySize+","+cpuSize, avgbilledDuration);
//                    if(cpuSize==getMaxVcpu(memorySize,step))
//                        perfProfile.put(memorySize, avgbilledDuration);

                    System.out.printf("Iteration: %d, %s worked, MemorySize: %d, CpuSize: %f, BilledDuration: %d ms, FunctionState: %s, Time: %s\n", (repeatedTimes + 5), dataTypeOfLog.getFunctionName(),
                            dataTypeOfLog.getMemorySize(), dataTypeOfLog.getCpusize(), avgbilledDuration, dataTypeOfLog.getFunctionState(), Instant.now());


                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
            Tools.generateFunctionPerfProfilecpu(perfProfilecpu, functionName);
//            Tools.generateFunctionPerfProfile(perfProfile, functionName);

            Tools.generateFunctionInvokeResult(logVector, "FunctionInvoke", null, 0);
            // FcClient.shutdown();


//            try {
//                TimePath = new File("").getCanonicalFile() + "/src/main/resources/TimeRcord.txt";
//                String content = functionName + "函数结束时间：" + Instant.now() + System.lineSeparator();
//                Files.write(Paths.get(TimePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }




        }
    }
}


