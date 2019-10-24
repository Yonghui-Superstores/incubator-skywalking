package org.apache.skywalking.oap.server.storage.plugin.jdbc.h2.dao;

import java.util.List;
import org.apache.skywalking.oap.server.core.register.ProjectInventory;
import org.apache.skywalking.oap.server.core.storage.cache.IProjectInventoryCacheDAO;

/**
 * @author peng-yongsheng
 */
public class H2ProjectInventoryCacheDAO extends H2SQLExecutor implements IProjectInventoryCacheDAO {

    @Override public int getProjectId(String externalProjectId) {
        return 0;
    }

    @Override public ProjectInventory get(int projectId) {
        return null;
    }

    @Override public List<ProjectInventory> loadLastUpdate(long lastUpdateTime) {
        return null;
    }
}
