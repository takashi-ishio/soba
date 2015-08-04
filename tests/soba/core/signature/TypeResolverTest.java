package soba.core.signature;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class TypeResolverTest {

	@Test
	public void testGetTypeName() {
		assertThat(TypeResolver.getTypeName("Z"), is("boolean"));
		assertThat(TypeResolver.getTypeName("B"), is("byte"));
		assertThat(TypeResolver.getTypeName("C"), is("char"));
		assertThat(TypeResolver.getTypeName("S"), is("short"));
		assertThat(TypeResolver.getTypeName("I"), is("int"));
		assertThat(TypeResolver.getTypeName("J"), is("long"));
		assertThat(TypeResolver.getTypeName("F"), is("float"));
		assertThat(TypeResolver.getTypeName("D"), is("double"));
		assertThat(TypeResolver.getTypeName("V"), is("void"));
		assertThat(TypeResolver.getTypeName("Ljava/lang/String;"), is("java/lang/String"));
		assertThat(TypeResolver.getTypeName("[I"), is("int[]"));
		assertThat(TypeResolver.getTypeName("[[[I"), is("int[][][]"));
	}

}
