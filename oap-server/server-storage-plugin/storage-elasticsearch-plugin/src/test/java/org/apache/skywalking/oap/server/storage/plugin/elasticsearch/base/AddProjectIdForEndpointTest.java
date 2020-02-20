package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class AddProjectIdForEndpointTest {
    private static TransportClient client = null;
    private static String ip = "10.251.112.10";
    private static String clusterName = "skywalking_test";
    private static String destIndex = "segment";

    public static void main(String[] args) throws IOException {

        Map<Object, Object> integerIntegerMap = buildServiceProject();
        int i = 0;
        for (Object key : integerIntegerMap.keySet()) {
            Object value = integerIntegerMap.get(key);
            updateDestIndexProjectId((Integer) key, (Integer) value, clusterName);
            i++;
            System.out.println("一共有" + integerIntegerMap.size() + "个service需要更新，已经完成" + i + "个");
        }
        System.out.println("全部完成！");
        System.exit(0);
        // updateDestIndexProjectId(121, 2, "skywalking_kf");
    }

    static RestHighLevelClient getRestclient() throws IOException {
        RestClientBuilder builder = RestClient.builder(new HttpHost(ip, 9200));
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
        restHighLevelClient.ping();
        return restHighLevelClient;
    }

    static ElasticsearchClient getTransport(String clusterName) throws UnknownHostException {
        if (client == null) {
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName).build();
            TransportClient transportClient = new PreBuiltTransportClient(settings);
            transportClient.addTransportAddress(new TransportAddress(InetAddress.getByName(ip), 9300));
            client = transportClient;
        }
        return client;
    }

    static Map<Object, Object> buildServiceProject() throws IOException {
        RestHighLevelClient client = getRestclient();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().size(1000);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must().add(QueryBuilders.rangeQuery("project_id").gte(1));
        sourceBuilder.query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest("service_inventory");
        searchRequest.source(sourceBuilder);
        SearchResponse search = client.search(searchRequest);
        Map<Object, Object> map = new HashMap<>();
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits.getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            map.put(sourceAsMap.get("sequence"), sourceAsMap.get("project_id"));
        }
        return map;
    }

    static void updateDestIndexProjectId(Integer serviceId, Integer projectId, String clusterName) throws IOException {
        UpdateByQueryRequestBuilder updateByQuery = UpdateByQueryAction.INSTANCE.newRequestBuilder(getTransport(clusterName));
        updateByQuery.source(destIndex)
                .size(90000000)
                .abortOnVersionConflict(true)
                .filter(QueryBuilders.termQuery("service_id", serviceId))
                .script(new Script(ScriptType.INLINE, "painless", "ctx._source.project_id = " + projectId, Collections.emptyMap()));
        BulkByScrollResponse response = updateByQuery.get();
        long updated = response.getUpdated();
        System.out.println("更新数据条数：" + updated);
    }

}
