package org.commonutils.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CollectionSupportTest {
  @Test
  void isEmptyAndIsNotEmptyReflectCollectionContents() {
    assertTrue(CollectionSupport.isEmpty(null));
    assertTrue(CollectionSupport.isEmpty(List.of()));
    assertFalse(CollectionSupport.isEmpty(List.of("a")));

    assertFalse(CollectionSupport.isNotEmpty(null));
    assertFalse(CollectionSupport.isNotEmpty(List.of()));
    assertTrue(CollectionSupport.isNotEmpty(List.of("a")));
  }

  @Test
  void sizeReturnsZeroForNullOrEmptyAndCountOtherwise() {
    assertEquals(0, CollectionSupport.size(null));
    assertEquals(0, CollectionSupport.size(List.of()));
    assertEquals(3, CollectionSupport.size(List.of("a", "b", "c")));
  }

  @Test
  void getFirstReturnsNullForNullOrEmptyAndFirstElementOtherwise() {
    assertNull(CollectionSupport.getFirst(null));
    assertNull(CollectionSupport.getFirst(List.of()));

    assertEquals("a", CollectionSupport.getFirst(List.of("a")));
    assertEquals("a", CollectionSupport.getFirst(List.of("a", "b")));

    final HashSet<String> set = new HashSet<>(List.of("z", "y"));
    assertTrue(set.contains(CollectionSupport.getFirst(set)));
  }

  @Test
  void getFirstUsesIteratorWhenNotSequencedCollection() {
    final PriorityQueue<String> heap = new PriorityQueue<>(List.of("only"));
    assertEquals("only", CollectionSupport.getFirst(heap));
  }

  @Test
  void getLastReturnsNullForNullOrEmptyAndLastElementOtherwiseForSequencedCollections() {
    assertNull(CollectionSupport.getLast(null));
    assertNull(CollectionSupport.getLast(new ArrayDeque<String>()));

    assertEquals("a", CollectionSupport.getLast(List.of("a")));
    assertEquals("c", CollectionSupport.getLast(List.of("a", "b", "c")));

    final LinkedHashSet<String> ordered = new LinkedHashSet<>(List.of("x", "y", "z"));
    assertEquals("z", CollectionSupport.getLast(ordered));

    final ArrayDeque<String> deque = new ArrayDeque<>(List.of("x", "y", "z"));
    assertEquals("z", CollectionSupport.getLast(deque));
  }

  @Test
  void containsAnyReturnsTrueWhenAtLeastOneCandidateExists() {
    assertFalse(CollectionSupport.containsAny(null, List.of("a")));
    assertFalse(CollectionSupport.containsAny(List.of("a"), null));
    assertFalse(CollectionSupport.containsAny(List.of(), List.of("a")));
    assertFalse(CollectionSupport.containsAny(List.of("a", "b"), List.of("x", "y")));
    assertTrue(CollectionSupport.containsAny(List.of("a", "b"), List.of("x", "b")));
  }

  @Test
  void containsAllReturnsTrueOnlyWhenAllCandidatesExist() {
    assertFalse(CollectionSupport.containsAll(null, List.of("a")));
    assertFalse(CollectionSupport.containsAll(List.of("a"), null));
    assertTrue(CollectionSupport.containsAll(Set.of("a", "b", "c"), List.of()));
    assertTrue(CollectionSupport.containsAll(Set.of("a", "b", "c"), List.of("a", "c")));
    assertFalse(CollectionSupport.containsAll(Set.of("a", "b"), List.of("a", "c")));
  }

  @Test
  void sizeThrowsWhenCollectionReportsNegativeSize() {
    final AbstractCollection<String> broken =
        new AbstractCollection<String>() {
          @Override
          public Iterator<String> iterator() {
            return Collections.emptyIterator();
          }

          @Override
          public int size() {
            return -1;
          }
        };

    final IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> CollectionSupport.size(broken));
    assertTrue(ex.getMessage().contains("collection.size()"));
  }

  @Test
  void containsAnyMatchesNullWhenSourceAllowsNullElements() {
    final List<String> source = Arrays.asList("x", null, "y");
    assertTrue(CollectionSupport.containsAny(source, Collections.singletonList(null)));
    assertFalse(CollectionSupport.containsAny(source, Collections.singletonList("z")));
  }

  @Test
  void containsAllReturnsFalseWhenSourceIsNullEvenIfCandidatesEmpty() {
    assertFalse(CollectionSupport.containsAll(null, List.of()));
  }

  @Test
  void getFirstAndLastDelegateToSequencedCollectionWhenApplicable() {
    final LinkedHashSet<String> ordered = new LinkedHashSet<>(List.of("first", "mid", "last"));
    assertEquals("first", CollectionSupport.getFirst(ordered));
    assertEquals("last", CollectionSupport.getLast(ordered));
  }

  @Test
  void getFirstReturnsNullWhenLeadingElementIsNullInList() {
    final List<String> withNullHead = new ArrayList<>();
    withNullHead.add(null);
    withNullHead.add("b");
    assertNull(CollectionSupport.getFirst(withNullHead));
  }
}
