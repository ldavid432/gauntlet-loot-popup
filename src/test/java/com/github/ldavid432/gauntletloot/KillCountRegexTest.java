package com.github.ldavid432.gauntletloot;

import static com.github.ldavid432.GauntletLootUtil.KC_PATTERN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class KillCountRegexTest
{

	private Pattern pattern;

	@Before
	public void setUp()
	{
		pattern = KC_PATTERN;
	}

	// ========== VALID BASIC PATTERNS ==========

	@Test
	public void testBasicGauntletCompletionCount()
	{
		String input = "Your Gauntlet completion count is: <col=000000>10</col>.";
		assertTrue("Should match basic Gauntlet completion count", pattern.matcher(input).find());
		assertEquals("Should extract count", "10", extractCount(input));
	}

	@Test
	public void testBasicHunllefKillCount()
	{
		String input = "Your Corrupted Hunllef (Echo) kill count is: <col=aabbcc>42</col>";
		assertTrue("Should match basic Hunllef kill count", pattern.matcher(input).find());
		assertEquals("Should extract count", "42", extractCount(input));
	}

	@Test
	public void testCorruptedGauntletCompletionCount()
	{
		String input = "Your Corrupted Gauntlet completion count is: <col=00ff00>25</col>.";
		assertTrue("Should match Corrupted Gauntlet completion count", pattern.matcher(input).find());
		assertEquals("Should extract count", "25", extractCount(input));
	}

	// ========== COLOR TAG VARIATIONS ==========

	@Test
	public void testWithOpeningAndClosingColorTags()
	{
		String input = "Your <col=fedcba>Gauntlet</col> kill count is: <col=654321>75</col>.";
		assertTrue("Should match with opening and closing color tags", pattern.matcher(input).find());
		assertEquals("Should extract count", "75", extractCount(input));
	}

	@Test
	public void testNumericHexOnly()
	{
		String input = "Your Gauntlet kill count is: <col=123456>5</col>.";
		assertTrue("Should match with numeric hex only", pattern.matcher(input).find());
	}

	// ========== NUMBER FORMATS ==========

	@Test
	public void testSingleDigitCount()
	{
		String input = "Your Gauntlet kill count is: <col=000000>1</col>.";
		assertTrue("Should match single digit", pattern.matcher(input).find());
		assertEquals("Should extract single digit", "1", extractCount(input));
	}

	@Test
	public void testTwoDigitCount()
	{
		String input = "Your Gauntlet kill count is: <col=000000>42</col>.";
		assertTrue("Should match two digit count", pattern.matcher(input).find());
		assertEquals("Should extract two digit count", "42", extractCount(input));
	}

	@Test
	public void testThreeDigitCount()
	{
		String input = "Your Gauntlet kill count is: <col=000000>999</col>.";
		assertTrue("Should match three digit count", pattern.matcher(input).find());
		assertEquals("Should extract three digit count", "999", extractCount(input));
	}

	@Test
	public void testThousandsSeparator()
	{
		String input = "Your Gauntlet kill count is: <col=000000>1,000</col>.";
		assertTrue("Should match thousands separator", pattern.matcher(input).find());
		assertEquals("Should extract thousands with comma", "1,000", extractCount(input));
	}

	@Test
	public void testTenThousandWithComma()
	{
		String input = "Your Gauntlet kill count is: <col=000000>10,500</col>.";
		assertTrue("Should match ten thousands with comma", pattern.matcher(input).find());
		assertEquals("Should extract ten thousands", "10,500", extractCount(input));
	}

	@Test
	public void testMillionWithCommas()
	{
		String input = "Your Gauntlet kill count is: <col=000000>1,234,567</col>.";
		assertTrue("Should match millions with commas", pattern.matcher(input).find());
		assertEquals("Should extract millions", "1,234,567", extractCount(input));
	}

	@Test
	public void testBillionWithCommas()
	{
		String input = "Your Gauntlet kill count is: <col=000000>2,147,483,647</col>.";
		assertTrue("Should match billions with commas", pattern.matcher(input).find());
		assertEquals("Should extract billions", "2,147,483,647", extractCount(input));
	}

	@Test
	public void testLargeNumberWithoutCommas()
	{
		String input = "Your Gauntlet kill count is: <col=000000>9876543</col>.";
		assertTrue("Should match large number without commas", pattern.matcher(input).find());
		assertEquals("Should extract large number", "9876543", extractCount(input));
	}

	// ========== REAL-WORLD SCENARIOS ==========

	@Test
	public void testRealWorldScenario1()
	{
		String input = "Your <col=ff6b9d>Corrupted Gauntlet</col> completion count is: <col=00d9ff>1,234</col>.";
		assertTrue("Should match real-world scenario 1", pattern.matcher(input).find());
		assertEquals("Should extract count", "1,234", extractCount(input));
	}

	@Test
	public void testRealWorldScenario2()
	{
		String input = "Your Corrupted Hunllef (Echo) kill count is: <col=ffd700>42,069</col>.";
		assertTrue("Should match real-world scenario 2", pattern.matcher(input).find());
		assertEquals("Should extract count", "42,069", extractCount(input));
	}

	@Test
	public void testRealWorldScenario3()
	{
		String input = "Your <col=c0ffee>Gauntlet</col> kill count is: <col=deadbe>999,999</col>";
		assertTrue("Should match real-world scenario 3", pattern.matcher(input).find());
		assertEquals("Should extract count", "999,999", extractCount(input));
	}

	@Test
	public void testRealWorldScenario4()
	{
		String input = "Your Corrupted Gauntlet completion count is: <col=123abc>5,000,000</col>.";
		assertTrue("Should match real-world scenario 4", pattern.matcher(input).find());
		assertEquals("Should extract count", "5,000,000", extractCount(input));
	}

	// ========== NEGATIVE TESTS ==========

	@Test
	public void testMissingStartingYour()
	{
		String input = "Gauntlet kill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match without 'Your'", pattern.matcher(input).find());
	}

	@Test
	public void testInvalidBosName()
	{
		String input = "Your Zuk kill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match invalid boss name", pattern.matcher(input).find());
	}

	@Test
	public void testInvalidCountType()
	{
		String input = "Your Gauntlet deaths count is: <col=000000>10</col>.";
		assertFalse("Should NOT match invalid count type", pattern.matcher(input).find());
	}

	@Test
	public void testMissingColorTag()
	{
		String input = "Your Gauntlet kill count is: 10</col>.";
		assertFalse("Should NOT match without color tag for count", pattern.matcher(input).find());
	}

	@Test
	public void testInvalidHexTooShort()
	{
		String input = "Your Gauntlet kill count is: <col=fff>10</col>.";
		assertFalse("Should NOT match hex with wrong length", pattern.matcher(input).find());
	}

	@Test
	public void testInvalidHexTooLong()
	{
		String input = "Your Gauntlet kill count is: <col=0000000>10</col>.";
		assertFalse("Should NOT match hex with wrong length", pattern.matcher(input).find());
	}

	@Test
	public void testInvalidHexWithG()
	{
		String input = "Your Gauntlet kill count is: <col=00000g>10</col>.";
		assertFalse("Should NOT match hex with invalid character 'g'", pattern.matcher(input).find());
	}

	@Test
	public void testMissingClosingColorTag()
	{
		String input = "Your Gauntlet kill count is: <col=000000>10</col";
		assertFalse("Should NOT match without closing >", pattern.matcher(input).find());
	}

	@Test
	public void testWrongCountFormat()
	{
		String input = "Your Gauntlet kill count is: <col=000000>10a</col>.";
		assertFalse("Should NOT match with letters in count", pattern.matcher(input).find());
	}

	@Test
	public void testCountWithoutNumbers()
	{
		String input = "Your Gauntlet kill count is: <col=000000>abc</col>.";
		assertFalse("Should NOT match without any numbers", pattern.matcher(input).find());
	}

	@Test
	public void testHunllefMissingEcho()
	{
		String input = "Your Corrupted Hunllef kill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match Hunllef without (Echo)", pattern.matcher(input).find());
	}

	@Test
	public void testHunllefWithWrongParentheses()
	{
		String input = "Your Corrupted Hunllef [Echo] kill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match with square brackets instead of parentheses", pattern.matcher(input).find());
	}

	@Test
	public void testDoubleSpaceBeforeKill()
	{
		String input = "Your Gauntlet  kill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match with double space", pattern.matcher(input).find());
	}

	@Test
	public void testMissingSpace()
	{
		String input = "Your Gauntletkill count is: <col=000000>10</col>.";
		assertFalse("Should NOT match without space", pattern.matcher(input).find());
	}

	// ========== UTILITY METHOD ==========

	/**
	 * Extracts the count value from the matched pattern using the capture group.
	 *
	 * @param input the input string to match
	 * @return the captured count value, or null if no match found
	 */
	private String extractCount(String input)
	{
		Matcher matcher = pattern.matcher(input);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		return null;
	}
}
