package com.rwm.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 降水信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Precipitation {
    /**
     * 降水概率
     */
    private PrecipitationProbability probability;
    
    /**
     * 降水量
     */
    private PrecipitationQuantity qpf;
    
    public Precipitation() {}
    
    public PrecipitationProbability getProbability() {
        return probability;
    }
    
    public void setProbability(PrecipitationProbability probability) {
        this.probability = probability;
    }
    
    public PrecipitationQuantity getQpf() {
        return qpf;
    }
    
    public void setQpf(PrecipitationQuantity qpf) {
        this.qpf = qpf;
    }
    
    @Override
    public String toString() {
        return "Precipitation{" +
                "probability=" + probability +
                ", qpf=" + qpf +
                '}';
    }
}
