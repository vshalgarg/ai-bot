package in.codemonks.properties;

import in.codemonks.context.TenantContext;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "vector.db.collection.name")
public class TenantCollectionProperties {

    private Map<String, String> values = new HashMap<>();

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public String getCollectionNameForCurrentTenant() {
        return values.get(TenantContext.getTenantId());
    }

}
