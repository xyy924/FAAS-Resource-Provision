package aliyunfc.StateMachineInvoker;

import java.util.*;
import java.util.stream.*;
import org.json.JSONObject;

public class DeterministicParamGenerator {
    // 预生成所有可能的参数组合（按概率分布）
    private static final List<JSONObject> NEW10_PARAMS = generateNew10Params();
    private static final List<JSONObject> NEW16_PARAMS = generateNew16Params();
    private static final List<JSONObject> NEW21_PARAMS = generateNEW21Params();

    private static int counter = 0;

    // 生成确定性参数（轮询预生成的序列）
    public static String getDeterministicEvent(String APPName) {
        List<JSONObject> targetList = NEW21_PARAMS;
        if ("NEW10".equals(APPName)) targetList = NEW10_PARAMS;
        else if ("NEW16".equals(APPName)) targetList = NEW16_PARAMS;

        JSONObject event = targetList.get(counter % targetList.size());
        counter++;
        return event.toString();
    }

    // 预生成NEW10的参数序列（40组，严格符合概率）
    private static List<JSONObject> generateNew10Params() {
        List<JSONObject> sequence = IntStream.range(0, 40)
                .mapToObj(i -> {
                    JSONObject event = new JSONObject();
                    if(i<8){
                        event.put("para1","f1");
                        event.put("para2","f4");
                    }
                    else if(i<16){
                        event.put("para1","f1");
                        event.put("para2","f5");
                    }
                    else if(i<28){
                        event.put("para1","f3");
                        event.put("para2","f4");
                    }
                    else{
                        event.put("para1","f3");
                        event.put("para2","f5");
                    }
                    return event;
                })
                .collect(Collectors.toList());
        Collections.shuffle(sequence, new Random(12345));
        return sequence;
    }

    // 预生成NEW16的参数序列（100组）
    private static List<JSONObject> generateNew16Params() {
        List<JSONObject> sequence = IntStream.range(0, 100)
                .mapToObj(i -> {
                    JSONObject event = new JSONObject();
                    if(i<4){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f11");
                    }
                    else if(i<8){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                    }
                    else if(i<14){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f11");
                    }
                    else if(i<20){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                    }
                    else if(i<24){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f11");
                    }
                    else if(i<28){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                    }
                    else if(i<34){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f11");
                    }
                    else if(i<40){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                    }
                    else if(i<46){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f11");
                    }
                    else if(i<52){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                    }
                    else if(i<61){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f11");
                    }
                    else if(i<70){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                    }
                    else if(i<76){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f11");
                    }
                    else if(i<82){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                    }
                    else if(i<91){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f11");
                    }
                    else{
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                    }

                    return event;
                })
                .collect(Collectors.toList());
        Collections.shuffle(sequence, new Random(12345)); // 使用固定种子打乱
        return sequence;
    }

    // 预生成完整参数序列（100组）
    private static List<JSONObject> generateNEW21Params() {
        List<JSONObject> sequence = IntStream.range(0, 250)
                .mapToObj(i -> {
                    JSONObject event = new JSONObject();
                    if(i<10){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<12){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<18){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<20){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<35){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<38){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<47){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<50){
                        event.put("para1","f1");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }

                    else if(i<60){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<62){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<68){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<70){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<85){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<88){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<97){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<100){
                        event.put("para1","f1");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<115){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<118){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<127){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<130){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<153){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<157){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<171){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<175){
                        event.put("para1","f3");
                        event.put("para2","f4");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<190){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<193){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<202){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else if(i<205){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f15");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    else if(i<227){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f11");
                        event.put("para5", "f17");
                    }
                    else if(i<232){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f17");
                    }
                    else if(i<245){
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f18");
                    }
                    else {
                        event.put("para1","f3");
                        event.put("para2","f5");
                        event.put("para3","f13");
                        event.put("para4","f12");
                        event.put("para5", "f19");
                    }
                    return event;
                })
                .collect(Collectors.toList());
        Collections.shuffle(sequence, new Random(12345));
        return sequence;
    }
}