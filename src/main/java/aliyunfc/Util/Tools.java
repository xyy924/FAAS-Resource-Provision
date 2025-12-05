package aliyunfc.Util;

import com.aliyun.tea.*;

import aliyunfc.FunctionsMonitor.DataTypeOfLog;

import org.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.OSSObjectSummary;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.json.JSONObject;

public class Tools {

    public static ArrayList<Float> calculateVcpuOptions(int memorySizeMB, float step) {
        BigDecimal  MIN_VCPU = new BigDecimal(Float.toString(0.15F));
        BigDecimal MAX_VCPU = new BigDecimal(Float.toString(16.0F));

        BigDecimal MIN_RATIO = new BigDecimal(Float.toString(0.25F));
        BigDecimal MAX_RATIO = new BigDecimal(Float.toString(1.0F));
        BigDecimal VCPU_STEP = new BigDecimal(Float.toString(step));
        BigDecimal  memorySizeGB = new BigDecimal(Double.toString(memorySizeMB / 1024.0));

        BigDecimal maxstep = memorySizeGB.multiply(MAX_RATIO).divide(VCPU_STEP,0, RoundingMode.FLOOR);
        BigDecimal minstep = memorySizeGB.multiply(MIN_RATIO).divide(VCPU_STEP,0, RoundingMode.CEILING);

        // 根据比例限制计算最小和最大 vCPU 值，并对齐到步长
        BigDecimal minVcpu =  new BigDecimal(Double.toString(Math.max(MIN_VCPU.doubleValue(), minstep.multiply(VCPU_STEP).doubleValue())));
        BigDecimal maxVcpu = new BigDecimal(Double.toString(Math.min(MAX_VCPU.doubleValue(), maxstep.multiply(VCPU_STEP).doubleValue())));

        ArrayList<Float> vcpuOptions = new ArrayList<>();

        // 遍历所有可能的vCPU值，并添加到列表中
        for (BigDecimal vcpu = minVcpu; vcpu.compareTo(maxVcpu)<=0; vcpu = vcpu.add(VCPU_STEP)) {
            vcpuOptions.add(vcpu.floatValue());
        }

        return vcpuOptions;
    }

//    public static float getMaxVcpu(int memorySizeMB,float step) {
//        BigDecimal  MIN_VCPU = new BigDecimal(Float.toString(0.15F));
//        BigDecimal MAX_VCPU = new BigDecimal(Float.toString(16.0F));
//
//        BigDecimal MIN_RATIO = new BigDecimal(Float.toString(0.25F));
//        BigDecimal MAX_RATIO = new BigDecimal(Float.toString(1.0F));
//        BigDecimal VCPU_STEP = new BigDecimal(Float.toString(step));
//        BigDecimal  memorySizeGB = new BigDecimal(Double.toString(memorySizeMB / 1024.0));
//
//        BigDecimal maxstep = memorySizeGB.multiply(MAX_RATIO).divide(VCPU_STEP,0, RoundingMode.FLOOR);
//        BigDecimal minstep = memorySizeGB.multiply(MIN_RATIO).divide(VCPU_STEP,0, RoundingMode.CEILING);
//
//        // 根据比例限制计算最小和最大 vCPU 值，并对齐到步长
//        BigDecimal maxVcpu = new BigDecimal(Double.toString(Math.min(MAX_VCPU.doubleValue(), maxstep.multiply(VCPU_STEP).doubleValue())));
//
//        return maxVcpu.floatValue();
//    }

