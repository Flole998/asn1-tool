package com.yafred.asn1.generator.java;

import java.util.ArrayList;

import com.yafred.asn1.model.BitStringType;
import com.yafred.asn1.model.BooleanType;
import com.yafred.asn1.model.ChoiceType;
import com.yafred.asn1.model.Component;
import com.yafred.asn1.model.EnumeratedType;
import com.yafred.asn1.model.IntegerType;
import com.yafred.asn1.model.NamedNumber;
import com.yafred.asn1.model.NamedType;
import com.yafred.asn1.model.NullType;
import com.yafred.asn1.model.OctetStringType;
import com.yafred.asn1.model.RestrictedCharacterStringType;
import com.yafred.asn1.model.SequenceType;
import com.yafred.asn1.model.Tag;
import com.yafred.asn1.model.Type;
import com.yafred.asn1.model.TypeReference;

public class BERHelper {
	Generator generator;
	final static private String BER_READER = "com.yafred.asn1.runtime.BERReader";
	final static private String BER_WRITER = "com.yafred.asn1.runtime.BERWriter";

	public BERHelper(Generator generator) {
		this.generator = generator;
		
	}

	void processTypeAssignment(Type type, String className) throws Exception {
        ArrayList<Tag> tagList = Utils.getTagChain(type);
		
		// readPdu method
		generator.output.println("public static " + className + " readPdu(" + BER_READER
				+ " reader) throws Exception {");
		writeTagsDecode(type);
		String lengthText = "reader.getLengthValue()";

		if (tagList == null || tagList.size() == 0) { // it is an untagged CHOICE
			lengthText = "0";
		}

		generator.output.println(className + " ret = new " + className + "();");

		generator.output.println("ret.read(reader, " + lengthText + ");");
		generator.output.println("return ret;");
		generator.output.println("}");

		// writePdu method
		generator.output.println("public static void writePdu(" + className + " pdu, "
				+ BER_WRITER + " writer) throws Exception {");
        String lengthDeclaration = "";
        if (tagList != null && tagList.size() != 0) { // it is not an untagged CHOICE
            lengthDeclaration = "int length = ";
        }
        generator.output.println(lengthDeclaration + "pdu.write(writer);");
		writeTagsEncode(type);
		generator.output.println("writer.flush();");
		generator.output.println("}");
	}
	
