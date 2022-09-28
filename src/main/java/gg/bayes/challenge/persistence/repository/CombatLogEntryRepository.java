package gg.bayes.challenge.persistence.repository;

import gg.bayes.challenge.persistence.model.CombatLogEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombatLogEntryRepository extends JpaRepository<CombatLogEntryEntity, Long> {
    // TODO: add the necessary methods for your solution
    List<CombatLogEntryEntity> findAllByTypeAndMatchId(CombatLogEntryEntity.Type type, long matchId);
    List<CombatLogEntryEntity> findAllByTypeAndMatchIdAndActor(CombatLogEntryEntity.Type type,
                                                               long matchId,
                                                               String actor);
}
