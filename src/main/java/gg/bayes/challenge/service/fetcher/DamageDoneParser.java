package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;

import java.util.Optional;
import java.util.regex.Matcher;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.DAMAGE_DONE;

public class DamageDoneParser implements LogEntryEntityParser {
    private final Matcher matcher;

    public DamageDoneParser(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Optional<CombatLogEntryEntity> parse() {
        CombatLogEntryEntity result = new CombatLogEntryEntity();
        result.setTimestamp(LogEntryEntityParser.parseTimeStamp(matcher.group(1)));
        String actor = matcher.group(2);
        String target = matcher.group(3);
        if (LogEntryEntityParser.isHero(actor) && LogEntryEntityParser.isHero(target)) {
            result.setType(DAMAGE_DONE);
            result.setActor(LogEntryEntityParser.parseHeroName(actor));
            result.setTarget(LogEntryEntityParser.parseHeroName(target));
            result.setAbility(matcher.group(4));
            result.setDamage(Integer.parseInt(matcher.group(5)));
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
