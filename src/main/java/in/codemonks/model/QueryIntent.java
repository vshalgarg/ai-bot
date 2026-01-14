package in.codemonks.model;

import java.util.List;

public record QueryIntent(
        String intent,
        List<String> keywords,
        String expectedAnswerType
) {}
