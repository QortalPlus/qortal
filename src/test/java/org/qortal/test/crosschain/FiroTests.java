package org.qortal.test.crosschain;

import org.junit.Ignore;
import org.junit.Test;
import org.qortal.crosschain.Bitcoiny;
import org.qortal.crosschain.Firo;

public class FiroTests extends BitcoinyTests {

	@Override
	protected String getCoinName() {
		return "Firo";
	}

	@Override
	protected String getCoinSymbol() {
		return "FIRO";
	}

	@Override
	protected Bitcoiny getCoin() {
		return Firo.getInstance();
	}

	@Override
	protected void resetCoinForTesting() {
		Firo.resetForTesting();
	}

	@Override
	protected String getDeterministicKey58() {
		return "xprv9upDP5iAFcrembT6yj3bnStAs6NZjFcJLWqBUw7UfFGxfJ2K8hh8nnLx4XXfSkWQQB1Fyi9KF5ZcSkX8JhTUzLRjeX6Zm2HVNpyoG3sAyTN";
	}

	@Override
	protected String getDeterministicPublicKey58() {
		return "xpub661MyMwAqRbcEnabTLX5uebYcsE3uG5y7ve9jn1VK8iY1MaU3YLoLJEe8sTu2YVav5Zka5qf2dmMssfxmXJTqZnazZL2kL7M2tNKwEoC34R";
	}

	@Override
	protected String getRecipient() {
		return "2N8WCg52ULCtDSMjkgVTm5mtPdCsUptkHWE";
	}

	@Test
	@Ignore(value = "Doesn't work, to be fixed later")
	public void testFindHtlcSecret() {}

	@Test
	@Ignore(value = "No testnet nodes available, so we can't regularly test buildSpend yet")
	public void testBuildSpend() {}
}
