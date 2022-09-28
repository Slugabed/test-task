package gg.bayes.challenge.service.fetcher;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;

import java.util.Optional;
import java.util.regex.Matcher;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.ITEM_PURCHASED;

public class ItemPurchasedParser implements LogEntryEntityParser {
    private static final String ITEM_OBJECT_PREFIX = "item_";
    private final Matcher matcher;

    public ItemPurchasedParser(Matcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Optional<CombatLogEntryEntity> parse() {
        CombatLogEntryEntity result = new CombatLogEntryEntity();
        result.setTimestamp(LogEntryEntityParser.parseTimeStamp(matcher.group(1)));
        result.setActor(LogEntryEntityParser.parseHeroName(matcher.group(2)));
        result.setItem(parseItemName(matcher.group(3)));
        result.setType(ITEM_PURCHASED);
        return Optional.of(result);
    }

    static String parseItemName(String objectName) {
        return objectName.substring(ITEM_OBJECT_PREFIX.length());
    }
}
