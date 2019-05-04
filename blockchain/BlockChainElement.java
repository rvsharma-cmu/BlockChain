import lib.Block;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;


public class BlockChainElement implements BlockChainBase {
    private int difficulty;
    private List<Block> blockChain;
    private Node node;
    private byte[] newMinedBlockBytes;
    private Block newMinedBlock;

    public BlockChainElement() {
        blockChain = new ArrayList<Block>();
    }

    @Override
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Create the genesis block
     * @return Block the genesis block without previous hash
     */
    public Block createGenesisBlock() {
        long curTimestamp = System.currentTimeMillis() / 1000L;
        String curPreviousHash = "";

        // hardcode the sha256 hash (32 Bytes with 20 leading zero bits) and data
        String curData = "genesis";
        String curHash = "0";
        return new Block(curHash, curPreviousHash, curData, curTimestamp);
    }


    /**
     * Download the block chain from other nodes
     */
    public void downloadBlockchain() {
        int maxLength = Integer.MIN_VALUE;
        List<Block> curBlock = new ArrayList<Block>();
        // loop all the peers
        for (int i = 0; i < node.getPeerNumber(); i++) {
            // skip myself
            if (i == node.getId()) {
                continue;
            }
            // call the getBlockChainDataFromPeer and deserialize
            try {
                byte[] resBlockChainBytes = node.getBlockChainDataFromPeer(i);

                // bypass the type checking. better to use instanceof
                @SuppressWarnings("unchecked")
                List<Block> resBlockChain = (ArrayList<Block>) Block.buildBlock(resBlockChainBytes);
                // choose the longest chain
                if (resBlockChain.size() > maxLength) {
                    maxLength = resBlockChain.size();
                    curBlock = resBlockChain;
                } else if (resBlockChain.size() == maxLength && !curBlock.isEmpty()) {
                    // for block chains with the same length, choose the one with the earliest timestamp
                    long resTimestamp = resBlockChain.get(resBlockChain.size() - 1).getTimestamp();
                    long curTimestamp = curBlock.get(curBlock.size() - 1).getTimestamp();
                    if (resTimestamp < curTimestamp) {
                        curBlock = resBlockChain;
                    }
                }
            } catch (RemoteException e) {
                System.out.println("[Exception getBlockChainDataFromPeer()] on node: " + i);
                e.printStackTrace();
            }
        }
        this.blockChain = curBlock;
    }

    /**
     * Get the block chain data and serialize to byte array
     * @return byte[] the serialized block chain data
     */
    public byte[] getBlockchainData() {
        return Block.blockToData(this.blockChain);
    }

    public byte[] createNewBlock(String data) {
        String curPrevious = getLastBlock().getHash();
        long nonce = 0;
        String newHash;
        while (true) {
            String tmpString = curPrevious + String.valueOf(nonce) + data;
            //System.out.println("tmp string is " + tmpString);
            String tmpHash = sha256Hex(tmpString);
            if(tmpHash.startsWith("00000")) {
                newHash = tmpHash;
                //System.out.println("get the correct nonce: " + String.valueOf(nonce));
                break;
            }
            nonce++;
        }
        long curTimestamp = System.currentTimeMillis() / 1000L;
        //System.out.println("new hash is: " + newHash);
        //System.out.println("new block has nonce: " + String.valueOf(nonce));
        newMinedBlock = new Block(newHash, curPrevious, data, curTimestamp);
        newMinedBlock.setNonce(nonce);
        newMinedBlockBytes = Block.blockToData(newMinedBlock);
        return newMinedBlockBytes;
    }

    public boolean addBlock(Block block) {
        try {
            if (block.getData().equals("genesis")) {
                this.blockChain.add(block);
                return true;
            }
        } catch(Exception e) {
            System.out.println("catch add block exception at getData: " + node.getId());
            e.printStackTrace();
        }
        try{
            if (isValidNewBlock(block, getLastBlock())) {
                this.blockChain.add(block);
                return true;
            }
        } catch(Exception e) {
            System.out.println("catch add block exception at isValidNewBlock: " + node.getId());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the last block of the block chain.
     */
    public Block getLastBlock() {
        return this.blockChain.get(blockChain.size() - 1);
    }

    /**
     * Return the length of current block chain
     */
    public int getBlockChainLength() {
        System.out.println("current length of node " + node.getId() + " is " + this.blockChain.size());
        return this.blockChain.size();
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean isValidNewBlock(Block newBlock, Block prevBlock) {
        // check the previous hash
        if (!newBlock.getPreviousHash().equals(prevBlock.getHash())) {
            return false;
        }
        // check the correctness of hashcode
        String curString = newBlock.getPreviousHash() + String.valueOf(newBlock.getNonce()) + newBlock.getData();
        String curHash = sha256Hex(curString);
        if (!curHash.equals(newBlock.getHash()) || !curHash.startsWith("00000")) {
            return false;
        }
        // is valid
        return true;
    }

    public boolean broadcastNewBlock() {
        // send the new block to peers
        if(newMinedBlock == null) {
            return false;
        }
        int agreeCount = 0;
        for (int i = 0; i < node.getPeerNumber(); i++) {
            // skip myself
            if (i == node.getId()) {
                continue;
            }
            try {
                System.out.println("send new block to peer: " + i);
                if (node.broadcastNewBlockToPeer(i, newMinedBlockBytes)) {
                    agreeCount++;
                }
            } catch (RemoteException e) {
                System.out.println("[Remote exception in broadcastNewBlock, node: ]" + node.getId());
            }
        }
        if (agreeCount == node.getPeerNumber() - 1) {
            this.blockChain.add(newMinedBlock);
            return true;
        }
        return false;
    }

}
