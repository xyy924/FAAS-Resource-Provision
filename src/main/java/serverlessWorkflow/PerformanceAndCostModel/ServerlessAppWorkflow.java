package serverlessWorkflow.PerformanceAndCostModel;


import aliyunfc.Util.Tools;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import org.jgrapht.GraphPath;
import serverlessWorkflow.graph.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

public class ServerlessAppWorkflow {
    private double pgs;
    private double ppr;
    private String platform;
    private APPGraph Graph;
    private long sizeOfOSS;

    public APPGraph getGraph() {
        return Graph;
    }

    public void setGraph(APPGraph g) {
        Graph = g;
    }

    public ServerlessAppWorkflow(APPGraph G, String delayType, String platform, double PGS, double PPR) {
        this.Graph = G;
        Set<WVertex> vertexSet = G.getDirectedGraph().vertexSet();
        Set<WEdge> edgeSet = G.getDirectedGraph().edgeSet();
        if (delayType.equals("None")) {
            for (WVertex vertex : vertexSet)
                vertex.setNode_delay(0);
            for (WEdge edge : edgeSet)
                edge.setEdge_delay(0);
        } else if (delayType.equals("SFN")) {
            for (WVertex vertex : vertexSet)
//                vertex.setNode_delay(100);
                vertex.setNode_delay(100);
            for (WEdge edge : edgeSet)
//                edge.setEdge_delay(70);
                edge.setEdge_delay(600);
        } else if (delayType.equals("Defined")) {
            double[] node_delay = G.getNode_delay();
            WVertex[] vertexs = vertexSet.toArray(new WVertex[0]);
            for (int i = 0; i < this.getGraph().getNode_num(); i++)
                vertexs[i].setNode_delay(node_delay[i]);
            double[] edge_delay = G.getEdge_delay();
            WEdge[] edges = edgeSet.toArray(new WEdge[0]);
            for (int i = 0; i < edge_delay.length; i++)
                edges[i].setEdge_delay(edge_delay[i]);
        }

        if (PGS == -1)

            this.pgs = 0.0000166667;
        else
            this.pgs = PGS;
        if (PPR == -1)
            this.ppr = 0.0000009;
        else
            this.ppr = PPR;

        this.platform = platform;
        getSizeOfOSS();
    }

