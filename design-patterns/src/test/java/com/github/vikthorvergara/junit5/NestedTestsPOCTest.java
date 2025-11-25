package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EmptyStackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Nested Tests with Stack")
class NestedTestsPOCTest {

  @Nested
  @DisplayName("When stack is created")
  class WhenNew {

    private NestedTestsPOC<String> stack;

    @BeforeEach
    void createNewStack() {
      stack = new NestedTestsPOC<>(3);
    }

    @Test
    @DisplayName("should be empty")
    void isEmpty() {
      assertTrue(stack.isEmpty());
    }

    @Test
    @DisplayName("should have size of zero")
    void hasZeroSize() {
      assertEquals(0, stack.size());
    }

    @Test
    @DisplayName("should not be full")
    void isNotFull() {
      assertFalse(stack.isFull());
    }

    @Test
    @DisplayName("should throw EmptyStackException when popped")
    void throwsExceptionWhenPopped() {
      assertThrows(EmptyStackException.class, () -> stack.pop());
    }

    @Test
    @DisplayName("should throw EmptyStackException when peeked")
    void throwsExceptionWhenPeeked() {
      assertThrows(EmptyStackException.class, () -> stack.peek());
    }

    @Nested
    @DisplayName("After pushing an element")
    class AfterPushing {

      @BeforeEach
      void pushElement() {
        stack.push("First");
      }

      @Test
      @DisplayName("should not be empty")
      void isNotEmpty() {
        assertFalse(stack.isEmpty());
      }

      @Test
      @DisplayName("should have size of one")
      void hasOneElement() {
        assertEquals(1, stack.size());
      }

      @Test
      @DisplayName("should return the element when popped")
      void returnsElementWhenPopped() {
        assertEquals("First", stack.pop());
      }

      @Test
      @DisplayName("should return the element when peeked")
      void returnsElementWhenPeeked() {
        assertEquals("First", stack.peek());
      }

      @Test
      @DisplayName("should not remove element when peeked")
      void doesNotRemoveElementWhenPeeked() {
        stack.peek();
        assertEquals(1, stack.size());
      }

      @Nested
      @DisplayName("After pushing multiple elements")
      class AfterPushingMultiple {

        @BeforeEach
        void pushMoreElements() {
          stack.push("Second");
          stack.push("Third");
        }

        @Test
        @DisplayName("should have correct size")
        void hasCorrectSize() {
          assertEquals(3, stack.size());
        }

        @Test
        @DisplayName("should be full")
        void isFull() {
          assertTrue(stack.isFull());
        }

        @Test
        @DisplayName("should throw exception when pushing to full stack")
        void throwsExceptionWhenPushingToFull() {
          assertThrows(IllegalStateException.class, () -> stack.push("Fourth"));
        }

        @Test
        @DisplayName("should pop elements in LIFO order")
        void popsInLIFOOrder() {
          assertEquals("Third", stack.pop());
          assertEquals("Second", stack.pop());
          assertEquals("First", stack.pop());
        }

        @Test
        @DisplayName("should peek last element")
        void peeksLastElement() {
          assertEquals("Third", stack.peek());
        }

        @Nested
        @DisplayName("After clearing")
        class AfterClearing {

          @BeforeEach
          void clearStack() {
            stack.clear();
          }

          @Test
          @DisplayName("should be empty again")
          void isEmptyAgain() {
            assertTrue(stack.isEmpty());
          }

          @Test
          @DisplayName("should have size of zero")
          void hasZeroSizeAgain() {
            assertEquals(0, stack.size());
          }
        }
      }
    }
  }
}
