package gg.bayes.challenge.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 * Integration test template to get you started. Add tests and make modifications as you see fit.
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class MatchControllerIntegrationTest {

    private static final String COMBATLOG_FILE_1 = "/data/combatlog_1.log.txt";
    private static final String COMBATLOG_FILE_2 = "/data/combatlog_2.log.txt";

    @Autowired
    private MockMvc mvc;

    private Map<String, Long> matchIds;

    @BeforeAll
    void setup() throws Exception {
        // Populate the database with all events from both sample data files and store the returned
        // match IDS.
        matchIds = Map.of(
                COMBATLOG_FILE_1, ingestMatch(COMBATLOG_FILE_1),
                COMBATLOG_FILE_2, ingestMatch(COMBATLOG_FILE_2));
    }

    @Test
    void testHeroKills() throws Exception {
        var returnedBody = mvc.perform(get("/api/match/" + matchIds.get(COMBATLOG_FILE_1)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        assertThat(returnedBody).isEqualTo("[{\"hero\":\"snapfire\",\"kills\":2}" +
                ",{\"hero\":\"rubick\",\"kills\":4}," +
                "{\"hero\":\"mars\",\"kills\":6},{\"hero\":\"dragon_knight\",\"kills\":3}," +
                "{\"hero\":\"bane\",\"kills\":2},{\"hero\":\"puck\",\"kills\":7},{\"hero\":\"death_prophet\",\"kills\":9}" +
                ",{\"hero\":\"abyssal_underlord\",\"kills\":6},{\"hero\":\"pangolier\",\"kills\":5}" +
                ",{\"hero\":\"bloodseeker\",\"kills\":11}]");
    }

    @Test
    void testHeroDamages() throws Exception {
        var returnedBody = mvc.perform(get(
                String.format("/api/match/%s/%s/damage", matchIds.get(COMBATLOG_FILE_1), "mars")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(returnedBody).isEqualTo("[{\"target\":\"snapfire\",\"damage_instances\":21,\"total_damage\":4279}," +
                "{\"target\":\"dragon_knight\",\"damage_instances\":14,\"total_damage\":2198}," +
                "{\"target\":\"puck\",\"damage_instances\":5,\"total_damage\":1711}," +
                "{\"target\":\"abyssal_underlord\",\"damage_instances\":17,\"total_damage\":3009}," +
                "{\"target\":\"pangolier\",\"damage_instances\":13,\"total_damage\":2295}]");
    }

    @Test
    void testHeroItems() throws Exception {
        var returnedBody = mvc.perform(get(
                        String.format("/api/match/%s/%s/items", matchIds.get(COMBATLOG_FILE_1), "mars")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(returnedBody).isEqualTo("[{\"item\":\"quelling_blade\",\"timestamp\":530292}," +
                "{\"item\":\"tango\",\"timestamp\":531658},{\"item\":\"tango\",\"timestamp\":531825}," +
                "{\"item\":\"flask\",\"timestamp\":532025},{\"item\":\"branches\",\"timestamp\":532558}," +
                "{\"item\":\"branches\",\"timestamp\":532691},{\"item\":\"magic_stick\",\"timestamp\":745606}," +
                "{\"item\":\"recipe_magic_wand\",\"timestamp\":745772},{\"item\":\"boots\",\"timestamp\":745906}," +
                "{\"item\":\"blades_of_attack\",\"timestamp\":749605},{\"item\":\"magic_wand\",\"timestamp\":782130}," +
                "{\"item\":\"chainmail\",\"timestamp\":799826},{\"item\":\"boots\",\"timestamp\":888238}," +
                "{\"item\":\"phase_boots\",\"timestamp\":917997},{\"item\":\"ring_of_regen\",\"timestamp\":984347}," +
                "{\"item\":\"gauntlets\",\"timestamp\":984514},{\"item\":\"gauntlets\",\"timestamp\":984681}," +
                "{\"item\":\"soul_ring\",\"timestamp\":986347},{\"item\":\"recipe_soul_ring\",\"timestamp\":986347}," +
                "{\"item\":\"mithril_hammer\",\"timestamp\":1245150},{\"item\":\"blight_stone\",\"timestamp\":1245817}," +
                "{\"item\":\"mithril_hammer\",\"timestamp\":1437937},{\"item\":\"desolator\",\"timestamp\":1466630}," +
                "{\"item\":\"blink\",\"timestamp\":1685610},{\"item\":\"ogre_axe\",\"timestamp\":2242283}," +
                "{\"item\":\"mithril_hammer\",\"timestamp\":2242517},{\"item\":\"black_king_bar\",\"timestamp\":2242617}," +
                "{\"item\":\"recipe_black_king_bar\",\"timestamp\":2242617}]");
    }

    @Test
    void testHeroSpells() throws Exception {
        var returnedBody = mvc.perform(get(
                        String.format("/api/match/%s/%s/spells", matchIds.get(COMBATLOG_FILE_1), "mars")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(returnedBody).isEqualTo("[{\"spell\":\"mars_gods_rebuke\",\"casts\":39}," +
                "{\"spell\":\"mars_spear\",\"casts\":30},{\"spell\":\"mars_arena_of_blood\",\"casts\":7}]");
    }

    /**
     * Helper method that ingests a combat log file and returns the match id associated with all parsed events.
     *
     * @param file file path as a classpath resource, e.g.: /data/combatlog_1.log.txt.
     * @return the id of the match associated with the events parsed from the given file
     * @throws Exception if an error happens when reading or ingesting the file
     */
    private Long ingestMatch(String file) throws Exception {
        String fileContent = IOUtils.resourceToString(file, StandardCharsets.UTF_8);

        return Long.parseLong(mvc.perform(post("/api/match")
                                         .contentType(MediaType.TEXT_PLAIN)
                                         .content(fileContent))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString());
    }
}
