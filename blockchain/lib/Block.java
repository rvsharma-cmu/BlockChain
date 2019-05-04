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
public class Block implements Serializable{

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

    public Block() {}

    public Block(String hash, String previousHash, String data,
                 long timestamp) {
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

	public static Block fromString(String s){
        
    	Block result;
//    	if(s.isEmpty())
//    		return result; 
    	
    	byte[] bytes = s.getBytes();
		result = (Block) buildBlock(bytes);
        return result;
    }
    
    public static Object buildBlock (byte[] arr) {
        Object res = null;
        if (arr == null) {
            return res;
        }
        try {
            byte[] cur = Hex.decodeHex(new String(arr));
            ByteArrayInputStream bis = new ByteArrayInputStream(cur);
            ObjectInputStream ois = new ObjectInputStream(bis);
            res = ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (DecoderException e){
            e.printStackTrace();
        }
        return res;
    }
    
    public static byte[] blockToData(Object block) {
        byte[] res = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(block);
            res = Hex.encodeHexString(bos.toByteArray()).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    @Override
    public String toString() {
    	
    	String output = "";
    	byte[] result = blockToData(this);
    	output = result.toString();
    	return output;
    }

}
