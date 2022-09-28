package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;

import java.util.Optional;
import java.util.regex.Matcher;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.SPELL_CAST;

public class SpellCastedParser implements LogEntryEntityParser {
    private final Matcher matcher;

    public SpellCastedParser(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Optional<CombatLogEntryEntity> parse() {
        CombatLogEntryEntity result = new CombatLogEntryEntity();
        result.setTimestamp(LogEntryEntityParser.parseTimeStamp(matcher.group(1)));

        String actor = matcher.group(2);
        if (LogEntryEntityParser.isHero(actor)) {
            result.setType(SPELL_CAST);
            result.setActor(LogEntryEntityParser.parseHeroName(actor));
            result.setAbility(matcher.group(3));
            result.setAbilityLevel(Integer.parseInt(matcher.group(4)));
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
