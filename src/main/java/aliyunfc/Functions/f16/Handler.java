package aliyunfc.Functions.f16;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.PojoRequestHandler;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Handler implements PojoRequestHandler<Map<String, String>, Map<String, Map<String, String>>> {

    @Override
    public Map<String, Map<String, String>> handleRequest(Map<String, String> event, Context context) {
        String endpoint = "https://oss-cn-hangzhou-internal.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = ""; // 替换为您的

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ObjectListing objectListing = ossClient.listObjects(bucketName);
        List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
        try {
            for (OSSObjectSummary objectSummary : objectSummaries) {
                String key = objectSummary.getKey();
                OSSObject ossObject = ossClient.getObject(bucketName, key);
                InputStream objectDataInputStream = ossObject.getObjectContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(objectDataInputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> head = new HashMap<>();
        head.put("StatusCode", "200");
        head.put("FunctionName", "f16");
        head.put("Task type", "Network I/O intensive task");
        Map<String, String> body = new HashMap<>();
        for(String key : event.keySet())
            body.put(key,event.get(key));
        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("head", head);
        result.put("body", body);
        return result;
    }
}