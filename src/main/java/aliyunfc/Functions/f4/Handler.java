package aliyunfc.Functions.f4;

import com.aliyun.fc.runtime.PojoRequestHandler;
import com.aliyun.fc.runtime.Context;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.*;
import com.aliyuncs.exceptions.ClientException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class Handler implements PojoRequestHandler<Map<String, String>, Map<String, Map<String, String>>> {

    @Override
    public Map<String, Map<String, String>> handleRequest(Map<String, String> event, Context context) {
        String endpoint = "https://oss-cn-hangzhou-internal.aliyuncs.com";
        String accessKeyId = "";
        String accessKeySecret = "";
        String bucketName = ""; // 替换为您的

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);


        try {
            // 列举存储库中的所有文件
            ObjectListing objectListing = ossClient.listObjects(bucketName);
            List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();

            // 遍历每个文件
            for (OSSObjectSummary objectSummary : objectSummaries) {
                String objectName = objectSummary.getKey(); // 获取文件名
                // 读取文件内容并输出最后一行的长度
                readAndPrintLastLineLength(ossClient, bucketName, objectName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // 关闭OSS客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        Map<String, String> head = new HashMap<>();
        head.put("StatusCode", "200");
        head.put("FunctionName", "f4");
        head.put("Task type", "Network I/O intensive task");
        Map<String, String> body = new HashMap<>();
        for (
                String key : event.keySet())
            body.put(key, event.get(key));
        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("head", head);
        result.put("body", body);
        return result;
    }

    private static void readAndPrintLastLineLength(OSS ossClient, String bucketName, String objectName) {
        try {
            // 获取文件对象
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ossObject.getObjectContent()));

            // 逐行读取文件内容
            String line;
            String lastLine = "";
            while ((line = reader.readLine()) != null) {
                lastLine = line; // 更新最后一行内容
            }

            // 输出最后一行的长度
            System.out.println("文件 " + objectName + " 的最后一行长度: " + lastLine.length());

            // 关闭资源
            reader.close();
            ossObject.close();
        } catch (Exception e) {
            System.err.println("处理文件 " + objectName + " 时出错: " + e.getMessage());
        }
    }




}