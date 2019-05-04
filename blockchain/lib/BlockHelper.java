package lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class BlockHelper {
	public static Block buildBlockFromArray (byte[] byteArray) {
        Block block = null;
        
        try {
            byte[] cur = Hex.decodeHex(new String(byteArray));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cur));
            block = (Block) ois.readObject();
        } catch (DecoderException e){
        	System.out.println("invalid data, or characters outside of the expected range");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	System.out.println("Class cannot be found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO error occured while reading the input stream");
			e.printStackTrace();
		}
        return block;
    }
	
	public static byte[] blockToData (Object block) {
        String hexRepresentation = "";
        
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objOutputStream.writeObject(block);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
			hexRepresentation = Hex.encodeHexString(byteArray);
			
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hexRepresentation.getBytes();
    }
    
    @SuppressWarnings("unchecked")
	public static List<Block> buildBlock(byte[] byteArray){

        List<Block> output = null;
        try {
            byte[] cur = Hex.decodeHex(new String(byteArray));
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(cur));
            output = (List<Block>) ois.readObject();
        } catch (DecoderException e){
        	System.out.println("invalid data, or characters outside of the expected range");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
        	System.out.println("Class cannot be found");
            e.printStackTrace();
        } catch (IOException e) {
        	System.out.println("IO error occured while reading the input stream");
            e.printStackTrace();
        }
        return output;
    }

}
