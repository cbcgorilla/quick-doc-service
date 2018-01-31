package cn.mxleader.quickdoc.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("quickdoc.tensorflow")
public class TensorFlowProperties {

    /**
     * Tensorflow训练模型存储路径
     */
    private String modelDir;

    public String getModelDir() {
        return modelDir;
    }

    public void setModelDir(String modelDir) {
        this.modelDir = modelDir;
    }
}
