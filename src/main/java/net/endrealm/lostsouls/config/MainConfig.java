package net.endrealm.lostsouls.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MainConfig {
    private final Configuration configuration;

    @Getter
    private List<Integer> maxDraftSteps;

    public MainConfig(Configuration configuration) {
        this.configuration = configuration;
        reload();
    }

    public void reload() {
        this.maxDraftSteps = configuration.getConfiguration().getIntegerList("max_draft_perms_steps");
        this.maxDraftSteps.sort((o1, o2) -> o2-o1);
    }
}
