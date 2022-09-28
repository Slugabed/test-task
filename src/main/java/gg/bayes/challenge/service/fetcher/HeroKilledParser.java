package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;

import java.util.Optional;
import java.util.regex.Matcher;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.HERO_KILLED;

public class HeroKilledParser implements LogEntryEntityParser {
    private final Matcher matcher;

    public HeroKilledParser(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Optional<CombatLogEntryEntity> parse() {
        CombatLogEntryEntity result = new CombatLogEntryEntity();
        result.setTimestamp(LogEntryEntityParser.parseTimeStamp(matcher.group(1)));

        String target = matcher.group(2);
        String actor = matcher.group(3);
        if (LogEntryEntityParser.isHero(actor) && LogEntryEntityParser.isHero(target)) {
            result.setType(HERO_KILLED);
            result.setTarget(LogEntryEntityParser.parseHeroName(target));
            result.setActor(LogEntryEntityParser.parseHeroName(actor));
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
