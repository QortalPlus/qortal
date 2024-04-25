package org.qortal.crosschain;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.libdohj.params.NamecoinMainNetParams;
import org.qortal.crosschain.ElectrumX.Server;
import org.qortal.crosschain.ChainableServer.ConnectionType;
import org.qortal.settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Namecoin extends Bitcoiny {

	public static final String CURRENCY_CODE = "NMC";

	private static final Coin DEFAULT_FEE_PER_KB = Coin.valueOf(600000); // 0.006 NMC per 1000 bytes

	private static final long MINIMUM_ORDER_AMOUNT = 10000000L; // 0.1 NMC minimum order, to avoid dust errors

	// Temporary values until a dynamic fee system is written.
	private static final long MAINNET_FEE = 200000L;
	private static final long NON_MAINNET_FEE = 1000L; // enough for TESTNET3 and should be OK for REGTEST

	private static final Map<ConnectionType, Integer> DEFAULT_ELECTRUMX_PORTS = new EnumMap<>(ConnectionType.class);
	static {
		DEFAULT_ELECTRUMX_PORTS.put(ConnectionType.TCP, 50001);
		DEFAULT_ELECTRUMX_PORTS.put(ConnectionType.SSL, 50002);
	}

	public enum NamecoinNet {
		MAIN {
			@Override
			public NetworkParameters getParams() {
				return NamecoinMainNetParams.get();
			}

			@Override
			public Collection<Server> getServers() {
				List<ElectrumX.Server> defaultServers = Arrays.asList(
					// Servers chosen on NO BASIS WHATSOEVER from various sources!
					// Status verified at https://1209k.com/bitcoin-eye/ele.php?chain=nmc
					new Server("162.212.154.52", Server.ConnectionType.SSL, 50002),
					new Server("46.229.238.187", Server.ConnectionType.SSL, 57002),
					new Server("electrumx1.nmc.dotbit.zone", Server.ConnectionType.SSL, 50002),
					new Server("electrumx2.nmc.dotbit.zone", Server.ConnectionType.SSL, 50002),
					new Server("electrumx3.nmc.dotbit.zone", Server.ConnectionType.SSL, 50002),
					new Server("electrumx4.nmc.dotbit.zone", Server.ConnectionType.SSL, 50002),
					new Server("nmc2.bitcoins.sk", Server.ConnectionType.SSL, 57002)
				);

				List<ElectrumX.Server> availableServers = new ArrayList<>();
				Boolean useDefault = Settings.getInstance().getUseNamecoinDefaults();
				if (useDefault == true) {
					availableServers.addAll(defaultServers);
				}

				String[] settingsList = Settings.getInstance().getNamecoinServers();
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
				return "000000000062b72c5e2ceb45fbc8587e807c155b0da735e6483dfba2f0a9c770";
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
				return "00000007199508e34a9ff81e6ec0c477a4cccff2a4767a8eee39c11db367b008";
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

	private static Namecoin instance;

	private final NamecoinNet namecoinNet;

	// Constructors and instance

	private Namecoin(NamecoinNet namecoinNet, BitcoinyBlockchainProvider blockchain, Context bitcoinjContext, String currencyCode) {
		super(blockchain, bitcoinjContext, currencyCode, DEFAULT_FEE_PER_KB);
		this.namecoinNet = namecoinNet;

		LOGGER.info(() -> String.format("Starting Namecoin support using %s", this.namecoinNet.name()));
	}

	public static synchronized Namecoin getInstance() {
		if (instance == null) {
			NamecoinNet namecoinNet = Settings.getInstance().getNamecoinNet();

			BitcoinyBlockchainProvider electrumX = new ElectrumX("Namecoin-" + namecoinNet.name(), namecoinNet.getGenesisHash(), namecoinNet.getServers(), DEFAULT_ELECTRUMX_PORTS);
			Context bitcoinjContext = new Context(namecoinNet.getParams());

			instance = new Namecoin(namecoinNet, electrumX, bitcoinjContext, CURRENCY_CODE);

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
	 * Returns estimated NMC fee, in sats per 1000bytes, optionally for historic timestamp.
	 * 
	 * @param timestamp optional milliseconds since epoch, or null for 'now'
	 * @return sats per 1000bytes, or throws ForeignBlockchainException if something went wrong
	 */
	@Override
	public long getP2shFee(Long timestamp) throws ForeignBlockchainException {
		return this.namecoinNet.getP2shFee(timestamp);
	}

	@Override
	public long getFeeCeiling() {
		return this.namecoinNet.getFeeCeiling();
	}

	@Override
	public void setFeeCeiling(long fee) {

		this.namecoinNet.setFeeCeiling( fee );
	}
}
