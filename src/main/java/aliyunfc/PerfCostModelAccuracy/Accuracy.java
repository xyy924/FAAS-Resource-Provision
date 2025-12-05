package aliyunfc.PerfCostModelAccuracy;


import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import aliyunfc.FunctionsMonitor.Monitor;
import aliyunfc.Util.Tools;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import serverlessWorkflow.graph.APPGraph;
import serverlessWorkflow.graph.WVertex;
import serverlessWorkflow.PerformanceAndCostModel.PerfOpt;
import serverlessWorkflow.PerformanceAndCostModel.ServerlessAppWorkflow;

public class Accuracy {
    public static double getAvgDurationOfStateMachine(String APPName, int iter) {
        double avgDuration = 0;
        try {
            File stateMachineInvokeResult = new File(new File("").getCanonicalPath() +
                    "/src/main/resources/aliyunfc_StateMachine_invoke_results/"+iter+"/aliyunfc_StateMachine_" + APPName + "_Logs.xls");
            FileInputStream inputStream = new FileInputStream(stateMachineInvokeResult.getPath());
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet = workbook.getSheet("StateMachine_" + APPName + "_logs");
            int rowNumbers = sheet.getLastRowNum();
            System.out.println("StateMachine " +APPName+ " executed " + rowNumbers + " times.");
            double[] durationsOfAPP = new double[rowNumbers];
            for (int i = 1; i <= rowNumbers; i++) {
                HSSFRow aRow = sheet.getRow(i);
                double aDuration = aRow.getCell(2).getNumericCellValue();
                durationsOfAPP[i - 1] = aDuration;
            }
            OptionalDouble avgDurationDouble = Arrays.stream(durationsOfAPP).average();
            avgDuration = avgDurationDouble.getAsDouble();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return avgDuration;
    }

        public static double getAvgCostOfStateMachine(String APPName, Integer startTime, Integer endTime, int repeatedTimes,String[] taskTypes, int iter) {
        double cost = 0;
        int funcNum = Integer.valueOf(APPName.split("NEW")[1]);
        try {
            double sizeOfOSS = Tools.getTotalSizeOfOSS();
            for (int i = 1; i <= funcNum; i++) {
                double rtOfFunction = 0;
                double costOfFunction = 0;
                String functionName = "f" + i;

//                Monitor.getSLSLogs(functionName, startTime, endTime, APPName, iter);

                String funcLogPath = new File("").getCanonicalPath() +
                        "/src/main/resources/aliyunfc_functions_invoke_results_got_by_cloudwatchlog/"+APPName+"/"+iter+"/aliyunfc_f" + i + "_Logs.xls";
                FileInputStream inputStream = new FileInputStream(funcLogPath);
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                HSSFSheet sheet = workbook.getSheet("f" + i + "_logs");
                int rowNums = sheet.getLastRowNum();
                System.out.print("aliyunfc function f" +i + " executed " + rowNums +" times. ");
                for (int j = 1; j <= rowNums; j++) {
                    HSSFRow aRow = sheet.getRow(j);
                    double aRT = aRow.getCell(5).getNumericCellValue();
                    rtOfFunction += aRT;
                    int memSize = (int) aRow.getCell(2).getNumericCellValue();

                    //TODO 这里其实只用固定step=0.05F。但是状态机跑的内存和cpu组合，其实单独的函数可能没有，故先这样设置。
                    // 到时候跑状态机的配置用到函数上获取数据再比较比较真实，不然函数的这个配置组合是模拟出来的，影响模型准确性，要修改就同步修改FunctionUpdator和Monitor中的设置
                    //这里的意思getMaxVcpu(memSize,step)就是说我指定了其余算法的cpu大小为步长为step时候的中位数
                    float step=0.05F;
                    if(memSize>=2048) step=0.1F;

                    costOfFunction += (Tools.getMaxVcpu(memSize,step) * aRT / 1000.0 * 1  + memSize / 1024.0 * aRT / 1000.0 * 0.15 + 0.0075)*0.00012*10000000;
                    //最后的10000000是放大系数， 0.00012是单位cu价格
                }
                if(taskTypes[i-1].equals("Network I/O")){
                    double costOfNetWorkTask =  sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
                    costOfFunction += costOfNetWorkTask * rowNums ;
                }

                //这里costOfFunction可能有问题 执行了很多次function,但是只加了一次传输的成本 costOfNetWorkTask 修改为 costOfNetWorkTask * rowNums
                cost += costOfFunction;
                inputStream.close();
                System.out.println("Average runtime of f"+i+": "+(rtOfFunction/rowNums)+" ms, Average cost of f"+i+": "+(costOfFunction/rowNums)+" USD.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        cost = cost/repeatedTimes;
        return cost;
    }

    public static double getAvgDurationOfPerfCostModel(TreeMap<String, Integer> memConfig, String APPName, int iter) {
        PerfOpt perfOpt = Accuracy.generatePerfOPTAndUpdateMemConfig(memConfig,APPName,iter);

        for(WVertex aVertex : perfOpt.getApp().getGraph().getDirectedGraph().vertexSet()){
            System.out.println(aVertex.toString()+": "+aVertex.getMem()+"MB, "+aVertex.getRt()+"ms, "+aVertex.getCost()+"USD.");
        }
        double duration = perfOpt.getApp().GetAverageRT();
        return duration;
    }

    public static double getAvgCostOfPerfCostModel(TreeMap<String, Integer> memConfig, String APPName,int iter) {
        PerfOpt perfOpt = Accuracy.generatePerfOPTAndUpdateMemConfig(memConfig,APPName,iter);
        double cost = perfOpt.getApp().GetAverageCost();
        return cost;
    }

    public static  PerfOpt generatePerfOPTAndUpdateMemConfig(TreeMap<String, Integer> memConfig, String APPName,int iter){
        String jsonPath = null;
        try{
            jsonPath = new File("").getCanonicalPath() + "/src/main/resources/serverless_workflow_json_files/" + APPName+".json";
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }

        APPGraph appGraph = new APPGraph(jsonPath);
        ServerlessAppWorkflow App = new ServerlessAppWorkflow(appGraph,"SFN","AWS",-1,-1);
        PerfOpt perfOpt = new PerfOpt(App, true, null);
        TreeMap<WVertex, Integer> updateMemConfig = new TreeMap<WVertex, Integer>();
        WVertex[] vertices = perfOpt.getApp().getGraph().getDirectedGraph().vertexSet().toArray(new WVertex[0]);
        for(int i=0;i< vertices.length;i++){
            int memSize = 0;
            for(String functionName : memConfig.keySet()){
                if(functionName.replaceFirst("f","v").equals(vertices[i].getVertexInfo())){
                    memSize = memConfig.get(functionName);
                    updateMemConfig.put(vertices[i], memSize);
                    break;
                }
            }
            updateMemConfig.put(vertices[i], memSize);
        }
//        perfOpt.update_App_workflow_mem_rt_cost_by_statemachine(updateMemConfig,APPName,iter);
        perfOpt.update_App_workflow_mem_rt_cost(updateMemConfig);
        return perfOpt;
    }

    public static void getAvgAccuracy(){
        String[] app = {"NEW10","NEW16","NEW22"};
        double[] accuracyOfPerf = new double[app.length];
        double[] accuracyOfCost = new double[app.length];
        for(int i=0;i<app.length;i++){
            try{
                String pathOfAvgAccuracyOfApp = new File("").getCanonicalPath() + "/src/main/resources/accuracy/"+app[i]+"_AvgAccuracy.xls";
                FileInputStream inputStream = new FileInputStream(pathOfAvgAccuracyOfApp);
                HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
                HSSFSheet sheet = workbook.getSheet("Accuracy");
                HSSFRow aRow = sheet.getRow(1);
                accuracyOfPerf[i] = aRow.getCell(1).getNumericCellValue();
                accuracyOfCost[i] = aRow.getCell(2).getNumericCellValue();
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println("Average accuracy of performance: " +
                new BigDecimal(Arrays.stream(accuracyOfPerf).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP) + "%.");
        System.out.println("Average accuracy of cost: " +
                new BigDecimal(Arrays.stream(accuracyOfCost).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP) + "%.");
    }
}
