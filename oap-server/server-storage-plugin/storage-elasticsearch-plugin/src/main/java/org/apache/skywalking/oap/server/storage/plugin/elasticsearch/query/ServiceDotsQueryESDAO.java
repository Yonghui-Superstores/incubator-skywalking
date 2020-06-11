package org.apache.skywalking.oap.server.storage.plugin.elasticsearch.query;

import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceFailureDotHeatMapMetrics;
import org.apache.skywalking.oap.server.core.analysis.manual.service.ServiceSuccessDotHeatMapMetrics;
import org.apache.skywalking.oap.server.core.analysis.metrics.ThermodynamicMetrics;
import org.apache.skywalking.oap.server.core.storage.query.IServiceDotsQueryDAO;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch.base.EsDAO;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDotsQueryESDAO extends EsDAO implements IServiceDotsQueryDAO {

    public ServiceDotsQueryESDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override
    public Map<String, ? extends ThermodynamicMetrics> getSuccessDots(String indexName, List<String> ids, long startTimeStamp, long endTimeStamp) throws IOException {
        Map<String, ServiceSuccessDotHeatMapMetrics> dots = new HashMap<>();

        SearchResponse response = getClient().ids(indexName, ids.toArray(new String[0]));
        for (SearchHit hit : response.getHits().getHits()) {
            ServiceSuccessDotHeatMapMetrics.Builder builder = new ServiceSuccessDotHeatMapMetrics.Builder();
            ServiceSuccessDotHeatMapMetrics dotHeatMapMetrics = builder.map2Data(hit.getSourceAsMap());
            dots.put(hit.getId(), dotHeatMapMetrics);
        }

        return dots;
    }

    @Override
    public Map<String, ? extends ThermodynamicMetrics> getFailureDots(String indexName, List<String> ids, long startTimeStamp, long endTimeStamp) throws IOException {
        Map<String, ServiceFailureDotHeatMapMetrics> dots = new HashMap<>();

        SearchResponse response = getClient().ids(indexName, ids.toArray(new String[0]));
        for (SearchHit hit : response.getHits().getHits()) {
            ServiceFailureDotHeatMapMetrics.Builder builder = new ServiceFailureDotHeatMapMetrics.Builder();
            ServiceFailureDotHeatMapMetrics dotHeatMapMetrics = builder.map2Data(hit.getSourceAsMap());
            dots.put(hit.getId(), dotHeatMapMetrics);
        }

        return dots;
    }
}
