package in.codemonks.service;

import in.codemonks.model.QueryIntent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryExpansionService {

    public List<String> expand(QueryIntent intent) {

        if (intent.keywords() == null || intent.keywords().isEmpty()) {
            return List.of(intent.intent());
        }

        // keep it conservative
        return intent.keywords().stream()
                .distinct()
                .limit(5)
                .toList();
    }
}