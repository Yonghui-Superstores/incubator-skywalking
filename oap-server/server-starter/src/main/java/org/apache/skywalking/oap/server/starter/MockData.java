package org.apache.skywalking.oap.server.starter;

import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class MockData {

    static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    public static void main(String[] args) throws KeyManagementException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        DateTime time = new DateTime(2020, 6, 2, 0, 0, 0);

        ElasticSearchClient client = new ElasticSearchClient("localhost:9200", "http", null, null, null, null, null);
        client.connect();

        BulkProcessor processor = client.createBulkProcessor(2000, 5, 5);

        for (int i = 5; i < 200; i++) {
            bulk(processor, i, time);
        }
    }


    private static void bulk(BulkProcessor processor, int entityId, DateTime time) {
        DateTime newTime = time;

        while (newTime.dayOfMonth().get() != 3) {
            newTime = newTime.plusSeconds(1);
            long timeBucket = Long.parseLong(newTime.toString(formatter));

            Map source = new HashMap();
            source.put("num_of_steps", 50);
            source.put("time_bucket", timeBucket);
            source.put("step", 50);
            source.put("entity_id", String.valueOf(entityId));
            source.put("detail_group", "0,17|1,19|2,13|3,11|4,17|5,12|6,15|7,19|8,13|9,15|10,22|11,19|12,12|13,13|14,12|15,16|16,12|17,12|18,13|19,14|20,13|21,14|22,11|23,11|24,13|25,20|26,19|27,19|28,19|29,6|30,16|31,16|32,13|33,17|34,17|35,11|36,15|37,21|38,15|39,19|40,15|41,14|42,24|43,14|44,18|45,17|46,18|47,13|48,14|49,17|50,13|51,15|52,14|53,15|54,20|55,23|56,15|57,22|58,17|59,13|60,14|61,14|62,10|63,13|64,9|65,12|66,13|67,10|68,18|69,12|70,20|71,13|72,16|73,17|74,13|75,11|76,23|77,10|78,12|79,8|80,10|81,18|82,13|83,15|84,19|85,12|86,15|87,17|88,16|89,17|90,19|91,14|92,21|93,13|94,16|95,12|96,17|97,12|98,17|99,14|100,19|101,19|102,11|103,13|104,12|105,13|106,20|107,20|108,13|109,18|110,14|111,17|112,10|113,17|114,18|115,19|116,15|117,11|118,16|119,10|120,21|121,14|122,15|123,17|124,17|125,24|126,11|127,15|128,12|129,20|130,8|131,13|132,22|133,10|134,14|135,18|136,22|137,19|138,16|139,15|140,23|141,13|142,14|143,11|144,20|145,11|146,12|147,12|148,18|149,17|150,15|151,10|152,11|153,14|154,20|155,7|156,22|157,14|158,14|159,16|160,18|161,15|162,14|163,9|164,16|165,20|166,13|167,5|168,11|169,17|170,9|171,10|172,9|173,14|174,18|175,18|176,9|177,17|178,11|179,18|180,23|181,12|182,11|183,16|184,16|185,16|186,14|187,10|188,17|189,12|190,14|191,21|192,15|193,10|194,14|195,14|196,15|197,11|198,23|199,16");

            IndexRequest request = new IndexRequest("service_dot_heatmap-20200602", "type", timeBucket + Const.ID_SPLIT + entityId);
            request.source(source);
            processor.add(request);
        }
    }
}
