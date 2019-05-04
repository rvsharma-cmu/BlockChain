import lib.Block;
import lib.BlockHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class BlockChainElement implements BlockChainBase {

	private Node currentNode;
	private List<Block> currentBlockChains;
	private String proofOfWorkPrefix;
	private int difficultyOfWork;
	private Block newBlock;
	private byte[] byteArrNewBlock;
	private int nodeId;

	public BlockChainElement(Node node_, int nodeId_) {

		currentNode = node_;
		nodeId = nodeId_;

		currentBlockChains = new ArrayList<Block>();
		currentBlockChains.add(createGenesisBlock());
	}

	public boolean addBlock(Block block) {

		if (block.getData().equals("genesisData")) {
			this.currentBlockChains.add(block);
			return true;
		} else {
			Block lastBlock = getLastBlock();
			if (!isValidNewBlock(block, lastBlock)) {
				return false;
			} else {
				this.currentBlockChains.add(block);
				return true;
			}
		}
	}

	/**
	 * Create the hardcoded first block in the chain.
	 * 
	 * @return the first block.
	 */
	public Block createGenesisBlock() {
		return new Block("0", "prevHash", "genesisData", System.currentTimeMillis() / 1000L);
	}

	/**
	 * Create a block based on the string data.
	 * 
	 * @param data the data contained in the block
	 * @return the byte representation of the block.
	 */
	public byte[] createNewBlock(String data) {
		if (proofOfWorkPrefix == null || proofOfWorkPrefix.isEmpty())
			setDifficulty(difficultyOfWork);
		Block lastBlock = getLastBlock();
		long initNonce;

		String previousHash = lastBlock.getHash();
		String currentBlockHash = "";

		for (initNonce = Long.MIN_VALUE; initNonce < Long.MAX_VALUE; initNonce++) {
			String newHashForData = DigestUtils.sha256Hex(previousHash + initNonce + data);
			if (!newHashForData.startsWith(proofOfWorkPrefix)) {
				continue;
			} else {
				currentBlockHash = newHashForData;
				break;
			}
		}
		newBlock = new Block(currentBlockHash, previousHash, data, System.currentTimeMillis() / 1000L);

		newBlock.setNonce(initNonce);
		byteArrNewBlock = BlockHelper.blockToData(newBlock);

		return byteArrNewBlock;
	}

	/**
	 * broadcast the new block to all the peer in the network
	 * 
	 * @return whether the node is added into the chain or not.
	 */
	public boolean broadcastNewBlock() {

		boolean added = false;

		int numberOfPeers = currentNode.getPeerNumber();
		int vote = 0;
		for (int i = 0; i < numberOfPeers; i++) {
			if (i != currentNode.getId()) {
				try {
					boolean broadcast = currentNode.broadcastNewBlockToPeer(i, byteArrNewBlock);
					if (broadcast)
						vote++;
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		if (numberOfPeers == vote + 1) {
			currentBlockChains.add(newBlock);
			added = true;
		}
		return added;
	}

	@Override
	public void setDifficulty(int difficulty) {
		this.difficultyOfWork = difficulty;

		if (difficulty <= 0) {
			throw new IllegalArgumentException("Wrong difficulty passed");
		}
		this.difficultyOfWork = difficulty;
		// System.out.println("difficulty is " + difficultyOfWork);
		// calculate Prefix now
		String prefix = "";
		for (int i = 0; i < difficulty / 4; i++) {
			prefix += "0";
		}
		this.proofOfWorkPrefix = prefix;
		//createGenesisBlock();
	}

	/**
	 * get the byte representation of the blockchain
	 * 
	 * @return the byte representation
	 */
	@Override
	public byte[] getBlockchainData() {

		byte[] output = BlockHelper.blockToData(this.currentBlockChains);
		return output;
	}

	/**
	 * Download the block chain from other nodes
	 */
	public void downloadBlockchain() {
		int numberOfPeers = currentNode.getPeerNumber();
		List<Block> curBlock = new ArrayList<Block>();

		int max = curBlock.size();

		for (int i = 0; i < numberOfPeers; i++) {
			if (i != currentNode.getId()) {
				try {
					List<Block> resBlockChain = BlockHelper.buildBlock(currentNode.getBlockChainDataFromPeer(i));
					if (resBlockChain.size() < max)
						continue;
					else if (resBlockChain.size() > max) {

						curBlock = resBlockChain;

						max = resBlockChain.size();
					} else {

						if (getTimeStamps(resBlockChain) < getTimeStamps(curBlock)) {
							curBlock = resBlockChain;
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		this.currentBlockChains = curBlock;
	}

	public void setNode(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("illegal argument while setting node");
		}

		this.currentNode = node;
	}

	/**
	 * Validate the new block based on the block before that.
	 * 
	 * @param newBlock  the new block
	 * @param prevBlock the block before that.
	 * @return valid or not
	 */
	@Override
	public boolean isValidNewBlock(Block newBlock, Block prevBlock) {
		/*
		 * To add a new block mined by others, the validity of this block needs to be
		 * verified. For a new block, it has to have the index right after the last
		 * block in the chain. Also, the previous hash value should be the same to the
		 * hash value contained in the chain. And the hash value contained in the new
		 * block itself should be correctly calculated. If there are multiple new blocks
		 * broadcasted, we want to choose the block containing the earliest timestamp.
		 */
		String prevBlockHash = prevBlock.getHash();
		String newBlockHash = newBlock.getHash();
		String newBlockPrevHash = newBlock.getPreviousHash();

		if (!newBlockPrevHash.equals(prevBlockHash)) {
			return false;
		}

		long newBlockNonce = newBlock.getNonce();

		String data = newBlock.getData();
		String curHash = DigestUtils.sha256Hex(newBlockPrevHash + String.valueOf(newBlockNonce) + data);

		if (!curHash.startsWith(proofOfWorkPrefix))
			return false;

		if (!curHash.equals(newBlockHash))
			return false;

		return true;
	}

	public Block getLastBlock() {

		if (currentBlockChains == null || currentBlockChains.size() == 0) {
			System.out.println("block chain is null or no element present");
			return null;
		}

		return this.currentBlockChains.get(currentBlockChains.size() - 1);
	}

	public int getBlockChainLength() {

		int length = currentBlockChains.size();
		return length;

	}

	/**
	 * @param block
	 * @return
	 */
	public long getTimeStamps(List<Block> block) {
		int size = block.size() - 1;
		return block.get(size).getTimestamp();
	}

}
