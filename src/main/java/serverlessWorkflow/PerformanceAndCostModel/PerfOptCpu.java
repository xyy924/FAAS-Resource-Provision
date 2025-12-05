package serverlessWorkflow.PerformanceAndCostModel;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import serverlessWorkflow.graph.WVertex;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
public class PerfOptCpu {
    private ServerlessAppWorkflow App;
    private int[] avalable_mem_list;
    private double cost_under_maximal_mem_configuration, cost_under_minimal_mem_configuration, rt_under_maximal_mem_configuration, rt_under_minimal_mem_configuration;
    private TreeMap<WVertex, MemoryCpuKey> minimumCostConfiguration = new TreeMap<>();
    private TreeMap<WVertex, MemoryCpuKey> bestPerformanceConfiguration = new TreeMap<>();
    private TreeMap<WVertex, MemoryCpuKey> maximumCostConfiguration = new TreeMap<>();
    private TreeMap<WVertex, MemoryCpuKey> worstPerformanceConfiguration = new TreeMap<>();
    private TreeMap<WVertex, HashMap<MemoryCpuKey, Double>> avgCostMap = new TreeMap<>();
    private TreeMap<WVertex, MemoryCpuKey> minimal_mem_configuration, maximal_mem_configuration;

    public ServerlessAppWorkflow getApp() {
        return App;
    }

    public int[] getAvalable_mem_list() {
        return avalable_mem_list;
    }

    public double getCost_under_maximal_mem_configuration() {
        return cost_under_maximal_mem_configuration;
    }

    public double getCost_under_minimal_mem_configuration() {
        return cost_under_minimal_mem_configuration;
    }

    public double getRT_under_maximal_mem_configuration() {
        return rt_under_maximal_mem_configuration;
    }

    public double getRT_under_minimal_mem_configuration() {
        return rt_under_minimal_mem_configuration;
    }

    public TreeMap<WVertex, MemoryCpuKey> getMinimal_mem_configuration() {
        return minimal_mem_configuration;
    }

    public TreeMap<WVertex, MemoryCpuKey> getMaximal_mem_configuration() {
        return maximal_mem_configuration;
    }
    public TreeMap<WVertex, MemoryCpuKey> getMinimumCostConfiguration() {
        return minimumCostConfiguration;
    }

    public TreeMap<WVertex, MemoryCpuKey> getBestPerformanceConfiguration() {
        return bestPerformanceConfiguration;
    }

    public PerfOptCpu(PerfOptCpu opt){
        this.App = new ServerlessAppWorkflow(opt.App);
        this.avalable_mem_list = opt.avalable_mem_list;
        this.cost_under_maximal_mem_configuration = opt.cost_under_maximal_mem_configuration;
        this.cost_under_minimal_mem_configuration = opt.cost_under_minimal_mem_configuration;
        this.rt_under_maximal_mem_configuration = opt.rt_under_maximal_mem_configuration;
        this.rt_under_minimal_mem_configuration = opt.rt_under_minimal_mem_configuration;
        this.maximal_mem_configuration = opt.maximal_mem_configuration;
        this.minimal_mem_configuration = opt.minimal_mem_configuration;
        this.minimumCostConfiguration = opt.minimumCostConfiguration;
        this.bestPerformanceConfiguration = opt.bestPerformanceConfiguration;
        this.maximumCostConfiguration = opt.maximumCostConfiguration;
        this.worstPerformanceConfiguration = opt.worstPerformanceConfiguration;
        this.avgCostMap = opt.avgCostMap;
        this.generate_perf_profile();
        this.get_optimization_boundary();
    }

    public PerfOptCpu(ServerlessAppWorkflow Appworkflow, boolean generate_perf_profile, int[] mem_list) {
        this.App = Appworkflow;
        // this.avalable_mem_list = new int[10240 - 191];
        this.avalable_mem_list = new int[(4096 - 256)/64+1];
        if (mem_list == null) {
            for (int i = 0; i <= (4096 - 256)/64; i++)
                avalable_mem_list[i] = 256+i*64;
        } else
            this.avalable_mem_list = mem_list;
        if (generate_perf_profile == true)
            this.generate_perf_profile();
        this.get_optimization_boundary();
    }

