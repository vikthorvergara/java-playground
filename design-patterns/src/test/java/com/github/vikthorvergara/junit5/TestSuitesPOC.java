package com.github.vikthorvergara.junit5;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("All JUnit 5 POC Tests")
@SelectPackages("com.github.vikthorvergara.junit5")
class TestSuitesPOC {}

@Suite
@SuiteDisplayName("Basic Features Suite")
@SelectClasses({
  BasicAnnotationsPOCTest.class,
  AssertionsPOCTest.class,
  LifecyclePOCTest.class,
  AssumptionsPOCTest.class
})
class BasicFeaturesSuite {}

@Suite
@SuiteDisplayName("Advanced Features Suite")
@SelectClasses({
  ParameterizedTestsPOCTest.class,
  DynamicTestsPOCTest.class,
  RepeatedTestsPOCTest.class,
  NestedTestsPOCTest.class
})
class AdvancedFeaturesSuite {}

@Suite
@SuiteDisplayName("Extension Features Suite")
@SelectClasses({CustomExtensionPOCTest.class, TempDirectoryPOCTest.class})
class ExtensionFeaturesSuite {}
