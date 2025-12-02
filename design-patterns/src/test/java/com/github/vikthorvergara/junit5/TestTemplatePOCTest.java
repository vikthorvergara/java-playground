package com.github.vikthorvergara.junit5;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.*;

@DisplayName("Test Templates")
class TestTemplatePOCTest {

  @TestTemplate
  @ExtendWith(MultipleContextProvider.class)
  @DisplayName("Template test with multiple contexts")
  void testWithMultipleContexts(String context) {
    TestTemplatePOC poc = new TestTemplatePOC();
    String result = poc.process(context);
    assertNotNull(result);
    assertTrue(result.contains(context));
  }

  @TestTemplate
  @ExtendWith(NumberContextProvider.class)
  @DisplayName("Template test with numbers")
  void testWithNumbers(int value) {
    TestTemplatePOC poc = new TestTemplatePOC();
    int result = poc.calculate(value);
    assertEquals(value * 2, result);
  }

  @TestTemplate
  @ExtendWith(ValidationContextProvider.class)
  @DisplayName("Template test for validation")
  void testValidation(String input, boolean expectedValid) {
    TestTemplatePOC poc = new TestTemplatePOC();
    assertEquals(expectedValid, poc.validate(input));
  }

  static class MultipleContextProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
      return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
        ExtensionContext context) {
      return Stream.of("context1", "context2", "context3")
          .map(
              ctx ->
                  new TestTemplateInvocationContext() {
                    @Override
                    public String getDisplayName(int invocationIndex) {
                      return "Context: " + ctx;
                    }

                    @Override
                    public List<Extension> getAdditionalExtensions() {
                      return Arrays.asList(
                          new ParameterResolver() {
                            @Override
                            public boolean supportsParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              return parameterContext
                                  .getParameter()
                                  .getType()
                                  .equals(String.class);
                            }

                            @Override
                            public Object resolveParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              return ctx;
                            }
                          });
                    }
                  });
    }
  }

  static class NumberContextProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
      return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
        ExtensionContext context) {
      return Stream.of(5, 10, 15, 20)
          .map(
              num ->
                  new TestTemplateInvocationContext() {
                    @Override
                    public String getDisplayName(int invocationIndex) {
                      return "Number: " + num;
                    }

                    @Override
                    public List<Extension> getAdditionalExtensions() {
                      return Arrays.asList(
                          new ParameterResolver() {
                            @Override
                            public boolean supportsParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              return parameterContext
                                  .getParameter()
                                  .getType()
                                  .equals(int.class);
                            }

                            @Override
                            public Object resolveParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              return num;
                            }
                          });
                    }
                  });
    }
  }

  static class ValidationContextProvider implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
      return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
        ExtensionContext context) {
      return Stream.of(
              new Object[][] {
                {"valid", true}, {"", false}, {null, false}, {"another-valid", true}
              })
          .map(
              params ->
                  new TestTemplateInvocationContext() {
                    @Override
                    public String getDisplayName(int invocationIndex) {
                      return "Validating: " + params[0] + " -> " + params[1];
                    }

                    @Override
                    public List<Extension> getAdditionalExtensions() {
                      return Arrays.asList(
                          new ParameterResolver() {
                            @Override
                            public boolean supportsParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              return true;
                            }

                            @Override
                            public Object resolveParameter(
                                ParameterContext parameterContext,
                                ExtensionContext extensionContext) {
                              int index = parameterContext.getIndex();
                              return params[index];
                            }
                          });
                    }
                  });
    }
  }
}
