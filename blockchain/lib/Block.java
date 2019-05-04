package lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Block Class, the element to compose a Blockchain.
 */
public class Block implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String hash;

	private String previousHash;

	private String data;

	private long timestamp;

	private int difficulty;

	private long nonce;

	private int blockPosition;

	public Block() {
	}

	public Block(String hash, String previousHash, String data, long timestamp) {
		this.hash = hash;
		this.previousHash = previousHash;
		this.data = data;
		this.timestamp = timestamp;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getData() {
		return data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the blockPosition
	 */
	public int getBlockPosition() {
		return blockPosition;
	}

	/**
	 * @param blockPosition the blockPosition to set
	 */
	public void setBlockPosition(int blockPosition) {
		this.blockPosition = blockPosition;
	}

	public static Block fromString(String s) {
		return (Block) BlockHelper.buildBlockFromArray(s.getBytes());
	}

}
