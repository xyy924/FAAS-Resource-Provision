package aliyunfc.Functions.f20;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.PojoRequestHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Handler implements PojoRequestHandler<Map<String, String>, Map<String, Map<String, String>>> {

    @Override
    public Map<String, Map<String, String>> handleRequest(Map<String, String> event, Context context) {
        int i=0;
        boolean file_indicator;
        while (i<30){
            String path = "/tmp/3MB";
            File file = new File(path);
            file_indicator = file.isFile();
            if (file_indicator) {
                file.delete();
            }
            FileOutputStream outputStream = null;
            try {
                file.createNewFile();
                for (int j = 0; j < 265; j++) {
                    outputStream = new FileOutputStream(file);
                    outputStream.write(new Random().nextInt());
                    outputStream.flush();
                    outputStream.close();
                }
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }

        Map<String, String> head = new HashMap<>();
        head.put("StatusCode", "200");
        head.put("FunctionName", "f20");
        head.put("Task type", "Disk I/O intensive task");
        Map<String, String> body = new HashMap<>();
        for(String key : event.keySet())
            body.put(key,event.get(key));
        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("head", head);
        result.put("body", body);
        return result;
    }
}