    /**
     *
     * @param memorySizeMB
     * @param step
     * @return
     * 这里默认策略是取1：2的CPU值 而不是最大的
     */
        public static float getMaxVcpu(int memorySizeMB,float step) {
            BigDecimal MIN_VCPU = new BigDecimal(Float.toString(0.15F));
            BigDecimal MAX_VCPU = new BigDecimal(Float.toString(16.0F));

//            BigDecimal MIN_RATIO = new BigDecimal(Float.toString(0.25F));
//            BigDecimal MAX_RATIO = new BigDecimal(Float.toString(1.0F));
            BigDecimal RATIO = new BigDecimal(Float.toString(0.5F));
            BigDecimal VCPU_STEP = new BigDecimal(Float.toString(step));
            BigDecimal  memorySizeGB = new BigDecimal(Double.toString(memorySizeMB / 1024.0));

//            BigDecimal maxstep = memorySizeGB.multiply(MAX_RATIO).divide(VCPU_STEP,0, RoundingMode.FLOOR);
//            BigDecimal minstep = memorySizeGB.multiply(MIN_RATIO).divide(VCPU_STEP,0, RoundingMode.CEILING);
            BigDecimal vstep = memorySizeGB.multiply(RATIO).divide(VCPU_STEP,0, RoundingMode.HALF_UP);

            // 根据比例限制计算最小和最大 vCPU 值，并对齐到步长
//            BigDecimal minVcpu = new BigDecimal(Double.toString(Math.max(MIN_VCPU.doubleValue(), minstep.multiply(VCPU_STEP).doubleValue())));
//            BigDecimal maxVcpu = new BigDecimal(Double.toString(Math.min(MAX_VCPU.doubleValue(), maxstep.multiply(VCPU_STEP).doubleValue())));
            BigDecimal medianVcpu = new BigDecimal(Double.toString( vstep.multiply(VCPU_STEP).doubleValue()));
//            ArrayList<Float> vcpuOptions = new ArrayList<>();

//            // 遍历所有可能的vCPU值，并添加到列表中
//            for (BigDecimal vcpu = minVcpu; vcpu.compareTo(maxVcpu)<=0; vcpu = vcpu.add(VCPU_STEP)) {
//                vcpuOptions.add(vcpu.floatValue());
//            }
//
//            float medianVcpu;
//            int size = vcpuOptions.size();
//            if (size % 2 == 1) {
//                medianVcpu = vcpuOptions.get(size / 2);
//            } else {
//                medianVcpu = vcpuOptions.get(size / 2 - 1); // 当数量为偶数时取编号较小的那个
//            }
            return medianVcpu.floatValue();
    }


    public static com.aliyun.fc_open20210406.Client getfcClient() throws Exception{
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        // Endpoint 请参考 https://api.aliyun.com/product/FC-Open
        config.endpoint = "1200908065695906.cn-hangzhou.fc.aliyuncs.com";
        return new com.aliyun.fc_open20210406.Client(config);
    }

