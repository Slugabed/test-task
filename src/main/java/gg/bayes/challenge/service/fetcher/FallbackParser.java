package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;

import java.util.Optional;

public class FallbackParser implements LogEntryEntityParser{
    @Override
    public Optional<CombatLogEntryEntity> parse() {
        return Optional.empty();
    }
}
