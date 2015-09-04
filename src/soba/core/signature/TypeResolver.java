package soba.core.signature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.signature.SignatureReader;


/**
 * A utility class to resolve a type name using TypeVisitor.
 */
public class TypeResolver {

	private static Map<String, String> types = null;
	
	/**
	 * Returns a readable text for a specified type descriptor.
	 * If failed to parse, return argument's typeDesc.
	 * @param typeDescriptor is a type descriptor of a single type.
	 * @return a type name corresponding to a specified descriptor.
	 */
	public static String getTypeName(String typeDesc) {
		if (typeDesc == null) { 
			return null;
		}
		if (types == null) {
			types = new ConcurrentHashMap<String, String>(4096);
		}
		if (types.containsKey(typeDesc)) {
			return types.get(typeDesc);
		} else {
			SignatureReader sig = new SignatureReader(typeDesc);
			TypeVisitor reader = new TypeVisitor();
			try {
				sig.acceptType(reader);
				types.put(typeDesc, reader.getTypeName());
				return reader.getTypeName();
			} catch(Exception e) {
				types.put(typeDesc, typeDesc);
				return typeDesc;
			}
		}
	}

}
