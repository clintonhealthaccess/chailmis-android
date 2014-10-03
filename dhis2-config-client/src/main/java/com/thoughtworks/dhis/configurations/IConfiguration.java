package com.thoughtworks.dhis.configurations;

import java.io.IOException;
import java.util.Map;

public interface IConfiguration {

    public Map<String, Object> generateMetaData() throws IOException;
}
