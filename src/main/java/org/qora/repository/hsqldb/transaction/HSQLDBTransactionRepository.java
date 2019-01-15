package org.qora.repository.hsqldb.transaction;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qora.data.PaymentData;
import org.qora.data.transaction.GroupInviteTransactionData;
import org.qora.data.transaction.TransactionData;
import org.qora.repository.DataException;
import org.qora.repository.TransactionRepository;
import org.qora.repository.hsqldb.HSQLDBRepository;
import org.qora.repository.hsqldb.HSQLDBSaver;
import org.qora.transaction.Transaction.TransactionType;

public class HSQLDBTransactionRepository implements TransactionRepository {

	private static final Logger LOGGER = LogManager.getLogger(HSQLDBTransactionRepository.class);

	protected HSQLDBRepository repository;
	private HSQLDBGenesisTransactionRepository genesisTransactionRepository;
	private HSQLDBPaymentTransactionRepository paymentTransactionRepository;
	private HSQLDBRegisterNameTransactionRepository registerNameTransactionRepository;
	private HSQLDBUpdateNameTransactionRepository updateNameTransactionRepository;
	private HSQLDBSellNameTransactionRepository sellNameTransactionRepository;
	private HSQLDBCancelSellNameTransactionRepository cancelSellNameTransactionRepository;
	private HSQLDBBuyNameTransactionRepository buyNameTransactionRepository;
	private HSQLDBCreatePollTransactionRepository createPollTransactionRepository;
	private HSQLDBVoteOnPollTransactionRepository voteOnPollTransactionRepository;
	private HSQLDBArbitraryTransactionRepository arbitraryTransactionRepository;
	private HSQLDBIssueAssetTransactionRepository issueAssetTransactionRepository;
	private HSQLDBTransferAssetTransactionRepository transferAssetTransactionRepository;
	private HSQLDBCreateOrderTransactionRepository createOrderTransactionRepository;
	private HSQLDBCancelOrderTransactionRepository cancelOrderTransactionRepository;
	private HSQLDBMultiPaymentTransactionRepository multiPaymentTransactionRepository;
	private HSQLDBDeployATTransactionRepository deployATTransactionRepository;
	private HSQLDBMessageTransactionRepository messageTransactionRepository;
	private HSQLDBATTransactionRepository atTransactionRepository;
	private HSQLDBCreateGroupTransactionRepository createGroupTransactionRepository;
	private HSQLDBUpdateGroupTransactionRepository updateGroupTransactionRepository;
	private HSQLDBAddGroupAdminTransactionRepository addGroupAdminTransactionRepository;
	private HSQLDBRemoveGroupAdminTransactionRepository removeGroupAdminTransactionRepository;
	private HSQLDBGroupBanTransactionRepository groupBanTransactionRepository;
	private HSQLDBGroupUnbanTransactionRepository groupUnbanTransactionRepository;
	private HSQLDBGroupKickTransactionRepository groupKickTransactionRepository;
	private HSQLDBGroupInviteTransactionRepository groupInviteTransactionRepository;
	private HSQLDBCancelGroupInviteTransactionRepository cancelGroupInviteTransactionRepository;
	private HSQLDBJoinGroupTransactionRepository joinGroupTransactionRepository;
	private HSQLDBLeaveGroupTransactionRepository leaveGroupTransactionRepository;

