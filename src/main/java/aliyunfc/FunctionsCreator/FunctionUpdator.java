package aliyunfc.FunctionsCreator;

import com.aliyun.fc_open20210406.Client;
import com.aliyun.fc_open20210406.models.UpdateFunctionHeaders;
import com.aliyun.fc_open20210406.models.UpdateFunctionRequest;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import aliyunfc.Util.Tools;
import com.aliyun.tea.TeaException;

public class FunctionUpdator {
    private static Client fcClient;
    public static void updateFunctions(TreeMap<String,Integer> memConfig) throws Exception {
        fcClient = Tools.getfcClient();

        for(String functionName : memConfig.keySet()){
            int memSize = memConfig.get(functionName);
            float step=0.05F;
            if(memSize>=2048) step=0.1F;

            UpdateFunctionHeaders updateFunctionHeaders = new com.aliyun.fc_open20210406.models.UpdateFunctionHeaders();
            UpdateFunctionRequest updateFunctionRequest = new com.aliyun.fc_open20210406.models.UpdateFunctionRequest()
                    .setCpu(Tools.getMaxVcpu(memSize,step))
                    .setMemorySize(memSize);

            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

            try {
                // 复制代码运行请自行打印 API 的返回值
                fcClient.updateFunctionWithOptions("serverless-xyy", functionName, updateFunctionRequest, updateFunctionHeaders, runtime);
            } catch (TeaException error) {
                // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
                // 错误 message
                System.out.println(error.getMessage());
                // 诊断地址
                System.out.println(error.getData().get("Recommend"));
                com.aliyun.teautil.Common.assertAsString(error.message);
            } catch (Exception _error) {
                TeaException error = new TeaException(_error.getMessage(), _error);
                // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
                // 错误 message
                System.out.println(error.getMessage());
                // 诊断地址
                System.out.println(error.getData().get("Recommend"));
                com.aliyun.teautil.Common.assertAsString(error.message);
            }

        }

    }
}
