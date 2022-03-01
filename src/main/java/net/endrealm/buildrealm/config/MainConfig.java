package net.endrealm.buildrealm.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

public class MainConfig {
    private final Configuration configuration;

    @Getter
    private List<Integer> maxDraftSteps;
    @Getter
    private BackendType backend;
    @Getter
    private MongoSettings mongoSettings;

    public MainConfig(Configuration configuration) {
        this.configuration = configuration;
        reload();
    }

    public void reload() {
        var config = configuration.getConfiguration();
        this.maxDraftSteps = config.getIntegerList("max_draft_perms_steps");
        this.maxDraftSteps.sort((o1, o2) -> o2 - o1);
        this.backend = BackendType.valueOf(config.getString("backend").toUpperCase());
        if(backend == BackendType.MONGO) {
            mongoSettings = new MongoSettings(
                    config.getString("backends.mongo.host"),
                    config.getString("backends.mongo.database"),
                    Optional.ofNullable(config.getString("backends.mongo.host"))
            );
        }
    }

    public enum BackendType {
        FILE, MONGO
    }

    public record MongoSettings(String host, String database,
                                Optional<String> password) {
    }
}
