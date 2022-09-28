package gg.bayes.challenge.service;

import org.junit.jupiter.api.Test;

import static gg.bayes.challenge.persistence.model.CombatLogEntryEntity.Type.*;
import static org.junit.jupiter.api.Assertions.*;

public class LogProcessingServiceImplTest {

    @Test
    public void testParseTimeStamp() {
        long timestamp1 = LogProcessingServiceImpl.parseTimeStamp("[00:24:57.256]");
        assertEquals(timestamp1, (24 * 60 + 57) * 1000 + 256);

        long timestamp2 = LogProcessingServiceImpl.parseTimeStamp("[12:24:57.256]");
        assertEquals(timestamp2, ((12 * 60 + 24) * 60 + 57) * 1000 + 256);
    }

    @Test
    public void testParseDamageDoneByHero() {
        String record = "[00:24:56.222] npc_dota_hero_monkey_king hits npc_dota_hero_lycan with " +
                "monkey_king_boundless_strike for 305 damage (756->451)";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(DAMAGE_DONE, result.getType());
        assertEquals(24 * 60 * 1000 + 56 * 1000 + 222, result.getTimestamp());
        assertEquals("monkey_king", result.getActor());
        assertEquals("lycan", result.getTarget());
        assertEquals(305, result.getDamage());
        assertEquals("monkey_king_boundless_strike", result.getAbility());
    }

    @Test
    public void testParseDamageDoneByNotHeroIgnored() {
        String record = "[00:24:56.222] npc_dota_mob hits npc_dota_hero_lycan with " +
                "monkey_king_boundless_strike for 305 damage (756->451)";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    public void testParseDamageDone() {
        String record = "[00:24:56.222] npc_dota_hero_monkey_king hits npc_dota_hero_lycan with " +
                "monkey_king_boundless_strike for 305 damage (756->451)";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(DAMAGE_DONE, result.getType());
        assertEquals(24 * 60 * 1000 + 56 * 1000 + 222, result.getTimestamp());
        assertEquals("monkey_king", result.getActor());
        assertEquals("lycan", result.getTarget());
        assertEquals(305, result.getDamage());
        assertEquals("monkey_king_boundless_strike", result.getAbility());
    }

    @Test
    public void testParseItemBought() {
        String record = "[00:25:05.687] npc_dota_hero_keeper_of_the_light buys item item_wind_lace";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(ITEM_PURCHASED, result.getType());
        assertEquals(25 * 60 * 1000 + 5 * 1000 + 687, result.getTimestamp());
        assertEquals("keeper_of_the_light", result.getActor());
        assertEquals("wind_lace", result.getItem());
    }

    @Test
    public void testHeroKilledByHero() {
        String record = "[00:16:53.140] npc_dota_hero_keeper_of_the_light is killed by npc_dota_hero_grimstroke";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(HERO_KILLED, result.getType());
        assertEquals(16 * 60 * 1000 + 53 * 1000 + 140, result.getTimestamp());
        assertEquals("grimstroke", result.getActor());
        assertEquals("keeper_of_the_light", result.getTarget());
    }

    @Test
    public void testHeroKilledByNotHeroIgnored() {
        String record = "[00:16:48.108] npc_dota_hero_earthshaker is killed by npc_dota_goodguys_tower1_bot";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    public void testNotHeroKilledByHeroIgnored() {
        String record = "[00:19:31.069] npc_dota_neutral_forest_troll_berserker is killed by " +
                "npc_dota_hero_keeper_of_the_light";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    public void testNotHeroKilledByNotHeroIgnored() {
        String record = "[00:19:26.170] npc_dota_neutral_kobold_taskmaster is killed " +
                "by npc_dota_creep_goodguys_melee";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    public void testSpellCastedHeroToHero() {
        String record = "[00:37:52.753] npc_dota_hero_keeper_of_the_light casts ability " +
                "keeper_of_the_light_chakra_magic (lvl 4) on npc_dota_hero_keeper_of_the_light";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(SPELL_CAST, result.getType());
        assertEquals(37 * 60 * 1000 + 52 * 1000 + 753, result.getTimestamp());
        assertEquals("keeper_of_the_light", result.getActor());
        assertEquals("keeper_of_the_light_chakra_magic", result.getAbility());
        assertEquals(4, result.getAbilityLevel());
    }

    @Test
    public void testSpellCastedHeroToNotHero() {
        String record = "[00:19:42.266] npc_dota_hero_ember_spirit casts ability ember_spirit_sleight_of_fist " +
                "(lvl 1) on dota_unknown";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isPresent());
        var result = resultOpt.get();
        assertEquals(SPELL_CAST, result.getType());
        assertEquals(19 * 60 * 1000 + 42 * 1000 + 266, result.getTimestamp());
        assertEquals("ember_spirit", result.getActor());
        assertEquals("ember_spirit_sleight_of_fist", result.getAbility());
        assertEquals(1, result.getAbilityLevel());
    }

    @Test
    public void testSpellCastedNotHeroToNotHeroIgnored() {
        String record = "[00:24:55.089] npc_dota_neutral_mud_golem_split casts ability mud_golem_hurl_boulder (lvl 1) " +
                "on npc_dota_hero_monkey_king";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }

    @Test
    public void testSpellCastedNotHeroToHeroIgnored() {
        String record = "[00:41:55.681] npc_dota_necronomicon_archer_3 casts ability necronomicon_archer_purge (lvl 3) " +
                "on npc_dota_hero_rubick";
        var resultOpt = LogProcessingServiceImpl.parseCombatLogEntry(record);
        assertTrue(resultOpt.isEmpty());
    }
}
