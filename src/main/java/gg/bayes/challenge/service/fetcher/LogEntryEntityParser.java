package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import org.joda.time.DateTime;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface LogEntryEntityParser {
    PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .appendHours().appendSuffix(":")
            .appendMinutes().appendSuffix(":")
            .appendSeconds().appendSuffix(".")
            .appendMillis()
            .toFormatter();
    Pattern ITEM_PURCHASED_PATTERN = Pattern.compile("\\[(.*)] (.*) buys item (.*)");
    Pattern HERO_KILLED_PATTERN = Pattern.compile("\\[(.*)] (.*) is killed by (.*)");
    Pattern SPELL_CASTED_PATTERN = Pattern.compile("\\[(.*)] (.*) casts ability (.*) \\(lvl (\\d)\\) on .*");
    Pattern DAMAGE_DONE_PATTERN = Pattern.compile("\\[(.*)] (.*) hits (.*) with (.*) for (.*) damage \\(\\d+->\\d+\\)");
    String HERO_OBJECT_PREFIX = "npc_dota_hero_";

    Optional<CombatLogEntryEntity> parse();

    static LogEntryEntityParser fromRecord(String logRecord) {
        Matcher matcher;
        if ((matcher = ITEM_PURCHASED_PATTERN.matcher(logRecord)).matches()) {
            return new ItemPurchasedParser(matcher);
        } else if ((matcher = HERO_KILLED_PATTERN.matcher(logRecord)).matches()) {
            return new HeroKilledParser(matcher);
        } else if ((matcher = SPELL_CASTED_PATTERN.matcher(logRecord)).matches()) {
            return new SpellCastedParser(matcher);
        } else if ((matcher = DAMAGE_DONE_PATTERN.matcher(logRecord)).matches()) {
            return new DamageDoneParser(matcher);
        }
        throw new IllegalArgumentException("Invalid log record format");
    }

    static boolean isHero(String objectName) {
        return objectName.startsWith(HERO_OBJECT_PREFIX);
    }

    static String parseHeroName(String objectName) {
        return objectName.substring(HERO_OBJECT_PREFIX.length());
    }

    static long parseTimeStamp(String rawTimestamp) {
        return PERIOD_FORMATTER.parsePeriod(rawTimestamp)
                .toDurationFrom(new DateTime(0))
                .toDuration()
                .getMillis();
    }
}