	public HSQLDBTransactionRepository(HSQLDBRepository repository) {
		this.repository = repository;
		this.genesisTransactionRepository = new HSQLDBGenesisTransactionRepository(repository);
		this.paymentTransactionRepository = new HSQLDBPaymentTransactionRepository(repository);
		this.registerNameTransactionRepository = new HSQLDBRegisterNameTransactionRepository(repository);
		this.updateNameTransactionRepository = new HSQLDBUpdateNameTransactionRepository(repository);
		this.sellNameTransactionRepository = new HSQLDBSellNameTransactionRepository(repository);
		this.cancelSellNameTransactionRepository = new HSQLDBCancelSellNameTransactionRepository(repository);
		this.buyNameTransactionRepository = new HSQLDBBuyNameTransactionRepository(repository);
		this.createPollTransactionRepository = new HSQLDBCreatePollTransactionRepository(repository);
		this.voteOnPollTransactionRepository = new HSQLDBVoteOnPollTransactionRepository(repository);
		this.arbitraryTransactionRepository = new HSQLDBArbitraryTransactionRepository(repository);
		this.issueAssetTransactionRepository = new HSQLDBIssueAssetTransactionRepository(repository);
		this.transferAssetTransactionRepository = new HSQLDBTransferAssetTransactionRepository(repository);
		this.createOrderTransactionRepository = new HSQLDBCreateOrderTransactionRepository(repository);
		this.cancelOrderTransactionRepository = new HSQLDBCancelOrderTransactionRepository(repository);
		this.multiPaymentTransactionRepository = new HSQLDBMultiPaymentTransactionRepository(repository);
		this.deployATTransactionRepository = new HSQLDBDeployATTransactionRepository(repository);
		this.messageTransactionRepository = new HSQLDBMessageTransactionRepository(repository);
		this.atTransactionRepository = new HSQLDBATTransactionRepository(repository);
		this.createGroupTransactionRepository = new HSQLDBCreateGroupTransactionRepository(repository);
		this.updateGroupTransactionRepository = new HSQLDBUpdateGroupTransactionRepository(repository);
		this.addGroupAdminTransactionRepository = new HSQLDBAddGroupAdminTransactionRepository(repository);
		this.removeGroupAdminTransactionRepository = new HSQLDBRemoveGroupAdminTransactionRepository(repository);
		this.groupBanTransactionRepository = new HSQLDBGroupBanTransactionRepository(repository);
		this.groupUnbanTransactionRepository = new HSQLDBGroupUnbanTransactionRepository(repository);
		this.groupKickTransactionRepository = new HSQLDBGroupKickTransactionRepository(repository);
		this.groupInviteTransactionRepository = new HSQLDBGroupInviteTransactionRepository(repository);
		this.cancelGroupInviteTransactionRepository = new HSQLDBCancelGroupInviteTransactionRepository(repository);
		this.joinGroupTransactionRepository = new HSQLDBJoinGroupTransactionRepository(repository);
		this.leaveGroupTransactionRepository = new HSQLDBLeaveGroupTransactionRepository(repository);
	}

	protected HSQLDBTransactionRepository() {
	}

	@Override
	public TransactionData fromSignature(byte[] signature) throws DataException {
		try (ResultSet resultSet = this.repository.checkedExecute("SELECT type, reference, creator, creation, fee FROM Transactions WHERE signature = ?",
				signature)) {
			if (resultSet == null)
				return null;

			TransactionType type = TransactionType.valueOf(resultSet.getInt(1));
			byte[] reference = resultSet.getBytes(2);
			byte[] creatorPublicKey = resultSet.getBytes(3);
			long timestamp = resultSet.getTimestamp(4, Calendar.getInstance(HSQLDBRepository.UTC)).getTime();
			BigDecimal fee = resultSet.getBigDecimal(5).setScale(8);

			TransactionData transactionData = this.fromBase(type, signature, reference, creatorPublicKey, timestamp, fee);
			return maybeIncludeBlockHeight(transactionData);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch transaction from repository", e);
		}
	}

	@Override
	public TransactionData fromReference(byte[] reference) throws DataException {
		try (ResultSet resultSet = this.repository.checkedExecute("SELECT type, signature, creator, creation, fee FROM Transactions WHERE reference = ?",
				reference)) {
			if (resultSet == null)
				return null;

			TransactionType type = TransactionType.valueOf(resultSet.getInt(1));
			byte[] signature = resultSet.getBytes(2);
			byte[] creatorPublicKey = resultSet.getBytes(3);
			long timestamp = resultSet.getTimestamp(4, Calendar.getInstance(HSQLDBRepository.UTC)).getTime();
			BigDecimal fee = resultSet.getBigDecimal(5).setScale(8);

			TransactionData transactionData = this.fromBase(type, signature, reference, creatorPublicKey, timestamp, fee);
			return maybeIncludeBlockHeight(transactionData);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch transaction from repository", e);
		}
	}

