package com.ifeng.chenjh5.engine;

import com.ifeng.chenjh5.utils.SolrClientFactory;

/**
 * Created by chenjh5 on 2017/3/21.
 */
public class ClassifyBySystem {
    private String feature;

    public ClassifyBySystem (String feature) {
        this.feature = feature;
    }

    public void classify () {
        SolrClientFactory.init();
        SolrClientFactory.properties.getProperty("xiaoMiFeature");
        switch (feature) {

        }
    }
}
