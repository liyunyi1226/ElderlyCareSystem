package config;

import org.springframework.stereotype.Component;
import util.DBUtil;

import javax.sql.DataSource;

@Component
public class DataSourceBridge {

    public DataSourceBridge(DataSource dataSource) {
        DBUtil.setDataSource(dataSource);
    }
}
