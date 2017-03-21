package com.ifeng.chenjh5.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.common.cloud.*;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * This class is difined by Singleton Partten, but it is no thread-safe.
 * Created by chenjh5 on 2017/3/21.
 */
public class SolrClientFactory {
    private static Logger log = LoggerFactory.getLogger(SolrClientFactory.class);
    public static Properties properties;
    private static CloudSolrClient cloudSolrClient;

    private static List<String> gruopedUrls;

    private static Map<String,String[]> shardToCoreUrls=new HashMap<>();

    public static void init(){
        //get the config profile by reflection
        URL fileURL = SolrClientFactory.class.getClassLoader().getResource("config.properties");
        File config  = new File(fileURL.getPath());
        //JDK1.7
        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(config), "UTF-8")) {

            properties = new Properties();
            properties.load(inputStreamReader);
            String zkhost = properties.getProperty("zkhost");
            if (StringUtils.isNotBlank(zkhost)) {
                ModifiableSolrParams params = new ModifiableSolrParams();
                params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, properties.getProperty(HttpClientUtil.PROP_MAX_CONNECTIONS));
                params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, properties.getProperty(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST));
                params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, properties.getProperty(HttpClientUtil.PROP_CONNECTION_TIMEOUT));
                params.set(HttpClientUtil.PROP_SO_TIMEOUT, properties.getProperty(HttpClientUtil.PROP_SO_TIMEOUT));

                HttpClient myClient = HttpClientUtil.createClient(params);


                cloudSolrClient = new CloudSolrClient(zkhost, myClient);
                cloudSolrClient.setDefaultCollection(properties.getProperty("colName", "userprofile"));
                cloudSolrClient.setIdField(properties.getProperty("id", "uid"));
                cloudSolrClient.setZkConnectTimeout(Integer.parseInt(properties.getProperty("ZkClientTimeout", "60000")));


                String colName = properties.getProperty("colName", "userprofile");
                ZkStateReader zk = new ZkStateReader(zkhost, Integer.parseInt(properties.getProperty("ZkClientTimeout", "60000")), 3000);
                zk.createClusterStateWatchersAndUpdate();
                ClusterState clusterState = zk.getClusterState();
                DocCollection col = clusterState.getCollection(colName);
                Set<String> urlList = new HashSet<>();
                for (Slice slice : col.getSlices()) {
                    String[] coreUrls=shardToCoreUrls.get(slice.getName());
                    if (coreUrls ==null){
                        coreUrls =new String[slice.getReplicasMap().values().size()];
                    }
                    int index=0;
                    for (ZkNodeProps nodeProps : slice.getReplicasMap().values()) {
                        coreUrls[index++]= ZkCoreNodeProps.getCoreUrl(nodeProps);

                        String baseUrl = ZkCoreNodeProps.getCoreUrl(nodeProps.getStr(ZkStateReader.BASE_URL_PROP), colName);
                        urlList.add(baseUrl);
                    }
                    shardToCoreUrls.put(slice.getName(),coreUrls);
                }

                int index = 0;
                String[] colUrls = new String[urlList.size()];
                for (String url : urlList) {
                    colUrls[index++] = url;
                }
                gruopedUrls=new ArrayList<>();
                for (int i=0;i<colUrls.length;i++){
                    for (int j=i+1;j<colUrls.length;j++){
                        gruopedUrls.add(colUrls[i]+","+colUrls[j]);
                    }
                }
                zk.close();
            } else {
                String solrUrl = properties.getProperty("solrUrl");
                if (StringUtils.isNotBlank(solrUrl)){
                    gruopedUrls.add(solrUrl);
                }
            }

        } catch(Exception e) {
            log.error("SolrClientFactory initiation failed.", e);
        }

        log.info("SolrClientFactory initiation done!");
    }


    public static CloudSolrClient getCloudSlorClient() {
        if (cloudSolrClient == null) {
            init();
        }
        return cloudSolrClient;
    }
}



