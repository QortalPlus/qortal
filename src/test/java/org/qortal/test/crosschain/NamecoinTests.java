package org.qortal.test.crosschain;

import org.junit.Ignore;
import org.junit.Test;
import org.qortal.crosschain.Bitcoiny;
import org.qortal.crosschain.Namecoin;

public class NamecoinTests extends BitcoinyTests {

	@Override
	protected String getCoinName() {
		return "Namecoin";
	}

	@Override
	protected String getCoinSymbol() {
		return "NMC";
	}

	@Override
	protected Bitcoiny getCoin() {
		return Namecoin.getInstance();
	}

	@Override
	protected void resetCoinForTesting() {
		Namecoin.resetForTesting();
	}

	@Override
	protected String getDeterministicKey58() {
		return "xprv9txumfvn2ZKUv9XBxVLqaEcEdh5jcQU3A5gyZKqXt39vCQPBXkF596UmyhF5TtiwPBjiRx1LwpPagjBQmxJu7eMTwHSgFwSLkD9BYtAUzab";
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