    public ServerlessAppWorkflow(ServerlessAppWorkflow serverlessAppWorkflow){
        this.pgs = serverlessAppWorkflow.pgs;
        this.ppr = serverlessAppWorkflow.ppr;
        this.platform = serverlessAppWorkflow.platform;
        this.sizeOfOSS = serverlessAppWorkflow.sizeOfOSS;
        try{
            this.Graph = new APPGraph(new File("").getCanonicalPath() + "/src/main/resources/serverless_workflow_json_files/" +
                    serverlessAppWorkflow.Graph.getAPPName() + ".json");
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void getSizeOfOSS(){
//        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com"; // 替换为您的Endpoint
//        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");  // 环境变量中获取AccessKey ID
//        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
//        String bucketName = "serverless-exp"; // 替换为您的Bucket名称
////        String prefix = "yourPrefix/"; // 替换为您的目录前缀
//
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        long totalSize = 0L;
//        String marker = null;
//
//        try {
//            ObjectListing listing;
//            do {
//                ListObjectsRequest request = new ListObjectsRequest(bucketName).withMarker(marker);
//                listing = ossClient.listObjects(request);
//
//                for (OSSObjectSummary summary : listing.getObjectSummaries()) {
//                    totalSize += summary.getSize();
//                }
//
//                marker = listing.getNextMarker();
//            } while (listing.isTruncated());
//            System.out.println("Total file size: " + totalSize + " bytes");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (ossClient != null) {
//                ossClient.shutdown();
//            }
//        }
//        this.sizeOfOSS = totalSize;
        this.sizeOfOSS = 8953447;
    }

    public double GetTPOfAPath(GraphPath<WVertex, WEdge> path) {
        double tp = 1.0;
        List<WEdge> edges = path.getEdgeList();
        for (WEdge edge : edges)
            tp = tp * edge.getWeight();
        return tp;
    }

    public double GetRTOfAPath(GraphPath<WVertex, WEdge> path) {
        double rt = 0.0;
        List<WEdge> edges = path.getEdgeList();
        List<WVertex> vertices = path.getVertexList();

        for (WEdge aEdge : edges)
            rt = rt + aEdge.getEdge_delay();
        for (WVertex aVertex : vertices)
            rt = rt + aVertex.getRt() + aVertex.getNode_delay();
        return rt;
    }

    public void UpdateVertexCost(WVertex vertex) {
        if (vertex.getTaskType().equals("Network I/O")) {
            float step=0.05F;
            if(vertex.getMem()>=2048) step=0.1F;

            double costOfNetworkTask = (Tools.getMaxVcpu(vertex.getMem(),step) * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000;
            costOfNetworkTask += (double) sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
            vertex.setCost(costOfNetworkTask);
        } else{
            float step=0.05F;
            if(vertex.getMem()>=2048) step=0.1F;

            vertex.setCost((Tools.getMaxVcpu(vertex.getMem(),step) * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000);

        }

    }

    public void UpdateVertexCost_by_statemachine(WVertex vertex) {
        if (vertex.getTaskType().equals("Network I/O")) {
            float step=0.05F;
            if(vertex.getMem()>=2048) step=0.1F;
            double costOfNetworkTask = (Tools.getMaxVcpu(vertex.getMem(),step) * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000;
            costOfNetworkTask += (double) sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
            vertex.setCost(costOfNetworkTask);
        } else{
            float step=0.05F;
            if(vertex.getMem()>=2048) step=0.1F;

            vertex.setCost((Tools.getMaxVcpu(vertex.getMem(),step) * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000);

        }

    }



    public void UpdateVertexCostCpu(WVertex vertex) {
        if (vertex.getTaskType().equals("Network I/O")) {

            double costOfNetworkTask = (vertex.getCpu() * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000;
            costOfNetworkTask += (double) sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
            vertex.setCost(costOfNetworkTask);
        } else{
            vertex.setCost((vertex.getCpu() * vertex.getRt() / 1000.0 * 1
                    + vertex.getMem() / 1024.0 * vertex.getRt() / 1000.0 * 0.15 + 0.0075)*0.00012*10000000);

        }

    }

    public double GetVertexCostInMem(WVertex vertex, int mem) {
        float step=0.05F;
        if(mem>2048) step=0.1F;
        double rt = vertex.getPerf_profile().get(mem);
        double cost = (Tools.getMaxVcpu(mem,step) * rt / 1000.0 * 1 + mem / 1024.0 * rt / 1000.0 * 0.15 + 75.0 / 10000)*0.00012*10000000;
        if (vertex.getTaskType().equals("Network I/O"))
            cost += (double) sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
        return cost;
    }

    public double GetVertexCostInMemCpu(WVertex vertex, int mem, float cpu) {

        double rt = vertex.getPerf_profileCpu().get(new MemoryCpuKey(mem,cpu));
        double cost = (cpu * rt / 1000.0 * 1 + mem / 1024.0 * rt / 1000.0 * 0.15 + 75.0 / 10000)*0.00012*10000000;
        if (vertex.getTaskType().equals("Network I/O"))
            cost += (double) sizeOfOSS / 1024 / 1024 / 1024 * 0.25 * 10000000;
        return cost;
    }

//    public double GetAverageRT() {
//        double ERT = 0;
//        Set<PathsInGraph> allExecutionInstances = GetExecutionInstanceBasedOnDepthFirstSearch(this.getGraph().getStart());
//        for (PathsInGraph executionInstance : allExecutionInstances)
//            ERT += executionInstance.rt * executionInstance.tp;
//        return ERT;
//    }

    public double GetAverageRT(){

        memort.clear(); // 清除缓存
        return calculateExpectedTimeRecursive(this.getGraph().getStart());

    }

//    public double GetAverageCost() {
//        double COST = 0;
//        Set<PathsInGraph> allExecutionInstances = GetExecutionInstanceBasedOnDepthFirstSearch(this.getGraph().getStart());
//        for (PathsInGraph executionInstance : allExecutionInstances)
//            COST += executionInstance.cost * executionInstance.tp;
//        return COST;
//    }
    public double GetAverageCost(){
        double cost  = computeCost();
//        getNodeProbabilities();
        return cost;
    }

    class PathsInGraph {
        double tp;
        Set<WVertex> vertices = new HashSet<>();
        Set<WEdge> edges = new HashSet<>();
        double rt;
        double cost;
    }

    public Set<PathsInGraph> GetExecutionInstanceBasedOnDepthFirstSearch(WVertex vertex) {
        if (vertex.equals(this.getGraph().getEnd())) {
            PathsInGraph pathsInGraph = new PathsInGraph();
            Set<PathsInGraph> returnValue = new HashSet<>();
            pathsInGraph.tp = 1.0;
            pathsInGraph.vertices.add(this.getGraph().getEnd());
            pathsInGraph.rt = this.getGraph().getEnd().getRt() + this.getGraph().getEnd().getNode_delay();
            pathsInGraph.cost = this.getGraph().getEnd().getCost();
            returnValue.add(pathsInGraph);
            return returnValue;
        } else {
            ArrayList<Set<PathsInGraph>> choice = new ArrayList<>();
            ArrayList<Set<PathsInGraph>> parallel = new ArrayList<>();
            Set<WEdge> outGoingEdges = this.getGraph().getDirectedGraph().outgoingEdgesOf(vertex);
            for (WEdge aOutGoingEdge : outGoingEdges) {
                WVertex targetVertex = this.getGraph().getDirectedGraph().getEdgeTarget(aOutGoingEdge);
                Set<PathsInGraph> pathsInGraph = GetExecutionInstanceBasedOnDepthFirstSearch(targetVertex);
                for (PathsInGraph path : pathsInGraph) {
                    path.vertices.add(vertex);
                    path.edges.add(aOutGoingEdge);
                    path.tp *= aOutGoingEdge.getWeight();
                    path.rt = path.rt + vertex.getRt() + vertex.getNode_delay() + aOutGoingEdge.getEdge_delay();
                    path.cost += vertex.getCost();
                }
                if ((1.0 - aOutGoingEdge.getWeight()) < Math.pow(10, -6))
                    parallel.add(pathsInGraph);
                else
                    choice.add(pathsInGraph);
            }


            Set<PathsInGraph> executionInstance = new HashSet<>();
            if (parallel.size() != 0) {
                for (int i = 0; i < parallel.size(); i++) {
                    if (i == 0) {
                        Set<PathsInGraph> pathsInGraphs = parallel.get(0);
                        for (PathsInGraph aPathsInGraph : pathsInGraphs)
                            executionInstance.add(aPathsInGraph);
                    } else {
                        Set<PathsInGraph> tempExecutionInstance = new HashSet<>();
                        Set<PathsInGraph> pathsInGraphs = parallel.get(i);
                        for (PathsInGraph pathInExecutionInstance : executionInstance) {
                            for (PathsInGraph pathsInParalleli : pathsInGraphs) {
                                PathsInGraph aNewPath = new PathsInGraph();
                                aNewPath.tp = pathInExecutionInstance.tp * pathsInParalleli.tp;
                                aNewPath.rt = Math.max(pathInExecutionInstance.rt, pathsInParalleli.rt);
                                aNewPath.cost = pathInExecutionInstance.cost + pathsInParalleli.cost;

                                for (WVertex aVertex : pathInExecutionInstance.vertices)
                                    aNewPath.vertices.add(aVertex);
                                for (WEdge aEdge : pathInExecutionInstance.edges)
                                    aNewPath.edges.add(aEdge);
                                for (WVertex aVertex : pathsInParalleli.vertices) {
                                    if (aNewPath.vertices.contains(aVertex))
                                        aNewPath.cost -= aVertex.getCost();
                                    else
                                        aNewPath.vertices.add(aVertex);
                                }
                                for (WEdge aEdge : pathsInParalleli.edges)
                                    aNewPath.edges.add(aEdge);
                                tempExecutionInstance.add(aNewPath);
                            }
                        }
                        executionInstance = tempExecutionInstance;
                    }
                }

                if (choice.size() != 0) {
                    for (int i = 0; i < choice.size(); i++) {
                        Set<PathsInGraph> tempExecutionInstance = new HashSet<>();
                        Set<PathsInGraph> pathsInGraphs = choice.get(i);
                        for (PathsInGraph pathInExecutionInstance : executionInstance) {
                            for (PathsInGraph pathInChoicei : pathsInGraphs) {
                                PathsInGraph aNewPath = new PathsInGraph();
                                aNewPath.tp = pathInExecutionInstance.tp * pathInChoicei.tp;
                                aNewPath.rt = Math.max(pathInExecutionInstance.rt, pathInChoicei.rt);
                                aNewPath.cost = pathInExecutionInstance.cost + pathInChoicei.cost;

                                for (WVertex aVertex : pathInExecutionInstance.vertices)
                                    aNewPath.vertices.add(aVertex);
                                for (WEdge aEdge : pathInExecutionInstance.edges)
                                    aNewPath.edges.add(aEdge);
                                for (WVertex aVertex : pathInChoicei.vertices) {
                                    if (aNewPath.vertices.contains(aVertex))
                                        aNewPath.cost -= aVertex.getCost();
                                    else
                                        aNewPath.vertices.add(aVertex);
                                }
                                for (WEdge aEdge : pathInChoicei.edges)
                                    aNewPath.edges.add(aEdge);
                                tempExecutionInstance.add(aNewPath);
                            }
                        }
                        executionInstance = tempExecutionInstance;
                    }
                }
            } else {
                for (int i = 0; i < choice.size(); i++) {
                    Set<PathsInGraph> pathsInGraphs = choice.get(i);
                    for (PathsInGraph aPathsInGraph : pathsInGraphs)
                        executionInstance.add(aPathsInGraph);
                }
            }
            return executionInstance;
        }
    }

    private Map<WVertex, Double> memort = new HashMap<>();

    private double calculateExpectedTimeRecursive(WVertex node) {
        if (memort.containsKey(node)) {
            return memort.get(node);
        }

        // 当前节点自身时间（执行时间 + 节点延迟）
        double currentTime = node.getRt() + node.getNode_delay();

        // 获取所有出边
        Set<WEdge> outEdges = this.getGraph().getDirectedGraph().outgoingEdgesOf(node);

        // 如果没有子节点，返回当前节点时间
        if (outEdges.isEmpty()) {
            memort.put(node, currentTime);
            return currentTime;
        }

        // 分离并行分支和概率分支
        List<Double> parallelTimes = new ArrayList<>();
        List<SimpleEntry<Double, Double>> probPairsTimes = new ArrayList<>();

        for (WEdge edge : outEdges) {
            WVertex child = this.getGraph().getDirectedGraph().getEdgeTarget(edge);

            // 递归计算子节点时间 + 边延迟
            double childTime = calculateExpectedTimeRecursive(child) + edge.getEdge_delay();


            if (Math.abs(edge.getWeight() - 1.0) < 1e-6) {
                // 并行分支（权重=1）
                parallelTimes.add(childTime);
            } else {
                // 概率分支（权重<1）
                probPairsTimes.add(new SimpleEntry<>(childTime, edge.getWeight()));
            }
        }

        // 计算后续时间
        double subsequentTime;
        if (!parallelTimes.isEmpty() && !probPairsTimes.isEmpty()) {
            // 混合分支处理
            double maxParallelTime = Collections.max(parallelTimes);
            double weightedProbSumTime = 0.0;
            for (SimpleEntry<Double, Double> pair : probPairsTimes) {
                double childTime = pair.getKey();
                double weight = pair.getValue();
                // 与并行分支取最大值
                weightedProbSumTime += weight * Math.max(maxParallelTime, childTime);
            }
            subsequentTime = Math.max(maxParallelTime, weightedProbSumTime);

        } else if (!parallelTimes.isEmpty()) {
            // 纯并行分支
            subsequentTime = Collections.max(parallelTimes)+140;
        } else if (!probPairsTimes.isEmpty()) {
            // 纯概率分支
            double weightedSumTime = 0.0;
            for (SimpleEntry<Double, Double> pair : probPairsTimes) {
                double childTime = pair.getKey();
                double weight = pair.getValue();
                weightedSumTime += weight * childTime;
            }
            subsequentTime = weightedSumTime+50;


        } else {
            subsequentTime = 0.0;

        }
        // 总时间 = 当前节点时间 + 后续时间
        double totalTime = currentTime + subsequentTime;

        memort.put(node, totalTime);
        return totalTime + 220;
    }

    private final Map<WVertex, Double> visitProbability = new HashMap<>();
    private final Map<WVertex, Integer> inDegree = new HashMap<>();
    // 入口函数
    public double computeCost() {
        Queue<WVertex> queue = new LinkedList<>();

        // 初始化入度表和访问概率
        for (WVertex node : this.getGraph().getDirectedGraph().vertexSet()) {
            int inDeg = this.getGraph().getDirectedGraph().incomingEdgesOf(node).size();
            inDegree.put(node, inDeg);
            if (inDeg == 0) {
                queue.offer(node);
                visitProbability.put(node, 1.0); // 起点概率为1
            } else {
                visitProbability.put(node, 0.0); // 其他初始化为0
            }
        }

        // 拓扑遍历 + 概率传播
        while (!queue.isEmpty()) {
            WVertex current = queue.poll();
            double currentProb = visitProbability.get(current);

            for (WEdge edge : this.getGraph().getDirectedGraph().outgoingEdgesOf(current)) {
                WVertex target = this.getGraph().getDirectedGraph().getEdgeTarget(edge);
                double weight = edge.getWeight();

                // 传播概率累加
                double newProb = visitProbability.get(target) + currentProb * weight;
                visitProbability.put(target, newProb);

                // 入度减一并加入队列
                inDegree.put(target, inDegree.get(target) - 1);
                if (inDegree.get(target) == 0) {
                    queue.offer(target);
                }
            }
        }

        // 修正并行汇聚点（访问概率不应超过1）
        for (Map.Entry<WVertex, Double> entry : visitProbability.entrySet()) {
            if (entry.getValue() > 1.0) {
                visitProbability.put(entry.getKey(), 1.0);
            }
        }

        // 计算总成本 = 所有节点的执行概率 × 节点成本
        double totalCost = 0.0;
        for (WVertex node : this.getGraph().getDirectedGraph().vertexSet()) {
            double prob = visitProbability.getOrDefault(node, 0.0);
            totalCost += node.getCost() * prob;
        }

        return totalCost;
    }

    public Map<WVertex, Double> getNodeProbabilities() {
        for (Map.Entry<WVertex, Double> entry : visitProbability.entrySet()) {
            WVertex node = entry.getKey();
            double probability = entry.getValue();
            System.out.printf("Node %-10s -> Execution Probability: %.4f\n", node.getVertexInfo(), probability);
        }
        return visitProbability;
    }
}