    public static com.aliyun.fnf20190315.Client getcfClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        // Endpoint 请参考 https://api.aliyun.com/product/fnf
        config.endpoint = "cn-hangzhou.fnf.aliyuncs.com";
        return new com.aliyun.fnf20190315.Client(config);
    }

    public static com.aliyun.sls20201230.Client getslsClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        // Endpoint 请参考 https://api.aliyun.com/product/Sls
        config.endpoint = "cn-hangzhou.log.aliyuncs.com";
        return new com.aliyun.sls20201230.Client(config);
    }

    public static String getFileContent(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
            String line = "";
            while (true) {
                line = bufferedReader.readLine();
                if (line == null)
                    break;
                stringBuffer.append(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringBuffer.toString();
    }

    public static void generateFunctionPerfProfile(Map<Integer, Integer> perfProfile, String functionName) {
        String filePrefix = null;
        try {
            filePrefix = new File("").getCanonicalFile() + "/src/main/resources/aliyunfc_functions_perf_profile/";
            File file = new File(filePrefix + functionName + "_perf_profile.json");
            JSONObject jsonObject = new JSONObject();
            for (Integer item : perfProfile.keySet())
                jsonObject.put(String.valueOf(item), perfProfile.get(item));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            String s = jsonObject.toString();
            fileOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateFunctionPerfProfilecpu(Map<String, Integer> perfProfile, String functionName) {
        String filePrefix = null;
        try {
            filePrefix = new File("").getCanonicalFile() + "/src/main/resources/aliyunfc_functions_perf_profile_cpu/";
            File file = new File(filePrefix + functionName + "_perf_profile.json");
            JSONObject jsonObject = new JSONObject();
            for (String item : perfProfile.keySet())
                jsonObject.put((item), perfProfile.get(item));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            String s = jsonObject.toString();
            fileOutputStream.write(s.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void generateFunctionInvokeResult(ArrayList<DataTypeOfLog> logVector, String type, String APPName, int iter) {
        String filePath = null;
        if (logVector.size() == 0)
            return;
        try {
            if (type.equals("FunctionInvoke")) {
                filePath = new File("").getCanonicalFile() + "/src/main/resources/aliyunfc_functions_invoke_results_got_by_function/aliyunfc_" +
                        logVector.get(0).getFunctionName() + "_Logs.xls";
            } else if (type.equals("CloudWatchLog")) {
                filePath = new File("").getCanonicalFile() + "/src/main/resources/aliyunfc_functions_invoke_results_got_by_cloudwatchlog/" +
                        APPName + "/" + iter + "/aliyunfc_" + logVector.get(0).getFunctionName() + "_Logs.xls";
            }

            File file = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(file);

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet(logVector.get(logVector.size() - 1).getFunctionName() + "_logs");
            HSSFRow row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("FunctionName");
            row0.createCell(1).setCellValue("FunctionState");
            row0.createCell(2).setCellValue("MemorySize");
            row0.createCell(3).setCellValue("MaxMemoryUsed");
            row0.createCell(4).setCellValue("CpuSize");
            row0.createCell(5).setCellValue("Duration");
            row0.createCell(6).setCellValue("BilledDuration");
            row0.createCell(7).setCellValue("RequestedId");
            row0.createCell(8).setCellValue("UTCTimeStamp");
            for (int i = 0; i < logVector.size(); i++) {
                HSSFRow row = sheet.createRow(i + 1);
                DataTypeOfLog log = logVector.get(i);
                row.createCell(0).setCellValue(log.getFunctionName());
                row.createCell(1).setCellValue(log.getFunctionState());
                row.createCell(2).setCellValue(log.getMemorySize());
                row.createCell(3).setCellValue(log.getMaxMemoryUsed());
                row.createCell(4).setCellValue(log.getCpusize());
                row.createCell(5).setCellValue(log.getDuration());
                row.createCell(6).setCellValue(log.getBilledDuration());
                row.createCell(7).setCellValue(log.getRequestedId());
                row.createCell(8).setCellValue(log.getUTCTimeStamp());
            }
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static double getTotalSizeOfOSS() {
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com"; // 替换为您的Endpoint
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");  // 环境变量中获取AccessKey ID
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        String bucketName = "serverless-exp"; // 替换为您的Bucket名称
//        String prefix = "yourPrefix/"; // 替换为您的目录前缀

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        long totalSize = 0L;
        String marker = null;

        try {
            ObjectListing listing;
            do {
                ListObjectsRequest request = new ListObjectsRequest(bucketName).withMarker(marker);
                listing = ossClient.listObjects(request);

                for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                    totalSize += summary.getSize();
                }

                marker = listing.getNextMarker();
            } while (listing.isTruncated());

            System.out.println("Total file size: " + totalSize + " bytes");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return totalSize;
    }

    public static String getStateMachineExecutionEvent(String APPName) {
        JSONObject event = new JSONObject();

        int randInt = new Random().nextInt(100);
        if (randInt < 20) event.put("para1", "f1");
        else event.put("para1", "f3");

        randInt = new Random().nextInt(100);
        if (randInt < 30) event.put("para2", "f5");
        else event.put("para2", "f4");

        randInt = new Random().nextInt(100);
        if (randInt < 20) event.put("para3", "f15");
        else event.put("para3", "f13");

        randInt = new Random().nextInt(100);
        if (randInt < 30) event.put("para4", "f12");
        else event.put("para4", "f11");

        randInt = new Random().nextInt(100);
        if (randInt < 20) event.put("para5", "f18");
        else if (randInt >= 20 && randInt < 40) event.put("para5", "f19");
        else event.put("para5", "f17");


        if (APPName.equals("NEW10")) {
            event.remove("para3");
            event.remove("para4");
            event.remove("para5");
        } else if (APPName.equals("NEW16")) {
            event.remove("para5");
        }

        return event.toString();
    }

    public static void generateTimeStampOfStateMachineInvokation(ArrayList<String> timeExecutionStarted, ArrayList<String> timeExecutionEnded, String APPName, int iter) {
        String filePath = null;

        try {
            filePath = new File("").getCanonicalPath() + "/src/main/resources/aliyunfc_StateMachine_invoke_results/" + iter + "/aliyunfc_StateMachine_" +
                    APPName + "_Logs.xls";
            File file = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(file);

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("StateMachine_" + APPName + "_logs");
            HSSFRow row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("Start");
            row0.createCell(1).setCellValue("End");
            row0.createCell(2).setCellValue("Duration");

            int size = Math.min(timeExecutionStarted.size(), timeExecutionEnded.size());
            for (int i = 0; i < size; i++) {
                HSSFRow aRow = sheet.createRow(i + 1);
                String startTime = timeExecutionStarted.get(i);
                String endTime = timeExecutionEnded.get(i);
                aRow.createCell(0).setCellValue(startTime);
                aRow.createCell(1).setCellValue(endTime);

                Instant startInstant = Instant.parse(startTime);
                Instant endInstant = Instant.parse(endTime);

                aRow.createCell(2).setCellValue(Duration.between(startInstant, endInstant).toMillis());
            }
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        }
    }

    public static double[] generateAccuracy(String APPName, double StateMachineDuration,
                                            double StateMachineCost, double PerfCostModelDuration, double PerfCostModelCost, int index) {
        double PerfAccuracy = 0;
        double CostAccuracy = 0;
        double[] accuracyResult = new double[2];
        try {
            String filePath = new File("").getCanonicalPath() + "/src/main/resources/accuracy/" + index + "/" + APPName + "_Accuracy.xls";
            File accuracyFile = new File(filePath);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Accuracy");

            HSSFRow head = sheet.createRow(0);
            head.createCell(0).setCellValue("APPName");
            head.createCell(1).setCellValue("StateMachineDuration");
            head.createCell(2).setCellValue("StateMachineCost");
            head.createCell(3).setCellValue("PerfCostModelDuration");
            head.createCell(4).setCellValue("PerfCostModelCost");
            head.createCell(5).setCellValue("PerfAccuracy");
            head.createCell(6).setCellValue("CostAccuracy");

            HSSFRow aRow = sheet.createRow(1);
            aRow.createCell(0).setCellValue(APPName);
            aRow.createCell(1).setCellValue(StateMachineDuration);
            aRow.createCell(2).setCellValue(StateMachineCost);
            aRow.createCell(3).setCellValue(PerfCostModelDuration);
            aRow.createCell(4).setCellValue(PerfCostModelCost);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
            PerfAccuracy = 100 - Math.abs(PerfCostModelDuration - StateMachineDuration) / PerfCostModelDuration * 100;
            aRow.createCell(5).setCellValue(decimalFormat.format(PerfAccuracy) + "%");
            CostAccuracy = 100 - Math.abs(PerfCostModelCost - StateMachineCost) / PerfCostModelCost * 100;
            aRow.createCell(6).setCellValue(decimalFormat.format(CostAccuracy) + "%");

            FileOutputStream outputStream = new FileOutputStream(accuracyFile);
            workbook.write(outputStream);
            outputStream.close();
            System.out.println("Performance Model Accuracy of " + APPName + ": " + new BigDecimal(PerfAccuracy).setScale(2,RoundingMode.HALF_UP) + "%.");
            System.out.println("Cost Model Accuracy of " + APPName + ": " + new BigDecimal(CostAccuracy).setScale(2,RoundingMode.HALF_UP) + "%.");
            accuracyResult[0] = PerfAccuracy;
            accuracyResult[1] = CostAccuracy;
            return accuracyResult;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return accuracyResult;
    }

    public static void storeAvgAccuracyOfApp(String APPName, double avgPerfAccuracy, double avgCostAccuracy) {
        try {
            String filePath = new File("").getCanonicalPath() + "/src/main/resources/accuracy/" + APPName + "_AvgAccuracy.xls";
            File accuracyFile = new File(filePath);
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("Accuracy");

            HSSFRow head = sheet.createRow(0);
            head.createCell(0).setCellValue("APPName");
            head.createCell(1).setCellValue("PerfAccuracy");
            head.createCell(2).setCellValue("CostAccuracy");

            HSSFRow aRow = sheet.createRow(1);
            aRow.createCell(0).setCellValue(APPName);
            aRow.createCell(1).setCellValue(avgPerfAccuracy);
            aRow.createCell(2).setCellValue(avgCostAccuracy);

            FileOutputStream outputStream = new FileOutputStream(accuracyFile);
            workbook.write(outputStream);
            outputStream.close();
            System.out.println("Average Performance Model Accuracy of " + APPName + ": " +
                    new BigDecimal(avgPerfAccuracy).setScale(2,RoundingMode.HALF_UP) + "%.");
            System.out.println("Average Cost Model Accuracy of " + APPName + ": " +
                    new BigDecimal(avgCostAccuracy).setScale(2,RoundingMode.HALF_UP) + "%.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String[] getFunctionNames() {
        return new String[]{"f2", "f3", "f4", "f5", "f1","f6", "f7", "f8", "f9", "f10","f11","f12","f13","f14","f15","f22", "f17", "f18", "f19","f16", "f20", "f21"};
    }



}
