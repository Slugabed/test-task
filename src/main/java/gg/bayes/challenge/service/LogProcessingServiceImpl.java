package gg.bayes.challenge.service;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.persistence.repository.MatchRepository;
import gg.bayes.challenge.service.fetcher.LogEntryEntityParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LogProcessingServiceImpl implements LogProcessingService {
    private final MatchRepository matchRepository;
    private final CombatLogEntryRepository combatLogEntryRepository;

    @Autowired
    public LogProcessingServiceImpl(MatchRepository matchRepository, CombatLogEntryRepository combatLogEntryRepository) {
        this.matchRepository = matchRepository;
        this.combatLogEntryRepository = combatLogEntryRepository;
    }

    @Override
    public Long processLogCombat(String log) {
        MatchEntity newMatch = matchRepository.save(new MatchEntity());
        for (String record : log.split("\\r?\\n")) {
            parseCombatLogEntry(record).ifPresent(entry -> {
                entry.setMatch(newMatch);
                combatLogEntryRepository.save(entry);
            });
        }
        return newMatch.getId();
    }

    static Optional<CombatLogEntryEntity> parseCombatLogEntry(String logRecord) {
        return LogEntryEntityParser.fromRecord(logRecord).parse();
    }
}
