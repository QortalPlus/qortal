package org.qortal.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bitcoinj.core.Transaction;
import org.qortal.api.ApiError;
import org.qortal.api.ApiErrors;
import org.qortal.api.ApiExceptionFactory;
import org.qortal.api.Security;
import org.qortal.api.model.crosschain.AddressRequest;
import org.qortal.api.model.crosschain.DashSendRequest;
import org.qortal.crosschain.AddressInfo;
import org.qortal.crosschain.Dash;
import org.qortal.crosschain.ForeignBlockchainException;
import org.qortal.crosschain.SimpleTransaction;
import org.qortal.crosschain.ServerConfigurationInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/crosschain/dash")
@Tag(name = "Cross-Chain (Dash)")
public class CrossChainDashResource {

	@Context
	HttpServletRequest request;

	@GET
	@Path("/height")
	@Operation(
		summary = "Returns current Dash block height",
		description = "Returns the height of the most recent block in the Dash chain.",
		responses = {
			@ApiResponse(
				content = @Content(
					schema = @Schema(
						type = "number"
					)
				)
			)
		}
	)
	@ApiErrors({ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE})
	public String getDashHeight() {
		Dash dash = Dash.getInstance();

		try {
			Integer height = dash.getBlockchainHeight();
			if (height == null)
				throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);

			return height.toString();

		} catch (ForeignBlockchainException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);
		}
	}

	@POST
	@Path("/walletbalance")
	@Operation(
		summary = "Returns DASH balance for hierarchical, deterministic BIP32 wallet",
		description = "Supply BIP32 'm' private/public key in base58, starting with 'xprv'/'xpub' for mainnet, 'tprv'/'tpub' for testnet",
		requestBody = @RequestBody(
			required = true,
			content = @Content(
				mediaType = MediaType.TEXT_PLAIN,
				schema = @Schema(
					type = "string",
					description = "BIP32 'm' private/public key in base58",
					example = "tpubD6NzVbkrYhZ4XTPc4btCZ6SMgn8CxmWkj6VBVZ1tfcJfMq4UwAjZbG8U74gGSypL9XBYk2R2BLbDBe8pcEyBKM1edsGQEPKXNbEskZozeZc"
				)
			)
		),
		responses = {
			@ApiResponse(
				content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string", description = "balance (satoshis)"))
			)
		}
	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE})
	@SecurityRequirement(name = "apiKey")
	public String getDashWalletBalance(@HeaderParam(Security.API_KEY_HEADER) String apiKey, String key58) {
		Security.checkApiCallAllowed(request);

		Dash dash = Dash.getInstance();

		if (!dash.isValidDeterministicKey(key58))
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_PRIVATE_KEY);

		try {
			Long balance = dash.getWalletBalance(key58);
			if (balance == null)
				throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);

			return balance.toString();

		} catch (ForeignBlockchainException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);
		}
	}

	@POST
	@Path("/wallettransactions")
	@Operation(
		summary = "Returns transactions for hierarchical, deterministic BIP32 wallet",
		description = "Supply BIP32 'm' private/public key in base58, starting with 'xprv'/'xpub' for mainnet, 'tprv'/'tpub' for testnet",
		requestBody = @RequestBody(
			required = true,
			content = @Content(
				mediaType = MediaType.TEXT_PLAIN,
				schema = @Schema(
					type = "string",
					description = "BIP32 'm' private/public key in base58",
					example = "tpubD6NzVbkrYhZ4XTPc4btCZ6SMgn8CxmWkj6VBVZ1tfcJfMq4UwAjZbG8U74gGSypL9XBYk2R2BLbDBe8pcEyBKM1edsGQEPKXNbEskZozeZc"
				)
			)
		),
		responses = {
			@ApiResponse(
				content = @Content(array = @ArraySchema( schema = @Schema( implementation = SimpleTransaction.class ) ) )
			)
		}
	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE})
	@SecurityRequirement(name = "apiKey")
	public List<SimpleTransaction> getDashWalletTransactions(@HeaderParam(Security.API_KEY_HEADER) String apiKey, String key58) {
		Security.checkApiCallAllowed(request);

		Dash dash = Dash.getInstance();

		if (!dash.isValidDeterministicKey(key58))
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_PRIVATE_KEY);

		try {
			return dash.getWalletTransactions(key58);
		} catch (ForeignBlockchainException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);
		}
	}

	@POST
	@Path("/addressinfos")
	@Operation(
			summary = "Returns information for each address for a hierarchical, deterministic BIP32 wallet",
			description = "Supply BIP32 'm' private/public key in base58, starting with 'xprv'/'xpub' for mainnet, 'tprv'/'tpub' for testnet",
			requestBody = @RequestBody(
					required = true,
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON,
							schema = @Schema(
									implementation = AddressRequest.class
							)
					)
			),
			responses = {
					@ApiResponse(
							content = @Content(array = @ArraySchema( schema = @Schema( implementation = AddressInfo.class ) ) )
					)
			}

	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE})
	@SecurityRequirement(name = "apiKey")
	public List<AddressInfo> getDashAddressInfos(@HeaderParam(Security.API_KEY_HEADER) String apiKey, AddressRequest addressRequest) {
		Security.checkApiCallAllowed(request);

		Dash dash = Dash.getInstance();

		if (!dash.isValidDeterministicKey(addressRequest.xpub58))
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_PRIVATE_KEY);

		try {
			return dash.getWalletAddressInfos(addressRequest.xpub58);
		} catch (ForeignBlockchainException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);
		}
	}

	@POST
	@Path("/send")
	@Operation(
		summary = "Sends DASH from hierarchical, deterministic BIP32 wallet to specific address",
		description = "Currently only supports 'legacy' P2PKH Dash addresses. Supply BIP32 'm' private key in base58, starting with 'xprv' for mainnet, 'tprv' for testnet",
		requestBody = @RequestBody(
			required = true,
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON,
				schema = @Schema(
					implementation = DashSendRequest.class
				)
			)
		),
		responses = {
			@ApiResponse(
				content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "string", description = "transaction hash"))
			)
		}
	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.INVALID_CRITERIA, ApiError.INVALID_ADDRESS, ApiError.FOREIGN_BLOCKCHAIN_BALANCE_ISSUE, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE})
	@SecurityRequirement(name = "apiKey")
	public String sendBitcoin(@HeaderParam(Security.API_KEY_HEADER) String apiKey, DashSendRequest dashSendRequest) {
		Security.checkApiCallAllowed(request);

		if (dashSendRequest.dashAmount <= 0)
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_CRITERIA);

		if (dashSendRequest.feePerByte != null && dashSendRequest.feePerByte <= 0)
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_CRITERIA);

		Dash dash = Dash.getInstance();

		if (!dash.isValidAddress(dashSendRequest.receivingAddress))
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_ADDRESS);

		if (!dash.isValidDeterministicKey(dashSendRequest.xprv58))
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_PRIVATE_KEY);

		Transaction spendTransaction = dash.buildSpend(dashSendRequest.xprv58,
				dashSendRequest.receivingAddress,
				dashSendRequest.dashAmount,
				dashSendRequest.feePerByte);

		if (spendTransaction == null)
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_BALANCE_ISSUE);

		try {
			dash.broadcastTransaction(spendTransaction);
		} catch (ForeignBlockchainException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.FOREIGN_BLOCKCHAIN_NETWORK_ISSUE);
		}

		return spendTransaction.getTxId().toString();
	}

	@GET
	@Path("/serverinfos")
	@Operation(
			summary = "Returns current Dash server configuration",
			description = "Returns current Dash server locations and use status",
			responses = {
					@ApiResponse(
							content = @Content(
									mediaType = MediaType.APPLICATION_JSON,
									schema = @Schema(
											implementation = ServerConfigurationInfo.class
									)
							)
					)
			}
	)
	public ServerConfigurationInfo getServerConfiguration(@Parameter(
		description = "Returns info for the currently connected server only"
	) @QueryParam("current") Boolean current) {

		return CrossChainUtils.buildServerConfigurationInfo(Dash.getInstance(), current);
	}

	@GET
	@Path("/feekb")
	@Operation(
			summary = "Returns Dash fee per Kb.",
			description = "Returns Dash fee per Kb.",
			responses = {
					@ApiResponse(
							content = @Content(
									schema = @Schema(
											type = "number"
									)
							)
					)
			}
	)
	public String getDashFeePerKb() {
		Dash dash = Dash.getInstance();

		return String.valueOf(dash.getFeePerKb().value);
	}

	@POST
	@Path("/updatefeekb")
	@Operation(
			summary = "Sets Dash fee per Kb.",
			description = "Sets Dash fee per Kb.",
			requestBody = @RequestBody(
					required = true,
					content = @Content(
							mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(
									type = "number",
									description = "the fee per Kb",
									example = "100"
							)
					)
			),
			responses = {
					@ApiResponse(
							content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "number", description = "fee"))
					)
			}
	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.INVALID_CRITERIA})
	public String setDashFeePerKb(@HeaderParam(Security.API_KEY_HEADER) String apiKey, String fee) {
		Security.checkApiCallAllowed(request);

		Dash dash = Dash.getInstance();

		try {
			return CrossChainUtils.setFeePerKb(dash, fee);
		} catch (IllegalArgumentException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_CRITERIA);
		}
	}

	@GET
	@Path("/feeceiling")
	@Operation(
			summary = "Returns Dash fee per Kb.",
			description = "Returns Dash fee per Kb.",
			responses = {
					@ApiResponse(
							content = @Content(
									schema = @Schema(
											type = "number"
									)
							)
					)
			}
	)
	public String getDashFeeCeiling() {
		Dash dash = Dash.getInstance();

		return String.valueOf(dash.getFeeCeiling());
	}

	@POST
	@Path("/updatefeeceiling")
	@Operation(
			summary = "Sets Dash fee ceiling.",
			description = "Sets Dash fee ceiling.",
			requestBody = @RequestBody(
					required = true,
					content = @Content(
							mediaType = MediaType.TEXT_PLAIN,
							schema = @Schema(
									type = "number",
									description = "the fee",
									example = "100"
							)
					)
			),
			responses = {
					@ApiResponse(
							content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(type = "number", description = "fee"))
					)
			}
	)
	@ApiErrors({ApiError.INVALID_PRIVATE_KEY, ApiError.INVALID_CRITERIA})
	public String setDashFeeCeiling(@HeaderParam(Security.API_KEY_HEADER) String apiKey, String fee) {
		Security.checkApiCallAllowed(request);

		Dash dash = Dash.getInstance();

		try {
			return CrossChainUtils.setFeeCeiling(dash, fee);
		}
		catch (IllegalArgumentException e) {
			throw ApiExceptionFactory.INSTANCE.createException(request, ApiError.INVALID_CRITERIA);
		}
	}
}