	private TransactionData maybeIncludeBlockHeight(TransactionData transactionData) throws DataException {
		int blockHeight = getHeightFromSignature(transactionData.getSignature());
		if (blockHeight != 0)
			transactionData.setBlockHeight(blockHeight);

		return transactionData;
	}

	@Override
	public TransactionData fromHeightAndSequence(int height, int sequence) throws DataException {
		try (ResultSet resultSet = this.repository.checkedExecute(
				"SELECT transaction_signature FROM BlockTransactions JOIN Blocks ON signature = block_signature WHERE height = ? AND sequence = ?", height,
				sequence)) {
			if (resultSet == null)
				return null;

			byte[] signature = resultSet.getBytes(1);

			return this.fromSignature(signature);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch transaction from repository", e);
		}
	}

	private TransactionData fromBase(TransactionType type, byte[] signature, byte[] reference, byte[] creatorPublicKey, long timestamp, BigDecimal fee)
			throws DataException {
		switch (type) {
			case GENESIS:
				return this.genesisTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case PAYMENT:
				return this.paymentTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case REGISTER_NAME:
				return this.registerNameTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case UPDATE_NAME:
				return this.updateNameTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case SELL_NAME:
				return this.sellNameTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CANCEL_SELL_NAME:
				return this.cancelSellNameTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case BUY_NAME:
				return this.buyNameTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CREATE_POLL:
				return this.createPollTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case VOTE_ON_POLL:
				return this.voteOnPollTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case ARBITRARY:
				return this.arbitraryTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case ISSUE_ASSET:
				return this.issueAssetTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case TRANSFER_ASSET:
				return this.transferAssetTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CREATE_ASSET_ORDER:
				return this.createOrderTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CANCEL_ASSET_ORDER:
				return this.cancelOrderTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case MULTIPAYMENT:
				return this.multiPaymentTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case DEPLOY_AT:
				return this.deployATTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case MESSAGE:
				return this.messageTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case AT:
				return this.atTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CREATE_GROUP:
				return this.createGroupTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case UPDATE_GROUP:
				return this.updateGroupTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case ADD_GROUP_ADMIN:
				return this.addGroupAdminTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case REMOVE_GROUP_ADMIN:
				return this.removeGroupAdminTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case GROUP_BAN:
				return this.groupBanTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case GROUP_UNBAN:
				return this.groupUnbanTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case GROUP_KICK:
				return this.groupKickTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case GROUP_INVITE:
				return this.groupInviteTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case CANCEL_GROUP_INVITE:
				return this.cancelGroupInviteTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case JOIN_GROUP:
				return this.joinGroupTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			case LEAVE_GROUP:
				return this.leaveGroupTransactionRepository.fromBase(signature, reference, creatorPublicKey, timestamp, fee);

			default:
				throw new DataException("Unsupported transaction type [" + type.name() + "] during fetch from HSQLDB repository");
		}
	}

	/**
	 * Returns payments associated with a transaction's signature.
	 * <p>
	 * Used by various transaction types, like Payment, MultiPayment, ArbitraryTransaction.
	 * 
	 * @param signature
	 * @return list of payments, empty if none found
	 * @throws DataException
	 */
	protected List<PaymentData> getPaymentsFromSignature(byte[] signature) throws DataException {
		List<PaymentData> payments = new ArrayList<PaymentData>();

		try (ResultSet resultSet = this.repository.checkedExecute("SELECT recipient, amount, asset_id FROM SharedTransactionPayments WHERE signature = ?",
				signature)) {
			if (resultSet == null)
				return payments;

			// NOTE: do-while because checkedExecute() above has already called rs.next() for us
			do {
				String recipient = resultSet.getString(1);
				BigDecimal amount = resultSet.getBigDecimal(2);
				long assetId = resultSet.getLong(3);

				payments.add(new PaymentData(recipient, assetId, amount));
			} while (resultSet.next());

			return payments;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch payments from repository", e);
		}
	}