    private void generate_perf_profile() {
        WVertex[] node_list = this.App.getGraph().getDirectedGraph().vertexSet().toArray(new WVertex[0]);
        for (int i = 0; i < node_list.length; i++) {
            String perf_profile_path = null;
            String jsonContent = null;
            try {
                perf_profile_path = new File("").getCanonicalPath() + "/src/main/resources/aliyunfc_functions_perf_profile_cpu/f" +
                        (i + 1) + "_perf_profile.json";
                jsonContent = FileUtils.readFileToString(new File(perf_profile_path), "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject(jsonContent);

            Map<String, Double> knownData = new HashMap<>();
            for (String key : jsonObject.keySet()) {
                knownData.put(key, jsonObject.getDouble(key));
            }


            for (int mem = 256; mem <= 4096; mem += 64) { // 内存范围：128 MB 到 4 GB，步长 64 MB
                for (float vcpu : calculateVcpuOptions(mem,0.05F)) { // vCPU 范围：0.05 到 16，步长 0.05
                    MemoryCpuKey mc = new MemoryCpuKey(mem, vcpu);
                    String key = mem + "," + vcpu;
                    if(jsonObject.has(key)){
                        double time =  jsonObject.getDouble(key);
                        node_list[i].getPerf_profileCpu().put(mc, time);
                    }

                    //TODO 模拟计算未知样本的值 插值，机器学习
                    else{
                        node_list[i].getPerf_profileCpu().put(mc, simulateMissingValue(mem, vcpu, knownData));

                    }

                }
            }


        }
//        this.appgenerator.get_rt_mem_data(this.App.G.node_num, node_list);
    }

    private void get_optimization_boundary(){
        Set<WVertex> verticesSet = this.App.getGraph().getDirectedGraph().vertexSet();
        this.minimal_mem_configuration = new TreeMap<WVertex, MemoryCpuKey>();
        this.maximal_mem_configuration = new TreeMap<WVertex, MemoryCpuKey>();
        for (WVertex aVertex : verticesSet) {
            this.minimal_mem_configuration.put(aVertex, aVertex.getPerf_profileCpu().firstKey());
            this.maximal_mem_configuration.put(aVertex, aVertex.getPerf_profileCpu().lastKey());

            HashMap<MemoryCpuKey, Double> memCostMap = new HashMap<>();
            double maxRT = Double.MIN_VALUE;
            double maxCost = Double.MIN_VALUE, minCost = Double.MAX_VALUE, minRT = Double.MAX_VALUE;
            int memOfMaxRt = 0, memOfMaxCost = 0, memOfMinRT = 0, memOfMinCost = 0;
            float cpuOfMaxRt = 0, cpuOfMaxCost = 0, cpuOfMinRT = 0, cpuOfMinCost = 0;
            for (Map.Entry<MemoryCpuKey, Double> item : aVertex.getPerf_profileCpu().entrySet()) {
                int mem = item.getKey().getMemory();
                float cpu = item.getKey().getCpu();
                double rt = item.getValue();
                double cost = this.App.GetVertexCostInMemCpu(aVertex, mem, cpu);
                memCostMap.put(new MemoryCpuKey(mem, cpu), cost);
                if (rt > maxRT) {
                    maxRT = rt;
                    memOfMaxRt = mem;
                    cpuOfMaxRt = cpu;
                }
                if (rt < minRT) {
                    minRT = rt;
                    memOfMinRT = mem;
                    cpuOfMinRT = cpu;
                }
                if (cost > maxCost) {
                    maxCost = cost;
                    memOfMaxCost = mem;
                    cpuOfMaxCost = cpu;
                }
                if (cost < minCost) {
                    minCost = cost;
                    memOfMinCost = mem;
                    cpuOfMinCost = cpu;
                }
            }
            avgCostMap.put(aVertex, memCostMap);
            this.minimumCostConfiguration.put(aVertex, new MemoryCpuKey(memOfMinCost, cpuOfMinCost));
            //最小成本下的配置
            this.bestPerformanceConfiguration.put(aVertex, new MemoryCpuKey(memOfMinRT, cpuOfMinRT));
            this.maximumCostConfiguration.put(aVertex, new MemoryCpuKey(memOfMaxCost, cpuOfMaxCost));
            this.worstPerformanceConfiguration.put(aVertex, new MemoryCpuKey(memOfMaxRt, cpuOfMaxRt));

            this.update_App_workflow_mem_rt_cost(this.minimumCostConfiguration);
            this.cost_under_minimal_mem_configuration = this.App.GetAverageCost();
            this.update_App_workflow_mem_rt_cost(this.maximumCostConfiguration);
            this.cost_under_maximal_mem_configuration = this.App.GetAverageCost();
            this.update_App_workflow_mem_rt_cost(this.bestPerformanceConfiguration);
            this.rt_under_maximal_mem_configuration = this.App.GetAverageRT();
            this.update_App_workflow_mem_rt_cost(this.worstPerformanceConfiguration);
            this.rt_under_minimal_mem_configuration = this.App.GetAverageRT();
        }
    }

    public void update_App_workflow_mem_rt_cost(TreeMap<WVertex, MemoryCpuKey> mem_dict) {
        Set<WVertex> vertexArr = mem_dict.keySet();
        for (WVertex aVertex : vertexArr) {
            aVertex.setMem(mem_dict.get(aVertex).getMemory());
            aVertex.setCpu(mem_dict.get(aVertex).getCpu());
            aVertex.setRt(aVertex.getPerf_profileCpu().get(new MemoryCpuKey(aVertex.getMem(), aVertex.getCpu())));
            this.App.UpdateVertexCostCpu(aVertex);
        }
    }


    public ArrayList<Float> calculateVcpuOptions(int memorySizeMB, float step) {
        BigDecimal MIN_VCPU = new BigDecimal(Float.toString(0.15F));
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


    public double simulateMissingValue(int mem, float vcpu, Map<String, Double> knownData) {
        // 获取已知数据点
        List<double[]> knownPoints = new ArrayList<>();
        List<Double> knownValues = new ArrayList<>();

        for (Map.Entry<String, Double> entry : knownData.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int knownMem = Integer.parseInt(parts[0]);
            float knownVcpu = Float.parseFloat(parts[1]);
            double value = entry.getValue();

            knownPoints.add(new double[]{knownMem, knownVcpu});
            knownValues.add(value);
        }

        // 当前点
        double[] point = new double[]{mem, vcpu};

        // 使用线性插值法（可以根据需要替换为其他方法）
        return interpolateLinear(point, knownPoints, knownValues);
    }

    // 线性插值法
    private double interpolateLinear(double[] point, List<double[]> knownPoints, List<Double> knownValues) {
        if (knownPoints.isEmpty()) {
            throw new IllegalArgumentException("No known data points available for interpolation.");
        }

        // 寻找最近的两个点进行插值
        double minDistance = Double.MAX_VALUE;
        double closestValue1 = 0, closestValue2 = 0;
        double[] closestPoint1 = null, closestPoint2 = null;

        for (int i = 0; i < knownPoints.size(); i++) {
            double distance = calculateDistance(point, knownPoints.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint1 = knownPoints.get(i);
                closestValue1 = knownValues.get(i);

                // 找到第二个最近点
                if (i + 1 < knownPoints.size()) {
                    closestPoint2 = knownPoints.get(i + 1);
                    closestValue2 = knownValues.get(i + 1);
                } else if (i - 1 >= 0) {
                    closestPoint2 = knownPoints.get(i - 1);
                    closestValue2 = knownValues.get(i - 1);
                }
            }
        }

        // 如果只有一个点，直接返回该点的值
        if (closestPoint2 == null) {
            return closestValue1;
        }

        // 线性插值公式
        double weight1 = 1 / calculateDistance(point, closestPoint1);
        double weight2 = 1 / calculateDistance(point, closestPoint2);
        return (closestValue1 * weight1 + closestValue2 * weight2) / (weight1 + weight2);
    }

    // 计算两点之间的欧几里得距离
    private double calculateDistance(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

}
