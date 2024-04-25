package org.qortal.test.crosschain;

import org.junit.Ignore;
import org.junit.Test;
import org.qortal.crosschain.Bitcoiny;
import org.qortal.crosschain.Dash;

public class DashTests extends BitcoinyTests {

	@Override
	protected String getCoinName() {
		return "Dash";
	}

	@Override
	protected String getCoinSymbol() {
		return "DASH";
	}

	@Override
	protected Bitcoiny getCoin() {
		return Dash.getInstance();
	}

	@Override
	protected void resetCoinForTesting() {
		Dash.resetForTesting();
	}

	@Override
	protected String getDeterministicKey58() {
		return "xprv9u4EnBTbEYgYF4N8Mt1EzMpFRfCsa9VgVuKEmfBR3EGnZasYriWz1FX6yM9D6jBm1DTR8FdKG1YZDV35FtqyYJ1Dof1XUX1zLsY2p6XRiGT";
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