	protected void savePayments(byte[] signature, List<PaymentData> payments) throws DataException {
		for (PaymentData paymentData : payments) {
			HSQLDBSaver saver = new HSQLDBSaver("SharedTransactionPayments");

			saver.bind("signature", signature).bind("recipient", paymentData.getRecipient()).bind("amount", paymentData.getAmount()).bind("asset_id",
					paymentData.getAssetId());

			try {
				saver.execute(this.repository);
			} catch (SQLException e) {
				throw new DataException("Unable to save payment into repository", e);
			}
		}
	}

	@Override
	public int getHeightFromSignature(byte[] signature) throws DataException {
		if (signature == null)
			return 0;

		// Fetch height using join via block's transactions
		try (ResultSet resultSet = this.repository.checkedExecute(
				"SELECT height from BlockTransactions JOIN Blocks ON Blocks.signature = BlockTransactions.block_signature WHERE transaction_signature = ? LIMIT 1",
				signature)) {

			if (resultSet == null)
				return 0;

			return resultSet.getInt(1);
		} catch (SQLException e) {
			throw new DataException("Unable to fetch transaction's height from repository", e);
		}
	}

	@Override
	public List<byte[]> getAllSignaturesInvolvingAddress(String address) throws DataException {
		List<byte[]> signatures = new ArrayList<byte[]>();

		try (ResultSet resultSet = this.repository.checkedExecute("SELECT signature FROM TransactionRecipients WHERE participant = ?", address)) {
			if (resultSet == null)
				return signatures;

			do {
				byte[] signature = resultSet.getBytes(1);

				signatures.add(signature);
			} while (resultSet.next());

			return signatures;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch involved transaction signatures from repository", e);
		}
	}

	@Override
	public void saveParticipants(TransactionData transactionData, List<String> participants) throws DataException {
		byte[] signature = transactionData.getSignature();

		try {
			for (String participant : participants) {
				HSQLDBSaver saver = new HSQLDBSaver("TransactionParticipants");

				saver.bind("signature", signature).bind("participant", participant);

				saver.execute(this.repository);
			}
		} catch (SQLException e) {
			throw new DataException("Unable to save transaction participant into repository", e);
		}
	}

	@Override
	public void deleteParticipants(TransactionData transactionData) throws DataException {
		try {
			this.repository.delete("TransactionParticipants", "signature = ?", transactionData.getSignature());
		} catch (SQLException e) {
			throw new DataException("Unable to delete transaction participants from repository", e);
		}
	}

