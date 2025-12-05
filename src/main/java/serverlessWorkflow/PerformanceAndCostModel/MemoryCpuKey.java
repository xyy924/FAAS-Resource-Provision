package serverlessWorkflow.PerformanceAndCostModel;

import java.util.Objects;

public class MemoryCpuKey implements Comparable<MemoryCpuKey> {
    private final int memory; // 内存地址
    private final float cpu;    // CPU 核心编号

    public MemoryCpuKey(int memory, float cpu) {
        this.memory = memory;
        this.cpu = cpu;
    }

    public int getMemory() {
        return memory;
    }

    public float getCpu() {
        return cpu;
    }

    @Override
    public int compareTo(MemoryCpuKey other) {
        // 先按内存地址排序
        if (this.memory != other.memory) {
            return Integer.compare(this.memory, other.memory);
        }
        // 再按 CPU 核心编号排序
        return Float.compare(this.cpu, other.cpu);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryCpuKey that = (MemoryCpuKey) o;
        return memory == that.memory && cpu == that.cpu;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memory, cpu);
    }

    @Override
    public String toString() {
        return "{" +
                "memory=" + memory +
                ", cpu=" + cpu +
                '}';
    }
}