	void processEnumeratedTypeAssignment(EnumeratedType enumeratedType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
            " writer) throws Exception {");
		generator.output.println("int intValue=-1;");
		generator.output.println("switch(getValue()) {");
		for(NamedNumber namedNumber : enumeratedType.getRootEnumeration()) {
			generator.output.println("case " + Utils.normalize(namedNumber.getName()) + ":");
			generator.output.println("intValue=" + namedNumber.getNumber() + ";");
			generator.output.println("break;");
		}
		if(enumeratedType.getAdditionalEnumeration() != null) {
			for(NamedNumber namedNumber : enumeratedType.getAdditionalEnumeration()) {
				generator.output.println("case " + Utils.normalize(namedNumber.getName()) + ":");
				generator.output.println("intValue=" + namedNumber.getNumber() + ";");
				generator.output.println("break;");
			}
		}
		generator.output.println("}");
		generator.output.println("return writer.writeInteger(intValue);");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
            " reader, int length) throws Exception {");
		generator.output.println("int intValue=reader.readInteger(length);");
		for(NamedNumber namedNumber : enumeratedType.getRootEnumeration()) {
			generator.output.println("if(intValue ==" + namedNumber.getNumber() + "){");
			generator.output.println("setValue(Enum." + Utils.normalize(namedNumber.getName()) + ");");
			generator.output.println("}");
		}
		if(enumeratedType.getAdditionalEnumeration() == null) {
			generator.output.println("if(null == getValue()){");
			generator.output.println("throw new Exception(\"Invalid enumeration value: \" + intValue);");
			generator.output.println("}");
		}
		else {
			for(NamedNumber namedNumber : enumeratedType.getAdditionalEnumeration()) {
				generator.output.println("if(intValue ==" + namedNumber.getNumber() + "){");
				generator.output.println("setValue(Enum." + Utils.normalize(namedNumber.getName()) + ");");
				generator.output.println("}");
			}
			generator.output.println("// Extensible: this.getValue() can return null if unknown enum value is decoded.");
		}
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(enumeratedType, className);
	}
	
	void processIntegerTypeAssignment(IntegerType integerType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
            " writer) throws Exception {");
		generator.output.println("return writer.writeInteger(this.getValue());");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
            " reader, int length) throws Exception {");
		generator.output.println("this.setValue(reader.readInteger(length));");
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(integerType, className);
	}

	void processBitStringTypeAssignment(BitStringType bitStringType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
            " writer) throws Exception {");
		generator.output.println("return writer.writeBitString(this.getValue());");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
            " reader, int length) throws Exception {");
		generator.output.println("this.setValue(reader.readBitString(length));");
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(bitStringType, className);
	}

	void processBooleanTypeAssignment(BooleanType booleanType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
	            " writer) throws Exception {");
		generator.output.println("return writer.writeBoolean(this.getValue());");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
	            " reader, int length) throws Exception {");
		generator.output.println("this.setValue(reader.readBoolean(length));");
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(booleanType, className);
	}
	
	void processOctetStringTypeAssignment(OctetStringType octetStringType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
	            " writer) throws Exception {");
		generator.output.println("return writer.writeOctetString(this.getValue());");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
	            " reader, int length) throws Exception {");
		generator.output.println("this.setValue(reader.readOctetString(length));");
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(octetStringType, className);
	}

	void processRestrictedCharacterStringTypeAssignment(RestrictedCharacterStringType restrictedCharacterStringType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
	            " writer) throws Exception {");
		generator.output.println("return writer.writeRestrictedCharacterString(this.getValue());");
		generator.output.println("}");

		// write decoding code
		generator.output.println("void read(" + BER_READER + " reader, int length) throws Exception {");
		generator.output.println("this.setValue(reader.readRestrictedCharacterString(length));");
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(restrictedCharacterStringType, className);
	}

	void processNullTypeAssignment(NullType nullType, String className) throws Exception {
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
	            " writer) throws Exception {");
		generator.output.println("return 0;");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER + " reader, int length) throws Exception {");
		generator.output.println("this.setValue(new java.lang.Object());"); // dummy value
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(nullType, className);
	}
		

	private void writeTagsEncode(Type type) throws Exception {
		writeTagsEncode(type, null);
	}
	
	private void writeTagsEncode(Type type, Tag automaticTag) throws Exception {
		ArrayList<Tag> tagList = Utils.getTagChain(type);
		if (tagList != null && tagList.size() != 0) { // it is not an untagged CHOICE
			if(automaticTag != null) {
				tagList.set(0, automaticTag);
			}
			for (int iTag = tagList.size() - 1; iTag >= 0; iTag--) {
				boolean isConstructedForm = true;

				if ((iTag == (tagList.size() - 1)) && !Utils.isConstructed(type)) {
					isConstructedForm = false;
				}

				TagHelper tagHelper = new TagHelper(tagList.get(iTag), !isConstructedForm);
				generator.output.println("length += writer.writeLength(length);");

				byte[] tagBytes = tagHelper.getByteArray();
				
				for(int i=tagBytes.length-1; i>=0; i--) {
					generator.output.println(
							"length += writer.writeByte((byte)" + tagBytes[i] + ");");
				}
				generator.output.println("/* " + tagHelper.toString() + " */");
			}
		}
	}

	private void writeTagsDecode(NamedType namedType, Tag automaticTag) throws Exception {
		ArrayList<Tag> tagList = Utils.getTagChain(namedType.getType());
		if (tagList == null || tagList.size() == 0) { // it is a untagged CHOICE
			return;
		}
		if(automaticTag != null) {
			tagList.set(0, automaticTag);
		}

		for (int iTag = 0; iTag < tagList.size(); iTag++) {
			boolean isConstructedForm = true;

			if ((iTag == (tagList.size() - 1)) && !Utils.isConstructed(namedType.getType())) {
				isConstructedForm = false;
			}

			TagHelper tagHelper = new TagHelper(tagList.get(iTag), !isConstructedForm);
			generator.output.println("/* matching " + tagHelper.toString() + " */");
			byte[] tagBytes = tagHelper.getByteArray();
			String tagBytesAsString = "new byte[] {";
			for(int i=0; i<tagBytes.length; i++) {
				if(i!=0) {
					tagBytesAsString += ",";
				}
				tagBytesAsString += tagBytes[i];
			}
			tagBytesAsString += "}";
			
			if(iTag == 0) {
				if(namedType.isOptional()) {
					// we could test if this is a potential end
					generator.output.println("if(totalLength==-1 && reader.matchTag(new byte[]{0})) {");
					generator.output.println("reader.readTag();");
					generator.output.println("reader.mustMatchTag(new byte[]{0});");
					generator.output.println("return;");
					generator.output.println("}");
					generator.output.println("matchedPrevious= reader.matchTag(" + tagBytesAsString + ");");
					generator.output.println("if(matchedPrevious){");
					generator.output.println("reader.readLength();");
					generator.output.println("if(totalLength!=-1) totalLength-=reader.getLengthLength();");
					generator.output.println("}");
				}
				else {
					generator.output.println("reader.mustMatchTag(" + tagBytesAsString + ");");					
					generator.output.println("reader.readLength();");
					generator.output.println("if(totalLength!=-1) totalLength-=reader.getLengthLength();");
				}
			}
			else {
				generator.output.println("reader.mustMatchTag(" + tagBytesAsString + ");");
				generator.output.println("reader.readLength();");
				generator.output.println("if(totalLength!=-1) totalLength-=reader.getLengthLength();");
			}
		}
	}

	private void writeTagsDecode(Type type) throws Exception {
		ArrayList<Tag> tagList = Utils.getTagChain(type);
		if (tagList != null && tagList.size() != 0) { // it is not an untagged CHOICE
			for (int iTag = 0; iTag < tagList.size(); iTag++) {
				boolean isConstructedForm = true;

				if ((iTag == (tagList.size() - 1)) && !Utils.isConstructed(type)) {
					isConstructedForm = false;
				}

				TagHelper tagHelper = new TagHelper(tagList.get(iTag), !isConstructedForm);
				generator.output.println("/* " + tagHelper.toString() + " */");
				byte[] tagBytes = tagHelper.getByteArray();
				
				String tagBytesAsString = "" + tagBytes[0];
				
				for(int i=1; i<tagBytes.length; i++) {
					tagBytesAsString += "," + tagBytes[i];
				}
				generator.output.println("reader.readTag();");
				generator.output.println(
						"reader.mustMatchTag(new byte[] {" + tagBytesAsString + "});");
				
				generator.output.println("reader.readLength();");
			}
		}
	}
	
	void processSequenceTypeAssignment(SequenceType sequenceType, String className) throws Exception {
		ArrayList<Component> componentList = new ArrayList<Component>();
		Utils.addAllIfNotNull(componentList, sequenceType.getRootComponentList());
		Utils.addAllIfNotNull(componentList, sequenceType.getExtensionComponentList());
		Utils.addAllIfNotNull(componentList, sequenceType.getAdditionalComponentList());
		
		if(componentList.size() == 0) return;
		
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
            " writer) throws Exception {");
		generator.output.println("int totalLength=0;");
		for(int componentIndex = componentList.size()-1; componentIndex >= 0; componentIndex--) {
			Component component = componentList.get(componentIndex);
			if(!component.isNamedType()) throw new Exception("Component can only be a NamedType here");
			NamedType namedType = (NamedType)component;
			String componentName = Utils.normalize(namedType.getName());
			String componentClassName = Utils.uNormalize(namedType.getName());
			if(namedType.getType().isTypeReference()) {
				TypeReference typeReference = (TypeReference)namedType.getType();
				componentClassName = Utils.uNormalize(typeReference.getName());
			}
			generator.output.println("if(" + componentName + "!=null){");
			generator.output.print("int length=0;");
			switchEncodeComponent(namedType, componentName, componentClassName);
			Tag automaticTag = null;
			if(sequenceType.isAutomaticTaggingSelected()) {
				automaticTag = new Tag(new Integer(componentIndex), null, null);
			}
			writeTagsEncode(namedType.getType(), automaticTag);
			generator.output.println("totalLength+=length;");
			generator.output.println("}");
		}
		generator.output.println("return totalLength;");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
            " reader, int totalLength) throws Exception {");
		generator.output.println("boolean matchedPrevious=true;");
		generator.output.println("int componentLength=0;");
		for(int componentIndex = 0; componentIndex < componentList.size(); componentIndex++) {
			generator.output.println("if(totalLength==0) return;"); 			
			if(componentIndex != 0) {
				generator.output.println("if(matchedPrevious){");
			}
			generator.output.println("reader.readTag();");
			generator.output.println("if(totalLength!=-1) totalLength-=reader.getTagLength();");
			if(componentIndex != 0) {
				generator.output.println("}");
			}

			Component component = componentList.get(componentIndex);
			if(!component.isNamedType()) throw new Exception("Component can only be a NamedType here");
			NamedType namedType = (NamedType)component;
			String componentName = Utils.normalize(namedType.getName());
			String componentClassName = Utils.uNormalize(namedType.getName());
			if(namedType.getType().isTypeReference()) {
				TypeReference typeReference = (TypeReference)namedType.getType();
				componentClassName = Utils.uNormalize(typeReference.getName());
			}
			Tag automaticTag = null;
			if(sequenceType.isAutomaticTaggingSelected()) {
				automaticTag = new Tag(new Integer(componentIndex), null, null);
			}
			writeTagsDecode(namedType, automaticTag);
			
			switchDecodeComponent(namedType, componentName, componentClassName);
		}
		
		generator.output.println("if(totalLength==-1) {");
		generator.output.println("reader.readTag();");
		generator.output.println("reader.mustMatchTag(new byte[]{0});");
		generator.output.println("reader.readTag();");
		generator.output.println("reader.mustMatchTag(new byte[]{0});");
		generator.output.println("}");

		generator.output.println("else if(totalLength!=0) throw new Exception(\"length should be 0, not \" + totalLength);"); 
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(sequenceType, className);
	}
	
	void processChoiceTypeAssignment(ChoiceType choiceType, String className) throws Exception {
		ArrayList<Component> componentList = new ArrayList<Component>();
		Utils.addAllIfNotNull(componentList, choiceType.getRootAlternativeList());
		Utils.addAllIfNotNull(componentList, choiceType.getAdditionalAlternativeList());
		
	    // write encoding code
		generator.output.println("int write(" + BER_WRITER +
            " writer) throws Exception {");
		for(int componentIndex = componentList.size()-1; componentIndex >= 0; componentIndex--) {
			Component component = componentList.get(componentIndex);
			if(!component.isNamedType()) throw new Exception("Component can only be a NamedType here");
			NamedType namedType = (NamedType)component;
			String componentName = Utils.normalize(namedType.getName());
			String componentClassName = Utils.uNormalize(namedType.getName());
			if(namedType.getType().isTypeReference()) {
				TypeReference typeReference = (TypeReference)namedType.getType();
				componentClassName = Utils.uNormalize(typeReference.getName());
			}
			generator.output.println("if(" + componentName + "!=null){");
			generator.output.print("int length=0;");
			switchEncodeComponent(namedType, componentName, componentClassName);
			Tag automaticTag = null;
			if(choiceType.isAutomaticTaggingSelected()) {
				automaticTag = new Tag(new Integer(componentIndex), null, null);
			}
			writeTagsEncode(namedType.getType(), automaticTag);
			generator.output.println("return length;");
			generator.output.println("}");
		}
		generator.output.println("return 0;");
		generator.output.println("}");

        // write decoding code
		generator.output.println("void read(" + BER_READER +
            " reader, int totalLength) throws Exception {");
		generator.output.println("boolean matchedPrevious=false;");
		generator.output.println("int componentLength=0;");
		generator.output.println("reader.readTag();");
		for(int componentIndex = 0; componentIndex < componentList.size(); componentIndex++) {
			Component component = componentList.get(componentIndex);
			if(!component.isNamedType()) throw new Exception("Component can only be a NamedType here");
			NamedType namedType = (NamedType)component;
			String componentName = Utils.normalize(namedType.getName());
			String componentClassName = Utils.uNormalize(namedType.getName());
			if(namedType.getType().isTypeReference()) {
				TypeReference typeReference = (TypeReference)namedType.getType();
				componentClassName = Utils.uNormalize(typeReference.getName());
			}
			Tag automaticTag = null;
			if(choiceType.isAutomaticTaggingSelected()) {
				automaticTag = new Tag(new Integer(componentIndex), null, null);
			}
			namedType.setOptional(true); // force optional
			writeTagsDecode(namedType, automaticTag);
			
			switchDecodeComponent(namedType, componentName, componentClassName);
			
			generator.output.println("if(matchedPrevious) return;");
		}
		
		generator.output.println("}");
		
		// pdu methods
		processTypeAssignment(choiceType, className);
	}
	
	void switchEncodeComponent(NamedType namedType, String componentName, String componentClassName) throws Exception {
		if(namedType.getType().isRestrictedCharacterStringType()) {
			generator.output.println("length=writer.writeRestrictedCharacterString(this." +  componentName + ");");			
		}
		else if(namedType.getType().isIntegerType()) {
			generator.output.println("length=writer.writeInteger(this." +  componentName + ");");			
		}
		else if(namedType.getType().isBooleanType()) {
			generator.output.println("length=writer.writeBoolean(this." +  componentName + ");");			
		}	
		else if(namedType.getType().isBitStringType()) {
			generator.output.println("length=writer.writeBitString(this." +  componentName + ");");			
		}
		else if(namedType.getType().isOctetStringType()) {
			generator.output.println("length=writer.writeOctetString(this." +  componentName + ");");			
		}
		else if(namedType.getType().isEnumeratedType()) {
			generator.output.println("int intValue=-1;");
			generator.output.println("switch(this." +  componentName + ") {");
			EnumeratedType enumeratedType = (EnumeratedType)namedType.getType();
			for(NamedNumber namedNumber : enumeratedType.getRootEnumeration()) {
				generator.output.println("case " + Utils.normalize(namedNumber.getName()) + ":");
				generator.output.println("intValue=" + namedNumber.getNumber() + ";");
				generator.output.println("break;");
			}
			if(enumeratedType.getAdditionalEnumeration() != null) {
				for(NamedNumber namedNumber : enumeratedType.getAdditionalEnumeration()) {
					generator.output.println("case " + Utils.normalize(namedNumber.getName()) + ":");
					generator.output.println("intValue=" + namedNumber.getNumber() + ";");
					generator.output.println("break;");
				}
			}
			generator.output.println("}");
			generator.output.println("length=writer.writeInteger(intValue);");			
		}
		else if(namedType.getType().isTypeReference()) {
			generator.output.println("length=this." + componentName + ".write(writer);");		
		}
	}
	
	void switchDecodeComponent(NamedType namedType, String componentName, String componentClassName) throws Exception {
		generator.output.println("componentLength=reader.getLengthValue();");
		if(namedType.isOptional()) {
			generator.output.println("if(matchedPrevious){");
		}
		
		if(namedType.getType().isRestrictedCharacterStringType()) {
			generator.output.println("this." + componentName + "=" + "reader.readRestrictedCharacterString(componentLength);");
		}
		else if(namedType.getType().isIntegerType()) {
			generator.output.println("this." + componentName + "=" + "reader.readInteger(componentLength);");
		}
		else if(namedType.getType().isBooleanType()) {
			generator.output.println("this." + componentName + "=" + "reader.readBoolean(componentLength);");
		}	
		else if(namedType.getType().isBitStringType()) {
			generator.output.println("this." + componentName + "=" + "reader.readBitString(componentLength);");
		}
		else if(namedType.getType().isOctetStringType()) {
			generator.output.println("this." + componentName + "=" + "reader.readOctetString(componentLength);");
		}
		else if(namedType.getType().isEnumeratedType()) {
			EnumeratedType enumeratedType = (EnumeratedType)namedType.getType();
			generator.output.println("int intValue=reader.readInteger(componentLength);");
			for(NamedNumber namedNumber : enumeratedType.getRootEnumeration()) {
				generator.output.println("if(intValue ==" + namedNumber.getNumber() + "){");
				generator.output.println("this." + componentName + "=" + componentClassName + "." + Utils.normalize(namedNumber.getName()) + ";");
				generator.output.println("}");
			}
			if(enumeratedType.getAdditionalEnumeration() == null) {
				generator.output.println("if(this." + componentName + "==null){");
				generator.output.println("throw new Exception(\"Invalid enumeration value: \" + intValue);");
				generator.output.println("}");
			}
			else {
				for(NamedNumber namedNumber : enumeratedType.getAdditionalEnumeration()) {
					generator.output.println("if(intValue ==" + namedNumber.getNumber() + "){");
					generator.output.println("this." + componentName + "=" + componentClassName + "." + Utils.normalize(namedNumber.getName()) + ";");
					generator.output.println("}");
				}
				generator.output.println("// Extensible: this.getValue() can return null if unknown enum value is decoded.");
			}
		}
		else if(namedType.getType().isTypeReference()) {
			generator.output.println("this." + componentName + "=new " + componentClassName + "();");
			generator.output.println("this." + componentName + ".read(reader, componentLength);");		
		}
		
		generator.output.println("if(totalLength!=-1) totalLength-=componentLength;");
		if(namedType.isOptional()) {
			generator.output.println("}");
		}
		else {
			generator.output.println("matchedPrevious=true;");			
		}

	}
}