	@Override
	public List<byte[]> getAllSignaturesMatchingCriteria(Integer startBlock, Integer blockLimit, TransactionType txType, String address) throws DataException {
		List<byte[]> signatures = new ArrayList<byte[]>();

		boolean hasAddress = address != null && !address.isEmpty();
		boolean hasTxType = txType != null;
		boolean hasHeightRange = startBlock != null || blockLimit != null;

		if (hasHeightRange && startBlock == null)
			startBlock = 1;

		String signatureColumn = "NULL";
		List<Object> bindParams = new ArrayList<Object>();
		String groupBy = "";

		// Table JOINs first
		List<String> tableJoins = new ArrayList<String>();

		// Always JOIN BlockTransactions as we only ever want confirmed transactions
		tableJoins.add("Blocks");
		tableJoins.add("BlockTransactions ON BlockTransactions.block_signature = Blocks.signature");
		signatureColumn = "BlockTransactions.transaction_signature";

		// Always JOIN Transactions as we want to order by timestamp
		tableJoins.add("Transactions ON Transactions.signature = BlockTransactions.transaction_signature");
		signatureColumn = "Transactions.signature";

		if (hasAddress) {
			tableJoins.add("TransactionParticipants ON TransactionParticipants.signature = Transactions.signature");
			signatureColumn = "TransactionParticipants.signature";
			groupBy = " GROUP BY TransactionParticipants.signature, Transactions.creation";
		}

		// WHERE clauses next
		List<String> whereClauses = new ArrayList<String>();

		if (hasHeightRange) {
			whereClauses.add("Blocks.height >= " + startBlock);

			if (blockLimit != null)
				whereClauses.add("Blocks.height < " + (startBlock + blockLimit));
		}

		if (hasTxType)
			whereClauses.add("Transactions.type = " + txType.value);

		if (hasAddress) {
			whereClauses.add("TransactionParticipants.participant = ?");
			bindParams.add(address);
		}

		String sql = "SELECT " + signatureColumn + " FROM " + String.join(" JOIN ", tableJoins) + " WHERE " + String.join(" AND ", whereClauses) + groupBy + " ORDER BY Transactions.creation ASC";
		LOGGER.trace(sql);

		try (ResultSet resultSet = this.repository.checkedExecute(sql, bindParams.toArray())) {
			if (resultSet == null)
				return signatures;

			do {
				byte[] signature = resultSet.getBytes(1);

				signatures.add(signature);
			} while (resultSet.next());

			return signatures;
		} catch (SQLException e) {
			throw new DataException("Unable to fetch matching transaction signatures from repository", e);
		}
	}

	@Override
	public List<TransactionData> getAllUnconfirmedTransactions() throws DataException {
		List<TransactionData> transactions = new ArrayList<TransactionData>();

		// Find transactions with no corresponding row in BlockTransactions
		try (ResultSet resultSet = this.repository.checkedExecute("SELECT signature FROM UnconfirmedTransactions ORDER BY creation ASC, signature ASC")) {
			if (resultSet == null)
				return transactions;

			do {
				byte[] signature = resultSet.getBytes(1);

				TransactionData transactionData = this.fromSignature(signature);

				if (transactionData == null)
					// Something inconsistent with the repository
					throw new DataException("Unable to fetch unconfirmed transaction from repository?");

				transactions.add(transactionData);
			} while (resultSet.next());

			return transactions;
		} catch (SQLException | DataException e) {
			throw new DataException("Unable to fetch unconfirmed transactions from repository", e);
		}
	}

	@Override
	public void confirmTransaction(byte[] signature) throws DataException {
		try {
			this.repository.delete("UnconfirmedTransactions", "signature = ?", signature);
		} catch (SQLException e) {
			throw new DataException("Unable to remove transaction from unconfirmed transactions repository", e);
		}
	}

	@Override
	public void unconfirmTransaction(TransactionData transactionData) throws DataException {
		HSQLDBSaver saver = new HSQLDBSaver("UnconfirmedTransactions");
		saver.bind("signature", transactionData.getSignature()).bind("creation", new Timestamp(transactionData.getTimestamp()));
		try {
			saver.execute(repository);
		} catch (SQLException e) {
			throw new DataException("Unable to add transaction to unconfirmed transactions repository", e);
		}
	}

