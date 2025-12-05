# Towards Effective Performance- and Cost-Aware Resource Provision for Serverless Workflow

This repository contains the source code and experimental artifacts for the paper:

> **"Towards Effective Performance- and Cost-Aware Resource Provision for Serverless Workflow"**

## Repository Structure

- **`aliyunfc/`**  
  Contains utilities to interact with Alibaba Cloud Function Compute (FC) for collecting function and workflow execution data. The experiments in this directory validate the estimation error of our proposed model.  
  **Entry point**: `aliyunfc/AliyunFcMain.java`

- **`Main/`**  
  Implements our proposed optimization algorithm and baseline methods, along with comparative experiments.  
  **Entry point**: `Main/Main.java`

- **`resources/`**  
  Includes essential data and configuration files:
  - Workflow structures
  - Algorithm parameters
  - Function profiles
  - Workflow execution traces

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven (for dependency management)

### Configuration
To run experiments involving Alibaba Cloud Function Compute:

1. Obtain your Alibaba Cloud **AccessKey ID** and **AccessKey Secret** from the [Alibaba Cloud Console](https://ram.console.aliyun.com/manage/ak).
2. Do NOT hardcode your credentials in the source code. Instead, configure them via environment variables:
   ```bash
   export ALIBABA_CLOUD_ACCESS_KEY_ID=your_access_key_id
   export ALIBABA_CLOUD_ACCESS_KEY_SECRET=your_access_key_secret
