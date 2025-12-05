package aliyunfc.PerfCostModelAccuracy;

import aliyunfc.FunctionsCreator.FunctionUpdator;
import aliyunfc.StateMachineInvoker.DeterministicParamGenerator;
import aliyunfc.StateMachineMonitor.Monitor;
import aliyunfc.Util.Tools;
import aliyunfc.StateMachineInvoker.Invoker;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class APP21 {
    private static TreeMap<String, Integer> memConfig = new TreeMap<>();
    private static int[][] memSize = new int[3][22];
    private static String[] taskTypes = {"Disk I/O","CPU","CPU","Network I/O","CPU","CPU","CPU","CPU","CPU","CPU","Disk I/O",
            "Network I/O","CPU","CPU","Disk I/O","Network I/O", "CPU","CPU","CPU","Disk I/O","CPU","Network I/O"};


    static {
        memSize[0] = new int[]{512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512,512};
//        memSize[1] = new int[]{1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024, 1024};
        memSize[1] = new int[]{2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048,2048};
        memSize[2] = new int[]{4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096, 4096};
    }

    public static void getAccuracyOfAPP21() {
        try {
            String TimePath = new File("").getCanonicalFile() + "/src/main/resources/TimeRcord.txt";

            double[] perfAccuracy = new double[3];
            double[] costAccuracy = new double[3];
            String APPName = "NEW21";
            for (int numOfMemSize = 0; numOfMemSize < memSize.length; numOfMemSize++) {
                memConfig.clear();
                for (int i = 0; i < memSize[numOfMemSize].length; i++)
                    memConfig.put("f" + (i + 1), memSize[numOfMemSize][i]);
                System.out.println("The memory configuration of all functions: ");
                for (int i = 1; i <= 21; i++) {
                    System.out.print("f" + i + " : " + memConfig.get("f" + i) + "MB");
                    if (i != 21) System.out.print(",");
                }
                System.out.println();

                FunctionUpdator.updateFunctions(APP21.memConfig);
                System.out.println("The memory sizes of aliyunfc have been updated. Time : " + new Date());

                Instant startdate = Instant.now();
                int repeatedTimes = 250;

//                int repeatedTimes = 300;

//                for (int i = 1; i <= repeatedTimes + 20; i++) {
//                    if (i<=20){
//                        Invoker.invokeStateMachine("PreheatS_"+APPName, i+20);
//                        TimeUnit.SECONDS.sleep(20);
//                        if(i>12) Invoker.invokeStateMachine(APPName, i+100);
//                        if(i==20){
//                            TimeUnit.SECONDS.sleep(32);
//                            startdate = Instant.now();
//                            String content = (numOfMemSize+1) + "云工作流启动时间："+startdate + System.lineSeparator();
//                            Files.write(Paths.get(TimePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//                        }
//                    }
//
//                    else{
//                        TimeUnit.SECONDS.sleep(5);
//                        Invoker.invokeStateMachine(APPName, i+100, DeterministicParamGenerator.getDeterministicEvent(APPName));
//                        if(numOfMemSize==0) TimeUnit.SECONDS.sleep(15);
//                        TimeUnit.SECONDS.sleep(10);
//                    }
//                }
//
//                TimeUnit.MINUTES.sleep(1);
                TimeUnit.SECONDS.sleep(3);
                Instant enddate = Instant.now();
//
//
                //使用DateTimeFormatter格式化时间为RFC 3339格式
                DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
                String startRFC = formatter.format(startdate);
                String endTRFC = formatter.format(enddate);
                //将时间转换为获取秒级时间戳
                Integer startTime = (int)startdate.getEpochSecond();
                Integer endTime = (int)enddate.getEpochSecond();
                System.out.println("StateMachines execution is over! Time : " + new Date());
//
//                String content = (numOfMemSize+1) + "云工作流结束时间："+enddate+","+startTime+","+endTime + System.lineSeparator();
//                Files.write(Paths.get(TimePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

//                Monitor.getCloudFlowLogs(APPName, startRFC, endTRFC, numOfMemSize + 1);

                double avgStateMachineDuration = Accuracy.getAvgDurationOfStateMachine(APPName, numOfMemSize + 1);
                double avgStateMachineCost = Accuracy.getAvgCostOfStateMachine(APPName, startTime, endTime, repeatedTimes,taskTypes, numOfMemSize + 1);
                System.out.println("Average Duration obtained from StateMachine Execution = " +
                        new BigDecimal(avgStateMachineDuration).setScale(2, RoundingMode.HALF_UP) + "ms.");
                System.out.println("Average Cost obtained from StateMachine Execution = " +
                        new BigDecimal(avgStateMachineCost).setScale(2, RoundingMode.HALF_UP) + "USD.");
                System.out.println("Average duration and cost of StateMachine have been got ! Time : " + new Date());

                double avgPerfCostModelDuration = Accuracy.getAvgDurationOfPerfCostModel(APP21.memConfig, APPName,numOfMemSize + 1);
                double avgPerfCostModelCost = Accuracy.getAvgCostOfPerfCostModel(APP21.memConfig, APPName,numOfMemSize + 1);
                System.out.println("Average Duration obtained from PerfCost Model = " +
                        new BigDecimal(avgPerfCostModelDuration).setScale(2, RoundingMode.HALF_UP) + "ms.");
                System.out.println("Average Cost obtained from PerfCost Model = " +
                        new BigDecimal(avgPerfCostModelCost).setScale(2, RoundingMode.HALF_UP) + "USD.");
                System.out.println();

                double[] accuracyResult = Tools.generateAccuracy(APPName, avgStateMachineDuration, avgStateMachineCost, avgPerfCostModelDuration,
                        avgPerfCostModelCost, numOfMemSize + 1);
                perfAccuracy[numOfMemSize] = accuracyResult[0];
                costAccuracy[numOfMemSize] = accuracyResult[1];
            }
            double avgPerfAccuracy = Arrays.stream(perfAccuracy).average().getAsDouble();
            double avgCostAccuracy = Arrays.stream(costAccuracy).average().getAsDouble();
            Tools.storeAvgAccuracyOfApp(APPName, avgPerfAccuracy, avgCostAccuracy);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
