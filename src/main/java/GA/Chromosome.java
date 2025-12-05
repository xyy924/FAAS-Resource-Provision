package GA;

import GA.GA_SAL_OPT;
import serverlessWorkflow.PerformanceAndCostModel.MemoryCpuKey;
import serverlessWorkflow.graph.WVertex;
import serverlessWorkflow.PerformanceAndCostModel.PerfOptCpu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Chromosome {
    private int ID;
    private double rt;
    private double cost;
    private double fitness;
    private TreeMap<WVertex, MemoryCpuKey> memConfig;
    private static PerfOptCpu perfOptcpu;
    private static String OPTType;

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getFitness() {
        return fitness;
    }

    public TreeMap<WVertex, MemoryCpuKey> getMemConfig() {
        return memConfig;
    }

    public static void setPerfOptCpu(PerfOptCpu perfOptcpu) {
        GA.Chromosome.perfOptcpu = perfOptcpu;
    }

    public static String getOPTType() {
        return OPTType;
    }

    public static void setOPTType(String OPTType) {
        GA.Chromosome.OPTType = OPTType;
    }

    public Chromosome(int i, WVertex[] vertices) {
        this.ID = i;
        this.fitness = -1;
        this.rt = this.cost = Double.MAX_VALUE;
        if (vertices != null)
            this.GenerateMemConfig(vertices);
    }

    public void GenerateMemConfig(WVertex[] vertices) {
        this.memConfig = new TreeMap<>();
        Random rand = new Random();
        double randNum = rand.nextDouble();
        if (randNum <= 0.5) {
            if(OPTType.equals("BCPO")){
                TreeMap<WVertex, MemoryCpuKey> memConfiguration = perfOptcpu.getMinimumCostConfiguration();
                for(Map.Entry<WVertex,MemoryCpuKey> item : memConfiguration.entrySet())
                    memConfig.put(item.getKey(), item.getValue());
            }else if(OPTType.equals("PCCO")){
                TreeMap<WVertex,MemoryCpuKey> memConfiguration = perfOptcpu.getBestPerformanceConfiguration();
                for(Map.Entry<WVertex,MemoryCpuKey> item : memConfiguration.entrySet())
                    memConfig.put(item.getKey(), item.getValue());
            }
        }else {
            for (int i = 0; i < vertices.length; i++){
                int mem  = 256 + ThreadLocalRandom.current().nextInt(0,(4096-256)/64+1) * 64 ;
                ArrayList<Float> vcpuOptions = calculateVcpuOptions(mem, 0.05F);
                float cpu = vcpuOptions.get(ThreadLocalRandom.current().nextInt(0, vcpuOptions.size()));
                memConfig.put(vertices[i],new MemoryCpuKey(mem,cpu));
            }
        }
    }

    public void GetFitness(double constraint) {
        if (GA.Chromosome.OPTType.equals("BCPO")) {
            if (this.cost <= constraint)
                this.fitness = 1 / this.rt;
            else
                this.fitness = (-1) * this.cost / constraint;
        } else if (GA.Chromosome.OPTType.equals("PCCO")) {
            if (this.rt <= constraint)
                this.fitness = 1 / this.cost;
            else
                this.fitness = (-1) * this.rt / constraint;
        }
        this.fitness = this.fitness * 10000;
    }

    public void TranslateDNA() {
        GA.Chromosome.perfOptcpu.update_App_workflow_mem_rt_cost(this.memConfig);
        this.cost = Chromosome.perfOptcpu.getApp().GetAverageCost();
        this.rt = Chromosome.perfOptcpu.getApp().GetAverageRT();
    }

    public void CrossOver(GA_SAL_OPT GA_sal_opt, double constraint) {
        Random rand = new Random();
        double crossOverRate = rand.nextDouble();
        if (crossOverRate <= GA_sal_opt.getCROSS_RATE()) {
            int mom;
            do {
                mom = rand.nextInt(GA_sal_opt.getChromosomes().size());
            } while (mom == (this.ID - 1));

            GA.Chromosome child1 = new GA.Chromosome(this.ID, null);
            child1.memConfig = new TreeMap<WVertex, MemoryCpuKey>();
            GA.Chromosome child2 = new GA.Chromosome(this.ID, null);
            child2.memConfig = new TreeMap<WVertex, MemoryCpuKey>();
            for (WVertex vertex : this.memConfig.keySet())
                child1.memConfig.put(vertex, this.memConfig.get(vertex));
            for (WVertex vertex : GA_sal_opt.getChromosomes().get(mom).memConfig.keySet())
                child2.memConfig.put(vertex, GA_sal_opt.getChromosomes().get(mom).memConfig.get(vertex));

            MemoryCpuKey[] memOfDad = this.memConfig.values().toArray(new MemoryCpuKey[0]);
            MemoryCpuKey[] memOfMom = GA_sal_opt.getChromosomes().get(mom).memConfig.values().toArray(new MemoryCpuKey[0]);
            WVertex[] verticesOfChild1 = child1.memConfig.keySet().toArray(new WVertex[0]);
            WVertex[] verticesOfChild2 = child2.memConfig.keySet().toArray(new WVertex[0]);

            int crossPointLeft = rand.nextInt(child1.memConfig.size());
            int crossPointRight = rand.nextInt(child1.memConfig.size());
            if (crossPointLeft > crossPointRight) {
                int swapTemp = crossPointLeft;
                crossPointLeft = crossPointRight;
                crossPointRight = swapTemp;
            }

            for (int i = crossPointLeft; i <= crossPointRight; i++) {
                child1.memConfig.replace(verticesOfChild1[i], memOfMom[i]);
                child2.memConfig.replace(verticesOfChild2[i], memOfDad[i]);
            }

            child1.TranslateDNA();
            child1.getFitness();
            child1.isExcellent(child2, constraint);
            this.isExcellent(child1, constraint);
        }
    }

    public void Mutate(GA_SAL_OPT GA_sal_opt, double constraint) {
        GA.Chromosome child = new GA.Chromosome(-1, null);
        child.memConfig = new TreeMap<>();
        for (WVertex vertex : this.memConfig.keySet())
            child.memConfig.put(vertex, this.memConfig.get(vertex));
        WVertex[] vertices = child.memConfig.keySet().toArray(new WVertex[0]);
        for (int i = 0; i < vertices.length; i++) {
            double rate = new Random().nextDouble();
            if (rate <= GA_sal_opt.getMUTATIONRATE()) {
                float[] newMemCpu = this.Polynomial_mutation(vertices[i], CpuToIndex(vertices[i].getCpu()),CpuToIndex(4.0F), constraint, GA_sal_opt,vertices[i].getMem(), vertices[i].getTaskType());
                child.memConfig.replace(vertices[i], new MemoryCpuKey((int) newMemCpu[0],newMemCpu[1]));

            }
        }
        this.isExcellent(child, constraint);
    }

    public float[] Polynomial_mutation(WVertex vertex, int currentIndex, int maxIndex, double constraint, GA_SAL_OPT GA_sal_opt,int currentMem, String type) {
        double delta1 = (double) currentIndex / maxIndex;
        double delta2 = (double) (maxIndex - currentIndex) / maxIndex;
        double u = 0;
        double xy, val, deltaq;
        int newIndex = currentIndex;
        float newcpu = CpuToIndex(currentIndex);
        int mem = currentMem;
        int num = 0;
        double eta = GA_sal_opt.getETA_M_();;
        for (; num < 10; num++) {
            if ((GA.Chromosome.OPTType.equals("PCCO") && this.rt < constraint) || (GA.Chromosome.OPTType.equals("BCPO") && this.cost >= constraint)){
                if(type.equals("Disk I/O") && currentIndex<10){
                    u = new Random().nextDouble();
                    eta = GA_sal_opt.getETA_M_()+20;
                }
                else if(!type.equals("Disk I/O") && currentIndex<17) {
                    u = new Random().nextDouble();
                    eta = GA_sal_opt.getETA_M_()+20;
                }
                else u = new Random().nextDouble() * 0.5;
            }
            else if ((GA.Chromosome.OPTType.equals("PCCO") && this.rt >= constraint) || (GA.Chromosome.OPTType.equals("BCPO") && this.cost < constraint))
            {
                if(type.equals("Disk I/O") && currentIndex<10){
                    u = new Random().nextDouble() * 0.5 + 0.5;
                    eta = GA_sal_opt.getETA_M_() + 20;
                }
                else if(!type.equals("Disk I/O") && currentIndex<17) {
                    u = new Random().nextDouble() * 0.5 + 0.5;
                    eta = GA_sal_opt.getETA_M_() + 20;;
                }
                else u = new Random().nextDouble();
            }

            double mut_pow = 1.0 / (eta + 1.0);
            if (u <= 0.5) {
                xy = 1.0 - delta1;
                val = 2.0 * u + (1.0 - 2.0 * u) * Math.pow(xy, (1.0 + eta));
                deltaq = Math.pow(val, mut_pow) - 1.0;
            } else {
                xy = 1.0 - delta2;
                val = 2.0 * (1.0 - u) + 2.0 * (u - 0.5) * Math.pow(xy, (1.0 + eta));
                deltaq = 1.0 - Math.pow(val, mut_pow);
            }
            newIndex = currentIndex + (int) Math.round(deltaq * maxIndex);
            if (newIndex < 0)
                newIndex = 0;
            if (newIndex > maxIndex)
                newIndex = maxIndex;

            newcpu =IndexToCpu(newIndex);
            mem  = mutatemem(newcpu);

            if ((GA.Chromosome.OPTType.equals("PCCO") && this.rt < constraint) || (GA.Chromosome.OPTType.equals("BCPO") && this.cost >= constraint)) {
                if (perfOptcpu.getApp().GetVertexCostInMemCpu(vertex, mem, newcpu) < perfOptcpu.getApp().GetVertexCostInMemCpu(vertex, currentMem, IndexToCpu(currentIndex)))
                    break;
            } else if ((GA.Chromosome.OPTType.equals("PCCO") && this.rt >= constraint) || (GA.Chromosome.OPTType.equals("BCPO") && this.cost < constraint)) {
                if (vertex.getPerf_profileCpu().get(new MemoryCpuKey(mem, newcpu)) < vertex.getPerf_profileCpu().get(new MemoryCpuKey(currentMem, IndexToCpu(currentIndex))))
                    break;
            }
        }

        if (num == 10) {
            newIndex = currentIndex;
            newcpu = IndexToCpu(newIndex);
            mem = currentMem;
        }

        return new float[]{mem,newcpu};
    }

    public void isExcellent(GA.Chromosome child, double constraint) {
        child.TranslateDNA();
        child.GetFitness(constraint);
        if (this.fitness < child.fitness) {
            this.memConfig = child.memConfig;
            this.rt = child.rt;
            this.cost = child.cost;
            this.fitness = child.fitness;
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

    public int CpuToIndex(float cpu){
        BigDecimal Cpu = new BigDecimal(Float.toString(cpu));
        BigDecimal  index = Cpu.subtract(new BigDecimal("0.15")).divide(new BigDecimal("0.05"));
        return index.intValue();
    }

    public float IndexToCpu(int index) {
        return (float) (0.15+index*0.05);
    }

    public int MemToIndex(int mem) {
        return (mem-256)/64;
    }

    public int IndexToMem(int index) {
        return 256+index*64;
    }

    public ArrayList<Integer> getMemOptions(float cpusize) {
        BigDecimal  MIN_MEM = new BigDecimal("256");
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

    public int mutatemem(float cpu) {
       //随机从可用内存列表中选择一个新的内存大小
        ArrayList<Integer> memOptions = getMemOptions(cpu);
        return memOptions.get(ThreadLocalRandom.current().nextInt(0,memOptions.size()));
    }

}