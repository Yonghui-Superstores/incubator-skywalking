package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base;

import java.io.IOException;
import java.util.*;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.script.*;
import org.elasticsearch.search.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class AddProjectIdForEndpointPRD {
    private static TransportClient client = null;
    private static String ip = "10.251.68.4";
    private static String clusterName = "skywalking_prd";
    private static String destIndex = "segment";

    public static void main(String[] args) throws IOException {

        Map<Object, Object> integerIntegerMap = buildServiceProject();
        int i = 0;
        for (Object key : integerIntegerMap.keySet()) {
            Object value = integerIntegerMap.get(key);
            updateDestIndexProjectId((Integer)key, (Integer)value, clusterName);
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
        UpdateByQueryRequest request = new UpdateByQueryRequest(destIndex);

        request.setSize(90000000)
            .setAbortOnVersionConflict(true)
            .setQuery(QueryBuilders.termQuery("service_id", serviceId))
            .setScript(new Script(ScriptType.INLINE, "painless", "ctx._source.project_id = " + projectId, Collections.emptyMap()));

        BulkByScrollResponse bulkResponse = getRestclient().updateByQuery(request, RequestOptions.DEFAULT);
        long updated = bulkResponse.getUpdated();
        System.out.println("更新数据条数：" + updated);
    }

}
