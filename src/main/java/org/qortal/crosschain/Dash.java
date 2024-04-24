package org.qortal.crosschain;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.libdohj.params.DashMainNetParams;
import org.qortal.crosschain.ElectrumX.Server;
import org.qortal.crosschain.ChainableServer.ConnectionType;
import org.qortal.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Dash extends Bitcoiny {

	public static final String CURRENCY_CODE = "DASH";

	private static final Coin DEFAULT_FEE_PER_KB = Coin.valueOf(1100); // 0.00001100 DASH per 1000 bytes

	private static final long MINIMUM_ORDER_AMOUNT = 1000000; // 0.01 DASH minimum order, to avoid dust errors

	// Temporary values until a dynamic fee system is written.
	private static final long MAINNET_FEE = 1000L;
	private static final long NON_MAINNET_FEE = 1000L; // enough for TESTNET3 and should be OK for REGTEST

	private static final Map<ConnectionType, Integer> DEFAULT_ELECTRUMX_PORTS = new EnumMap<>(ConnectionType.class);
	static {
		DEFAULT_ELECTRUMX_PORTS.put(ConnectionType.TCP, 50001);
		DEFAULT_ELECTRUMX_PORTS.put(ConnectionType.SSL, 50002);
	}

	public enum DashNet {
		MAIN {
			@Override
			public NetworkParameters getParams() {
				return DashMainNetParams.get();
			}

			@Override
			public Collection<Server> getServers() {
				List<ElectrumX.Server> defaultServers = Arrays.asList(
					// Servers chosen on NO BASIS WHATSOEVER from various sources!
					// Status verified at https://1209k.com/bitcoin-eye/ele.php?chain=dash
					new Server("bch.reichster.de", Server.ConnectionType.SSL, 60002),
					new Server("electrum1.cipig.net", Server.ConnectionType.SSL, 20061),
					new Server("electrum2.cipig.net", Server.ConnectionType.SSL, 20061),
					new Server("electrum3.cipig.net", Server.ConnectionType.SSL, 20061)
				);

				List<ElectrumX.Server> availableServers = new ArrayList<>();
				Boolean useDefault = Settings.getInstance().getUseDashDefaults();
				if (useDefault == true) {
					availableServers.addAll(defaultServers);
				}

				String[] settingsList = Settings.getInstance().getDashServers();
				if (settingsList != null) {
					List<ElectrumX.Server> customServers = new ArrayList<>();
					for (String setting : settingsList) {
						String[] colonParts = setting.split(":");
						if (colonParts.length == 2) {
							String[] commaParts = colonParts[1].split(",");
							if (commaParts.length == 2) {
								String hostname = colonParts[0];
								int port = Integer.parseInt(commaParts[0].trim());
								String typeString = commaParts[1].trim().toUpperCase();
								Server.ConnectionType type = Server.ConnectionType.SSL;
								if (typeString.equals("TCP")) {
									type = Server.ConnectionType.TCP;
								}
								customServers.add(new Server(hostname, type, port));
							}
						}
					}
					availableServers.addAll(customServers);
				}
				return availableServers;
			}

			@Override
			public String getGenesisHash() {
				return "00000ffd590b1485b3caadc19b22e6379c733355108f107a430458cdf3407ab6";
			}

			@Override
			public long getP2shFee(Long timestamp) {
				return this.getFeeCeiling();
			}
		},
		TEST3 {
			@Override
			public NetworkParameters getParams() {
				return TestNet3Params.get();
			}

			@Override
			public Collection<Server> getServers() {
				return Arrays.asList(); // TODO: find testnet servers
			}

			@Override
			public String getGenesisHash() {
				return "00000bafbc94add76cb75e2ec92894837288a481e5c005f6563d91623bf8bc2c";
			}

			@Override
			public long getP2shFee(Long timestamp) {
				return NON_MAINNET_FEE;
			}
		},
		REGTEST {
			@Override
			public NetworkParameters getParams() {
				return RegTestParams.get();
			}

			@Override
			public Collection<Server> getServers() {
				return Arrays.asList(
					new Server("localhost", Server.ConnectionType.TCP, 50001),
					new Server("localhost", Server.ConnectionType.SSL, 50002)
				);
			}

			@Override
			public String getGenesisHash() {
				// This is unique to each regtest instance
				return null;
			}

			@Override
			public long getP2shFee(Long timestamp) {
				return NON_MAINNET_FEE;
			}
		};

		private long feeCeiling = MAINNET_FEE;

		public long getFeeCeiling() {
			return feeCeiling;
		}

		public void setFeeCeiling(long feeCeiling) {
			this.feeCeiling = feeCeiling;
		}

		public abstract NetworkParameters getParams();
		public abstract Collection<Server> getServers();
		public abstract String getGenesisHash();
		public abstract long getP2shFee(Long timestamp) throws ForeignBlockchainException;
	}

	private static Dash instance;

	private final DashNet dashNet;

	// Constructors and instance

	private Dash(DashNet dashNet, BitcoinyBlockchainProvider blockchain, Context bitcoinjContext, String currencyCode) {
		super(blockchain, bitcoinjContext, currencyCode, DEFAULT_FEE_PER_KB);
		this.dashNet = dashNet;

		LOGGER.info(() -> String.format("Starting Dash support using %s", this.dashNet.name()));
	}

	public static synchronized Dash getInstance() {
		if (instance == null) {
			DashNet dashNet = Settings.getInstance().getDashNet();

			BitcoinyBlockchainProvider electrumX = new ElectrumX("Dash-" + dashNet.name(), dashNet.getGenesisHash(), dashNet.getServers(), DEFAULT_ELECTRUMX_PORTS);
			Context bitcoinjContext = new Context(dashNet.getParams());

			instance = new Dash(dashNet, electrumX, bitcoinjContext, CURRENCY_CODE);

			electrumX.setBlockchain(instance);
		}

		return instance;
	}

	// Getters & setters

	public static synchronized void resetForTesting() {
		instance = null;
	}

	// Actual useful methods for use by other classes

	@Override
	public long getMinimumOrderAmount() {
		return MINIMUM_ORDER_AMOUNT;
	}

	/**
	 * Returns estimated DASH fee, in sats per 1000bytes, optionally for historic timestamp.
	 * 
	 * @param timestamp optional milliseconds since epoch, or null for 'now'
	 * @return sats per 1000bytes, or throws ForeignBlockchainException if something went wrong
	 */
	@Override
	public long getP2shFee(Long timestamp) throws ForeignBlockchainException {
		return this.dashNet.getP2shFee(timestamp);
	}

	@Override
	public long getFeeCeiling() {
		return this.dashNet.getFeeCeiling();
	}

	@Override
	public void setFeeCeiling(long fee) {

		this.dashNet.setFeeCeiling( fee );
	}
}
