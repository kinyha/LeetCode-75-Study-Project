package exercise.justCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CodeTest {

    @Test
    @DisplayName("reverseWords reverses word order and normalizes spaces")
    void reverseWordsReversesOrderAndNormalizesSpaces() {
        assertThat(Code.reverseWords("  java   interview prep  "))
                .isEqualTo("prep interview java");
    }

    @Test
    @DisplayName("firstNonRepeatingChar returns first unique character")
    void firstNonRepeatingCharReturnsFirstUniqueCharacter() {
        assertThat(Code.firstNonRepeatingChar("swiss"))
                .isEqualTo(Optional.of('w'));
    }

    @Test
    @DisplayName("firstNonRepeatingChar returns empty when every character repeats")
    void firstNonRepeatingCharReturnsEmptyWhenEveryCharacterRepeats() {
        assertThat(Code.firstNonRepeatingChar("aabbcc"))
                .isEmpty();
    }

    @Test
    @DisplayName("twoSum returns indexes of two values that reach target")
    void twoSumReturnsIndexesOfTwoValuesThatReachTarget() {
        assertThat(Code.twoSum(new int[]{2, 7, 11, 15}, 9))
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("twoSum works when matching values are duplicates")
    void twoSumWorksWithDuplicateValues() {
        assertThat(Code.twoSum(new int[]{3, 3}, 6))
                .containsExactly(0, 1);
    }

    @Test
    @DisplayName("groupWordsByFirstLetter groups non-empty words by lowercase first letter")
    void groupWordsByFirstLetterGroupsByLowercaseFirstLetter() {
        var grouped = Code.groupWordsByFirstLetter(List.of("Java", "join", "Map", "mock"));

        assertThat(grouped).containsOnlyKeys('j', 'm');
        assertThat(grouped.get('j')).containsExactly("Java", "join");
        assertThat(grouped.get('m')).containsExactly("Map", "mock");
    }

    @Test
    @DisplayName("topKFrequent returns most common words with alphabetical tie-breaker")
    void topKFrequentReturnsMostCommonWordsWithAlphabeticalTieBreaker() {
        assertThat(Code.topKFrequent(List.of("java", "map", "java", "list", "map", "java"), 2))
                .containsExactly("java", "map");
    }
}