	@Override
	public void save(TransactionData transactionData) throws DataException {
		HSQLDBSaver saver = new HSQLDBSaver("Transactions");
		saver.bind("signature", transactionData.getSignature()).bind("reference", transactionData.getReference()).bind("type", transactionData.getType().value)
				.bind("creator", transactionData.getCreatorPublicKey()).bind("creation", new Timestamp(transactionData.getTimestamp()))
				.bind("fee", transactionData.getFee()).bind("milestone_block", null);
		try {
			saver.execute(this.repository);
		} catch (SQLException e) {
			throw new DataException("Unable to save transaction into repository", e);
		}

		// Now call transaction-type-specific save() method
		switch (transactionData.getType()) {
			case GENESIS:
				this.genesisTransactionRepository.save(transactionData);
				break;

			case PAYMENT:
				this.paymentTransactionRepository.save(transactionData);
				break;

			case REGISTER_NAME:
				this.registerNameTransactionRepository.save(transactionData);
				break;

			case UPDATE_NAME:
				this.updateNameTransactionRepository.save(transactionData);
				break;

			case SELL_NAME:
				this.sellNameTransactionRepository.save(transactionData);
				break;

			case CANCEL_SELL_NAME:
				this.cancelSellNameTransactionRepository.save(transactionData);
				break;

			case BUY_NAME:
				this.buyNameTransactionRepository.save(transactionData);
				break;

			case CREATE_POLL:
				this.createPollTransactionRepository.save(transactionData);
				break;

			case VOTE_ON_POLL:
				this.voteOnPollTransactionRepository.save(transactionData);
				break;

			case ARBITRARY:
				this.arbitraryTransactionRepository.save(transactionData);
				break;

			case ISSUE_ASSET:
				this.issueAssetTransactionRepository.save(transactionData);
				break;

			case TRANSFER_ASSET:
				this.transferAssetTransactionRepository.save(transactionData);
				break;

			case CREATE_ASSET_ORDER:
				this.createOrderTransactionRepository.save(transactionData);
				break;

			case CANCEL_ASSET_ORDER:
				this.cancelOrderTransactionRepository.save(transactionData);
				break;

			case MULTIPAYMENT:
				this.multiPaymentTransactionRepository.save(transactionData);
				break;

			case DEPLOY_AT:
				this.deployATTransactionRepository.save(transactionData);
				break;

			case MESSAGE:
				this.messageTransactionRepository.save(transactionData);
				break;

			case AT:
				this.atTransactionRepository.save(transactionData);
				break;

			case CREATE_GROUP:
				this.createGroupTransactionRepository.save(transactionData);
				break;

			case UPDATE_GROUP:
				this.updateGroupTransactionRepository.save(transactionData);
				break;

			case ADD_GROUP_ADMIN:
				this.addGroupAdminTransactionRepository.save(transactionData);
				break;

			case REMOVE_GROUP_ADMIN:
				this.removeGroupAdminTransactionRepository.save(transactionData);
				break;

			case GROUP_BAN:
				this.groupBanTransactionRepository.save(transactionData);
				break;

			case GROUP_UNBAN:
				this.groupUnbanTransactionRepository.save(transactionData);
				break;

			case GROUP_KICK:
				this.groupKickTransactionRepository.save(transactionData);
				break;

			case GROUP_INVITE:
				this.groupInviteTransactionRepository.save(transactionData);
				break;

			case CANCEL_GROUP_INVITE:
				this.cancelGroupInviteTransactionRepository.save(transactionData);
				break;

			case JOIN_GROUP:
				this.joinGroupTransactionRepository.save(transactionData);
				break;

			case LEAVE_GROUP:
				this.leaveGroupTransactionRepository.save(transactionData);
				break;

			default:
				throw new DataException("Unsupported transaction type [" + transactionData.getType().name() + "] during save into HSQLDB repository");
		}
	}

	@Override
	public void delete(TransactionData transactionData) throws DataException {
		// NOTE: The corresponding row in sub-table is deleted automatically by the database thanks to "ON DELETE CASCADE" in the sub-table's FOREIGN KEY
		// definition.
		try {
			this.repository.delete("Transactions", "signature = ?", transactionData.getSignature());
		} catch (SQLException e) {
			throw new DataException("Unable to delete transaction from repository", e);
		}
		try {
			this.repository.delete("UnconfirmedTransactions", "signature = ?", transactionData.getSignature());
		} catch (SQLException e) {
			throw new DataException("Unable to remove transaction from unconfirmed transactions repository", e);
		}
	}

	@Override
	public List<GroupInviteTransactionData> getInvitesWithGroupReference(byte[] groupReference) throws DataException {
		// Let specialized subclass handle this
		return this.groupInviteTransactionRepository.getInvitesWithGroupReference(groupReference);
	}

}