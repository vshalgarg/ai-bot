package in.codemonks.properties;

import in.codemonks.context.TenantContext;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "vector.db")
public class TenantCollectionProperties {

    private Map<String, String> collection = new HashMap<>();

    public Map<String, String> getCollection() {
        return collection;
    }

    public void setCollection(Map<String, String> collection) {
        this.collection = collection;
    }

    public String getCollectionNameForCurrentTenant() {
        return collection.get(TenantContext.getTenantId());
    }

}
