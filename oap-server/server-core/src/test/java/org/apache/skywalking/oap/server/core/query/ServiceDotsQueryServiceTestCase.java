package org.apache.skywalking.oap.server.core.query;

import org.junit.Test;

public class ServiceDotsQueryServiceTestCase {

    @Test
    public void testIds() {
        ServiceDotsQueryService service = new ServiceDotsQueryService(null);
        String[][] ids = service.idGroups("2", 20200602160101L, 20200602200101L, 1);

        for (int i = 0; i < ids.length; i++) {
            for (String s : ids[i]) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
    }
}
