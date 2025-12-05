package serverlessWorkflow.graph;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import serverlessWorkflow.PerformanceAndCostModel.MemoryCpuKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.TreeMap;

public class WVertex implements Comparable {
    private String vertexInfo;
    private double rt;
    private TreeMap<Integer, Double> perf_profile;
    private TreeMap<MemoryCpuKey, Double> perf_profileCpu;
    private int mem;
    private float cpu;
    private int[] available_mem_list;
    private double cost;
    private double node_delay;
    private double BCR;
    private String taskType;

    public String getVertexInfo() {
        return vertexInfo;
    }

    public double getRt() {
        return rt;
    }

    public double getRt_by_statemachine(String APPName,int iter,String funcName) {
        try {
            double rtOfFunction = 0;
            String funcLogPath = new File("").getCanonicalPath() +
                    "/src/main/resources/aliyunfc_functions_invoke_results_got_by_cloudwatchlog/" + APPName + "/" + iter + "/aliyunfc_" + funcName + "_Logs.xls";
            FileInputStream inputStream = new FileInputStream(funcLogPath);
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet = workbook.getSheet(funcName + "_logs");
            int rowNums = sheet.getLastRowNum();
            for (int j = 1; j <= rowNums; j++) {
                HSSFRow aRow = sheet.getRow(j);
                double aRT = aRow.getCell(5).getNumericCellValue();
                rtOfFunction += aRT;
            }
            inputStream.close();

            return rtOfFunction / rowNums;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public TreeMap<Integer, Double> getPerf_profile() {
        return perf_profile;
    }
    public TreeMap<MemoryCpuKey, Double> getPerf_profileCpu() {
        return perf_profileCpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public float getCpu() {
        return cpu;
    }

    public void setCpu(float cpu) {
        this.cpu = cpu;
    }

    public int[] getAvailable_mem_list() {
        return available_mem_list;
    }

    public void setAvailable_mem_list(int[] available_mem_list) {
        this.available_mem_list = available_mem_list;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getNode_delay() {
        return node_delay;
    }

    public void setNode_delay(double node_delay) {
        this.node_delay = node_delay;
    }

    public double getBCR() {
        return BCR;
    }

    public void setBCR(double BCR) {
        this.BCR = BCR;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public WVertex(String vertexInfo, String taskType) {
        this.vertexInfo = vertexInfo;
        rt = 0;
        perf_profile = new TreeMap<Integer, Double>();
        perf_profileCpu = new TreeMap<MemoryCpuKey, Double>();
        mem = 256;
        cpu = 0.2f;
        available_mem_list = null;
        cost = 0;
        node_delay = 0;
        BCR = 0;
        this.taskType = taskType;
    }

    public WVertex(WVertex vertex) {
        this.vertexInfo = vertex.vertexInfo;
        this.rt = vertex.rt;
        this.cost = vertex.cost;
        this.node_delay = vertex.node_delay;
        this.BCR = vertex.BCR;
        this.mem = vertex.mem;
        this.cpu = vertex.cpu;
        this.perf_profile = new TreeMap<Integer, Double>();
        this.perf_profileCpu = new TreeMap<MemoryCpuKey, Double>();
        for (Integer key : vertex.perf_profile.keySet())
            this.perf_profile.put(key, vertex.perf_profile.get(key));
        this.available_mem_list = new int[vertex.available_mem_list.length];
        for (int i = 0; i < this.available_mem_list.length; i++) {
            this.available_mem_list[i] = vertex.available_mem_list[i];
        }
    }

    @Override
    public String toString() {  //toString的返回值会显示在绘制的图中
        return this.vertexInfo;
    }

    @Override
    public int compareTo(Object o) {
        WVertex temp = (WVertex) o;
        if (this.vertexInfo.length() != temp.vertexInfo.length())
            return this.vertexInfo.length() - temp.vertexInfo.length();
        else
            return this.vertexInfo.compareTo(temp.vertexInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WVertex vertex = (WVertex) o;
        return vertexInfo.equals(vertex.vertexInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertexInfo);
    }
}
