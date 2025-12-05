package aliyunfc.Main;

import Main.Optimizer;
import aliyunfc.FunctionsInvoker.Invoker;
import aliyunfc.PerfCostModelAccuracy.APP10;
import aliyunfc.PerfCostModelAccuracy.APP16;
import aliyunfc.PerfCostModelAccuracy.APP21;
import aliyunfc.PerfCostModelAccuracy.Accuracy;
import aliyunfc.Util.Tools;
import com.aliyun.tea.TeaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class aliyunfcMain {

    public static void main(String[] args){
        long delayInMillis = calculateDelayToMidnight();
        System.out.println("距离启动还有 " + delayInMillis + " 毫秒。");
        // 创建 Timer 和 TimerTask
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("启动实验项目！");

                String[] functionsNames = Tools.getFunctionNames();
                Invoker invoker = new Invoker();
                invoker.invokeFunctions(functionsNames);


                com.aliyun.fc_open20210406.Client client = null;
                try {
                    client = aliyunfcMain.createClient();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                com.aliyun.fc_open20210406.models.UpdateServiceHeaders updateServiceHeaders = new com.aliyun.fc_open20210406.models.UpdateServiceHeaders();
                com.aliyun.fc_open20210406.models.LogConfig logConfig = new com.aliyun.fc_open20210406.models.LogConfig()
                        .setEnableRequestMetrics(true)
                        .setEnableInstanceMetrics(false)
                        .setLogstore("default-logs")
                        .setProject("xxxxxx"); //替换为你的日志项目名称
                com.aliyun.fc_open20210406.models.UpdateServiceRequest updateServiceRequest = new com.aliyun.fc_open20210406.models.UpdateServiceRequest()
                        .setInternetAccess(true)
                        .setLogConfig(logConfig);
                com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
                try {
                    client.updateServiceWithOptions("serverless-xyy", updateServiceRequest, updateServiceHeaders, runtime);
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


                APP10.getAccuracyOfAPP10();
                APP16.getAccuracyOfAPP16();
                APP21.getAccuracyOfAPP21();
                Accuracy.getAvgAccuracy();
//
//                Optimizer.OptimizationOfAllServerlessWorkflow();

               // 停止 Timer
                timer.cancel();
            }
        };

        // 定时任务（只执行一次）
        timer.schedule(task, delayInMillis);

        System.out.println("定时任务已设置，将在 "+ "点启动实验项目。");
    }

    public static float getMaxVcpu(int memorySizeMB,float step) {
        BigDecimal  MIN_VCPU = new BigDecimal(Float.toString(0.15F));
        BigDecimal MAX_VCPU = new BigDecimal(Float.toString(16.0F));

        BigDecimal MIN_RATIO = new BigDecimal(Float.toString(0.25F));
        BigDecimal MAX_RATIO = new BigDecimal(Float.toString(1.0F));
        BigDecimal VCPU_STEP = new BigDecimal(Float.toString(step));
        BigDecimal  memorySizeGB = new BigDecimal(Double.toString(memorySizeMB / 1024.0));

        BigDecimal maxstep = memorySizeGB.multiply(MAX_RATIO).divide(VCPU_STEP,0, RoundingMode.FLOOR);
        BigDecimal minstep = memorySizeGB.multiply(MIN_RATIO).divide(VCPU_STEP,0, RoundingMode.CEILING);

        // 根据比例限制计算最小和最大 vCPU 值，并对齐到步长
        BigDecimal minVcpu = new BigDecimal(Double.toString(Math.max(MIN_VCPU.doubleValue(), minstep.multiply(VCPU_STEP).doubleValue())));
        BigDecimal maxVcpu = new BigDecimal(Double.toString(Math.min(MAX_VCPU.doubleValue(), maxstep.multiply(VCPU_STEP).doubleValue())));
        ArrayList<Float> vcpuOptions = new ArrayList<>();

        // 遍历所有可能的vCPU值，并添加到列表中
        for (BigDecimal vcpu = minVcpu; vcpu.compareTo(maxVcpu)<=0; vcpu = vcpu.add(VCPU_STEP)) {
            vcpuOptions.add(vcpu.floatValue());
        }

        float medianVcpu;
        int size = vcpuOptions.size();
        if (size % 2 == 1) {
            medianVcpu = vcpuOptions.get(size / 2);
        } else {
            medianVcpu = vcpuOptions.get(size / 2 - 1); // 当数量为偶数时取编号较小的那个
        }
        return medianVcpu;
    }

    public static ArrayList<Integer> GetMemOptions(float cpusize) {
        BigDecimal MIN_MEM = new BigDecimal("256");
        BigDecimal MAX_MEM = new BigDecimal("4096");

        BigDecimal MIN_RATIO = new BigDecimal("1.0");
        BigDecimal MAX_RATIO = new BigDecimal("4.0");
        BigDecimal MEM_STEP = new BigDecimal("64");
        BigDecimal CpuSize = new BigDecimal(Float.toString(cpusize*1024));

        BigDecimal maxstep = CpuSize.multiply(MAX_RATIO).divide(MEM_STEP,0, RoundingMode.FLOOR);
        BigDecimal minstep = CpuSize.multiply(MIN_RATIO).divide(MEM_STEP,0, RoundingMode.CEILING);

        // 根据比例限制计算最小和最大 vCPU 值，并对齐到步长
        BigDecimal minMem =  new BigDecimal(Double.toString(Math.max(MIN_MEM.intValue(), minstep.multiply(MEM_STEP).intValue())));
        BigDecimal maxMem = new BigDecimal(Double.toString(Math.min(MAX_MEM.intValue(), maxstep.multiply(MEM_STEP).intValue())));

        ArrayList<Integer> memOptions = new ArrayList<>();

        // 遍历所有可能的vCPU值，并添加到列表中
        for (BigDecimal mem = minMem; mem.compareTo(maxMem)<=0; mem = mem.add(MEM_STEP)) {
            memOptions.add(mem.intValue());
        }

        return memOptions;

    }

    private static long calculateDelayToMidnight() {
        // 计算当前时间到午夜的时间差
        long now = System.currentTimeMillis();
        long midnight = java.time.LocalDateTime.now()
                .plusHours(0)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        return midnight - now;
    }

    public static com.aliyun.fc_open20210406.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID。
                .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
                // 必填，请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
                .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        // Endpoint 请参考 https://api.aliyun.com/product/FC-Open
        config.endpoint = "xxxxxx.cn-hangzhou.fc.aliyuncs.com"; //替换为你的账号 ID
        return new com.aliyun.fc_open20210406.Client(config);
    }
}
