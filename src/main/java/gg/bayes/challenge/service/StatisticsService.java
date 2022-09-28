package gg.bayes.challenge.service;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import gg.bayes.challenge.persistence.repository.CombatLogEntryRepository;
import gg.bayes.challenge.rest.model.HeroDamage;
import gg.bayes.challenge.rest.model.HeroItem;
import gg.bayes.challenge.rest.model.HeroKills;
import gg.bayes.challenge.rest.model.HeroSpells;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final CombatLogEntryRepository repository;

    @Autowired
    public StatisticsService(CombatLogEntryRepository repository) {
        this.repository = repository;
    }

    public List<HeroKills> heroKillsList(Long matchId) {
        List<CombatLogEntryEntity> results = repository
                .findAllByTypeAndMatchId(CombatLogEntryEntity.Type.HERO_KILLED, matchId);
        return results.stream()
                .collect(Collectors.groupingBy(CombatLogEntryEntity::getActor, Collectors.counting()))
                .entrySet().stream()
                .map(heroAndKills -> new HeroKills(heroAndKills.getKey(), heroAndKills.getValue().intValue()))
                .collect(Collectors.toList());
    }

    public List<HeroDamage> heroDamage(Long matchId, String heroName) {
        List<CombatLogEntryEntity> results =
                repository.findAllByTypeAndMatchIdAndActor(CombatLogEntryEntity.Type.DAMAGE_DONE, matchId, heroName);
        return results.stream()
                .collect(Collectors.groupingBy(CombatLogEntryEntity::getTarget))
                .entrySet().stream()
                .map(targetAndDamages -> new HeroDamage(
                        targetAndDamages.getKey(),
                        targetAndDamages.getValue().size(),
                        targetAndDamages.getValue().stream().mapToInt(CombatLogEntryEntity::getDamage).sum()))
                .collect(Collectors.toList());
    }

    public List<HeroSpells> heroSpells(Long matchId, String heroName) {
        List<CombatLogEntryEntity> results =
                repository.findAllByTypeAndMatchIdAndActor(CombatLogEntryEntity.Type.SPELL_CAST, matchId, heroName);
        return results.stream()
                .collect(Collectors.groupingBy(CombatLogEntryEntity::getAbility))
                .entrySet().stream()
                .map(spellAndEntities -> new HeroSpells(spellAndEntities.getKey(), spellAndEntities.getValue().size()))
                .collect(Collectors.toList());
    }

    public List<HeroItem> heroItems(Long matchId, String heroName) {
        List<CombatLogEntryEntity> results =
                repository.findAllByTypeAndMatchIdAndActor(CombatLogEntryEntity.Type.ITEM_PURCHASED, matchId, heroName);
        return results.stream()
                .map(entry -> new HeroItem(entry.getItem(), entry.getTimestamp()))
                .collect(Collectors.toList());
    }
}
