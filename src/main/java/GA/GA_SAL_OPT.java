package GA;

import GA.Chromosome;
import GA.GA_Result;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import serverlessWorkflow.PerformanceAndCostModel.MemoryCpuKey;
import serverlessWorkflow.PerformanceAndCostModel.PerfOptCpu;
import serverlessWorkflow.graph.WVertex;
import util.DataStoreTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class GA_SAL_OPT {
    private int N_GENES;
    private double CROSS_RATE;
    private int N_GENERATIONS;
    private double ETA_M_;
    private double MUTATIONRATE;
    private ArrayList<GA.Chromosome> chromosomes;
    private PerfOptCpu perfOptcpu;


    private HashMap<WVertex, HashMap<MemoryCpuKey, Double>> costProfile = new HashMap<>();

    public double getCROSS_RATE() {
        return CROSS_RATE;
    }

    public double getMUTATIONRATE() {
        return MUTATIONRATE;
    }

    public double getETA_M_() {
        return ETA_M_;
    }

    public ArrayList<GA.Chromosome> getChromosomes() {
        return chromosomes;
    }

    public PerfOptCpu getPerfOptCpu() {
        return perfOptcpu;
    }

    public GA_SAL_OPT(int N_GENES, double CROSS_RATE, double MUTATION_RATE, int N_GENERATIONS,
                        double ETA_M_, PerfOptCpu perfOptcpu) {
        this.N_GENES = N_GENES;
        this.CROSS_RATE = CROSS_RATE;
        this.MUTATIONRATE = MUTATION_RATE;
        this.N_GENERATIONS = N_GENERATIONS;
        this.ETA_M_ = ETA_M_;
        this.perfOptcpu = perfOptcpu;
        GA.Chromosome.setPerfOptCpu(this.perfOptcpu);
        this.chromosomes = new ArrayList<>();

        Set<WVertex> vertices = this.perfOptcpu.getApp().getGraph().getDirectedGraph().vertexSet();
        for (WVertex aVertex : vertices) {
            this.costProfile.put(aVertex, new HashMap<>());
            for (MemoryCpuKey mc : aVertex.getPerf_profileCpu().keySet())
                this.costProfile.get(aVertex).put(mc, perfOptcpu.getApp().GetVertexCostInMemCpu(aVertex, mc.getMemory(),mc.getCpu()));
        }
    }

    public long GASearch(double[] constraintList, String OPTType, int iter) {
        GA.Chromosome.setOPTType(OPTType);
        ArrayList<GA_Result> results = new ArrayList<>();
        long GAStartTime = System.currentTimeMillis();
        GA.Chromosome lastBestChromosome = null;
        for (int numOfConstraint = 0; numOfConstraint < constraintList.length; numOfConstraint++) {
            WVertex[] vertices = this.perfOptcpu.getApp().getGraph().getDirectedGraph().vertexSet().toArray(new WVertex[0]);
            this.chromosomes.clear();
            for (int i = 0; i < this.N_GENES; i++)
                this.chromosomes.add(new GA.Chromosome(i + 1, vertices));
            if (lastBestChromosome != null)
                this.chromosomes.set(new Random().nextInt(this.chromosomes.size()), lastBestChromosome);

            double constraint = constraintList[numOfConstraint];
            int iteration = 0;
            int bestIndex = 0;
            while (iteration < this.N_GENERATIONS) {
                double[] fitness = new double[this.chromosomes.size()];
                for (int i = 0; i < this.chromosomes.size(); i++) {
                    this.chromosomes.get(i).TranslateDNA();
                    this.chromosomes.get(i).GetFitness(constraint);
                    fitness[i] = this.chromosomes.get(i).getFitness();
                }

                bestIndex = getBestOffspring(fitness, GA.Chromosome.getOPTType(), constraint);

                if (iteration == (this.N_GENERATIONS - 1) && GA.Chromosome.getOPTType().equals("BCPO")) {
                    System.out.printf("GA Repeatable times: %d, Budget_constraint: %f ms,  best fit: %f,  time: %f ms, cost: %f USD\n",iter, constraint, fitness[bestIndex],
                            this.chromosomes.get(bestIndex).getRt(), this.chromosomes.get(bestIndex).getCost());
                } else if (iteration == (this.N_GENERATIONS - 1) && Chromosome.getOPTType().equals("PCCO")) {
                    System.out.printf("GA Repeatable times: %d, Performance_constraint: %f ms,  best fit: %f,  time: %f ms, cost: %f USD\n",iter, constraint, fitness[bestIndex],
                            this.chromosomes.get(bestIndex).getRt(), this.chromosomes.get(bestIndex).getCost());
                }

                this.Evolve(constraint,iteration);
                iteration++;
            }
            lastBestChromosome = this.chromosomes.get(bestIndex);

            System.out.print("The iteration is over! Optimized Memory Configuration: ");
            TreeMap<WVertex, MemoryCpuKey> best_mem_config = this.chromosomes.get(bestIndex).getMemConfig();
            for (WVertex vertex : best_mem_config.keySet()) {
                System.out.print(vertex.toString() + " : " + best_mem_config.get(vertex) + "  ");
            }
            System.out.println();
            results.add(new GA_Result(this.N_GENES, this.CROSS_RATE, this.MUTATIONRATE, this.N_GENERATIONS, this.ETA_M_, constraint,
                    this.chromosomes.get(bestIndex).getRt(), this.chromosomes.get(bestIndex).getCost(), this.chromosomes.get(bestIndex).getFitness(),
                    this.chromosomes.get(bestIndex).getMemConfig()));
        }
        long GAEndTime = System.currentTimeMillis();

        DataStoreTools.GADataStore(results, this, iter);
        return (GAEndTime - GAStartTime) / constraintList.length;
    }

    public void Evolve(double constraint,int iter) {
        for (int i = 0; i < this.chromosomes.size(); i++) {
            this.chromosomes.get(i).CrossOver(this, constraint);
            this.chromosomes.get(i).Mutate(this, constraint);
        }
    }

    public int getBestOffspring(double[] fitness, String OPTType, double constarint) {
        double maxValue = fitness[0];
        int bestIndex = 0;
        for (int i = 0; i < fitness.length; i++) {
            if (fitness[i] > maxValue) {
                maxValue = fitness[i];
                bestIndex = i;
            } else if (fitness[i] == maxValue) {
                if (OPTType.equals("BCPO") && this.chromosomes.get(bestIndex).getCost() < constarint &&
                        this.chromosomes.get(i).getCost() < this.chromosomes.get(bestIndex).getCost())
                    bestIndex = i;
                else if (OPTType.equals("PCCO") && this.chromosomes.get(bestIndex).getRt() < constarint &&
                        this.chromosomes.get(i).getRt() < this.chromosomes.get(bestIndex).getRt())
                    bestIndex = i;
            }
        }
        return bestIndex;
    }
}


