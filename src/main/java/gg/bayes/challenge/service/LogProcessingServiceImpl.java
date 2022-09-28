package gg.bayes.challenge.service;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.model.MatchEntity;
import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.persistence.repository.MatchRepository;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.*;

@Service
public class LogProcessingServiceImpl implements LogProcessingService {
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\[(.*)].*");
    private static final Pattern ITEM_PURCHASED_PATTERN = Pattern.compile("\\[.*] (.*) buys item (.*)");
    private static final Pattern HERO_KILLED_PATTERN = Pattern.compile("\\[.*] (.*) is killed by (.*)");
    private static final Pattern SPELL_CASTED_PATTERN = Pattern.compile("\\[.*] (.*) casts ability (.*) \\(lvl (\\d)\\) on .*");
    private static final Pattern DAMAGE_DONE_PATTERN = Pattern.compile("\\[.*] (.*) hits (.*) with (.*) for (.*) damage \\(\\d+->\\d+\\)");
    private static final String HERO_OBJECT_PREFIX = "npc_dota_hero_";
    private static final String ITEM_OBJECT_PREFIX = "item_";

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

    static Optional<CombatLogEntryEntity> parseCombatLogEntry(String entry) {
        CombatLogEntryEntity result = new CombatLogEntryEntity();
        result.setTimestamp(parseTimeStamp(entry));

        Matcher matcher;
        if ((matcher = ITEM_PURCHASED_PATTERN.matcher(entry)).matches()) {
            result.setActor(parseHeroName(matcher.group(1)));
            result.setItem(parseItemName(matcher.group(2)));
            result.setType(ITEM_PURCHASED);
            return Optional.of(result);
        } else if ((matcher = HERO_KILLED_PATTERN.matcher(entry)).matches()) {
            String target = matcher.group(1);
            String actor = matcher.group(2);
            if (isHero(actor) && isHero(target)) {
                result.setType(HERO_KILLED);
                result.setTarget(parseHeroName(target));
                result.setActor(parseHeroName(actor));
                return Optional.of(result);
            }
        } else if ((matcher = SPELL_CASTED_PATTERN.matcher(entry)).matches()) {
            String actor = matcher.group(1);
            if (isHero(actor)) {
                result.setType(SPELL_CAST);
                result.setActor(parseHeroName(actor));
                result.setAbility(matcher.group(2));
                result.setAbilityLevel(Integer.parseInt(matcher.group(3)));
                return Optional.of(result);
            }
        } else if ((matcher = DAMAGE_DONE_PATTERN.matcher(entry)).matches()) {
            String actor = matcher.group(1);
            String target = matcher.group(2);
            if (isHero(actor) && isHero(target)) {
                result.setType(DAMAGE_DONE);
                result.setActor(parseHeroName(actor));
                result.setTarget(parseHeroName(target));
                result.setAbility(matcher.group(3));
                result.setDamage(Integer.parseInt(matcher.group(4)));
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    static boolean isHero(String objectName) {
        return objectName.startsWith(HERO_OBJECT_PREFIX);
    }

    static String parseHeroName(String objectName) {
        return objectName.substring(HERO_OBJECT_PREFIX.length());
    }

    static String parseItemName(String objectName) {
        return objectName.substring(ITEM_OBJECT_PREFIX.length());
    }

    static long parseTimeStamp(String record) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(record);

        if (matcher.matches()) {
            PeriodFormatter periodFormatterBuilder = new PeriodFormatterBuilder()
                    .appendHours().appendSuffix(":")
                    .appendMinutes().appendSuffix(":")
                    .appendSeconds().appendSuffix(".")
                    .appendMillis()
                    .toFormatter();
            Period period = periodFormatterBuilder.parsePeriod(matcher.group(1));

            return period.toDurationFrom(new DateTime(0)).toDuration().getMillis();
        }
        throw new IllegalArgumentException("Illegal record format");
    }
}
