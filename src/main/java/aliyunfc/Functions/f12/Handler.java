package aliyunfc.Functions.f12;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.PojoRequestHandler;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler implements PojoRequestHandler<Map<String, String>, Map<String, Map<String, String>>> {

    @Override
    public Map<String, Map<String, String>> handleRequest(Map<String, String> event, Context context) {
        String endpoint = "https://oss-cn-hangzhou-internal.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = "";

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();

        try {
            // 遍历每个文件
            for (OSSObjectSummary objectSummary : objectSummaries) {
                String objectName = objectSummary.getKey(); // 获取文件名
                // 读取文件内容并统计行数
                int numOfLines = readAndCountLines(ossClient, bucketName, objectName);
                System.out.println("文件 " + objectName + " 的行数: " + numOfLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭OSSClient。
            ossClient.shutdown();
        }

        Map<String, String> head = new HashMap<>();
        head.put("StatusCode", "200");
        head.put("FunctionName", "f12");
        head.put("Task type", "Network I/O intensive task");
        Map<String, String> body = new HashMap<>();
        for (String key : event.keySet()) {
            body.put(key, event.get(key));
        }
        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("head", head);
        result.put("body", body);
        return result;
    }

    private int readAndCountLines(OSS ossClient, String bucketName, String objectName) throws IOException {
        OSSObject ossObject = ossClient.getObject(bucketName, objectName);
        InputStream objectDataInputStream = ossObject.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(objectDataInputStream));
        int numOfLines = 0;
        while (reader.readLine() != null) {
            numOfLines++;
        }
        return numOfLines;
    }
}
