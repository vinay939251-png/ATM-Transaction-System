package com.atm.test;

/**
 * Lightweight test reporter and assertion framework for the ATM project.
 * Supports running without any external test framework dependency.
 */
public class TestReporter {

    private int totalCount = 0;
    private int passCount = 0;
    private int failCount = 0;

    @FunctionalInterface
    public interface TestCase {
        void execute() throws Exception;
    }

    public void section(String sectionName) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  SECTION: " + sectionName);
        System.out.println("=".repeat(60));
    }

    public void test(String testName, TestCase testCase) {
        totalCount++;
        long startTime = System.currentTimeMillis();
        try {
            testCase.execute();
            long elapsed = System.currentTimeMillis() - startTime;
            passCount++;
            System.out.printf("  [PASS] %-48s (%d ms)%n", testName, elapsed);
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - startTime;
            failCount++;
            System.err.printf("  [FAIL] %-48s (%d ms)%n", testName, elapsed);
            System.err.println("         Error: " + t.getMessage());
        }
    }

    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: expected true, got false. " + message);
        }
    }

    public void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: expected false, got true. " + message);
        }
    }

    public void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Assertion failed: expected [" + expected + "], but got [" + actual + "]. " + message);
    }

    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("  Total Tests Run : " + totalCount);
        System.out.println("  Passed          : " + passCount);
        System.out.println("  Failed          : " + failCount);
        double passRate = totalCount > 0 ? (passCount * 100.0 / totalCount) : 0.0;
        System.out.printf("  Pass Rate       : %.1f%%%n", passRate);
        System.out.println("=".repeat(60));
        if (failCount == 0) {
            System.out.println("  STATUS: ALL TESTS PASSED SUCCESSFULLY! (PASS)");
        } else {
            System.out.println("  STATUS: SOME TESTS FAILED! (FAIL)");
        }
        System.out.println("=".repeat(60) + "\n");
    }

    public int getFailCount() {
        return failCount;
    }
}
