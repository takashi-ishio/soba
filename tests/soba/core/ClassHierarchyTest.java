package soba.core;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.ClassHierarchy;
import soba.core.ClassInfo;
import soba.core.ClassHierarchy.FrozenHierarchyException;
import soba.core.method.FieldAccess;

public class ClassHierarchyTest implements ExampleProgram {

	private ClassHierarchy ch;

	// Class hierarchy
	//
	// C
	//  -> D
	//      -> H
	//  -> F
	//  -> G
	// E
	
	private static ClassInfo c;
	private static ClassInfo d;
	private static ClassInfo e;
	private static ClassInfo f;
	private static ClassInfo g;
	private static ClassInfo h;
	private static ClassInfo i;
	private static ClassInfo j;
	private static ClassInfo k;
	
	@BeforeClass
	public static void setUpClassInfo() throws IOException {
		c = new ClassInfo("C.class", new FileInputStream("bin/" + CLASS_C + ".class"));
		d = new ClassInfo("D.class", new FileInputStream("bin/" + CLASS_D + ".class"));
		e = new ClassInfo("E.class", new FileInputStream("bin/" + CLASS_E + ".class"));
		f = new ClassInfo("F.class", new FileInputStream("bin/" + CLASS_F + ".class"));
		g = new ClassInfo("G.class", new FileInputStream("bin/" + CLASS_G + ".class"));
		h = new ClassInfo("H.class", new FileInputStream("bin/" + CLASS_H + ".class"));
		i = new ClassInfo("I.class", new FileInputStream("bin/" + CLASS_I + ".class"));
		j = new ClassInfo("I.class", new FileInputStream("bin/" + CLASS_J + ".class"));
		k = new ClassInfo("I.class", new FileInputStream("bin/" + CLASS_K + ".class"));
	}
	
	@Before
	public void createHierarchy() {
		ch = new ClassHierarchy();
		ch.registerClass(c);
		ch.registerClass(d);
		ch.registerClass(e);
		ch.registerClass(f);
		ch.registerClass(g);
		ch.registerClass(h);
		ch.registerClass(i);
		ch.registerClass(j);
		ch.registerClass(k);
	}
	
	@Test
	public void testClasses() {
		assertThat(ch.getClassCount(), is(9));
		assertThat(ch.getClasses(), containsInAnyOrder(CLASS_C, CLASS_D, CLASS_E, CLASS_F, CLASS_G,
													   CLASS_H, CLASS_I, CLASS_J, CLASS_K));
		assertThat(ch.getRequestedClasses(), is(empty()));
	}
	
	@Test
	public void testGetClassInfo() {
		assertThat(ch.getClassInfo(CLASS_C), is(c));
		assertThat(ch.getClassInfo(CLASS_D), is(d));
		assertThat(ch.getClassInfo(CLASS_E), is(e));
		assertThat(ch.getClassInfo("pkg/Unknown"), is(nullValue()));
	}
	
	@Test
	public void testGetSuperClass() {
		assertThat(ch.getSuperClass(CLASS_D), is(CLASS_C));
		assertThat(ch.getSuperClass(CLASS_C), is("java/lang/Object"));
		assertThat(ch.getSuperClass(CLASS_E), is("java/lang/Object"));
		assertThat(ch.getSuperClass("pkg/Unknown"), is(nullValue()));
	}
	
	@Test
	public void testIsSamePackage() {
		assertThat(ch.isSamePackage(CLASS_C, CLASS_D), is(true));
		assertThat(ch.isSamePackage(CLASS_C, CLASS_F), is(false));
		assertThat(ch.isSamePackage(CLASS_C, "pkg/Unknown"), is(false));
		assertThat(ch.isSamePackage("pkg/Unknown", CLASS_D), is(false));
	}
	
	@Test
	public void testGetSubtypes() {
		Collection<String> subtypesOfC = ch.getSubtypes(CLASS_C);
		assertThat(subtypesOfC, containsInAnyOrder(CLASS_D, CLASS_F, CLASS_G));
		Collection<String> subtypesOfE = ch.getSubtypes(CLASS_E);
		assertThat(subtypesOfE, is(empty()));
		Collection<String> subtypesOfI = ch.getSubtypes(CLASS_I);
		assertThat(subtypesOfI, containsInAnyOrder(CLASS_D, CLASS_K));
	}
	
	@Test
	public void testGetAllSubtypes() {
		Set<String> typeNames = new HashSet<>();
		typeNames.add(CLASS_C);
		Collection<String> allSubtypesOfC = ch.getAllSubtypes(typeNames);
		assertThat(allSubtypesOfC, containsInAnyOrder(CLASS_C, CLASS_D, CLASS_H, CLASS_F, CLASS_G));

		typeNames.add(CLASS_I);
		Collection<String> allSubtypesOfCandI = ch.getAllSubtypes(typeNames);
		assertThat(allSubtypesOfCandI, containsInAnyOrder(CLASS_C, CLASS_D, CLASS_H, CLASS_F,
														  CLASS_G, CLASS_I, CLASS_K));
	}
	
	@Test
	public void testListAllSuperTypes() {
		Collection<String> supertypesOfH = ch.listAllSuperTypes(CLASS_H);
		assertThat(supertypesOfH, containsInAnyOrder(CLASS_C, CLASS_D, CLASS_K, CLASS_I, "java/lang/Object"));

		Collection<String> supertypesOfI = ch.listAllSuperTypes(CLASS_I);
		assertThat(supertypesOfI, containsInAnyOrder("java/lang/Object"));
	}
	
	@Test
	public void testGetSuperInterfaces() {
		Collection<String> superInterfaceOfH = ch.getSuperInterfaces(CLASS_D);
		assertThat(superInterfaceOfH, containsInAnyOrder(CLASS_I, CLASS_K));
		Collection<String> superInterfaceOfC = ch.getSuperInterfaces(CLASS_C);
		assertThat(superInterfaceOfC, is(empty()));
	}
	
	@Test
	public void testResolveCall01() {
		MethodInfo[] methodMain = ch.resolveCall(CLASS_E, "main", "([Ljava/lang/String;)V", false);
		checkClasses(methodMain, CLASS_E);
	}

	@Test
	public void testResolveCall02() {
		// C.n() has 4 implementation: C, D, G, H.  F.n() is different.
		MethodInfo[] methodsN = ch.resolveCall(CLASS_C, "n", "()V", true);
		checkClasses(methodsN, CLASS_C, CLASS_D, CLASS_G, CLASS_H);
		
		MethodInfo[] methodsNf = ch.resolveCall(CLASS_F, "n", "()V", true);
		checkClasses(methodsNf, CLASS_F);
	}
	
	@Test
	public void testResolveCall03() {
		// C.m() and D.m() are defined.  H.m() is not implemented. 
		MethodInfo[] methodsM = ch.resolveCall(CLASS_D, "m", "()V", true);
		checkClasses(methodsM, CLASS_D);

		MethodInfo[] methodsM2 = ch.resolveCall(CLASS_H, "m", "()V", true);
		checkClasses(methodsM2, CLASS_D);

		MethodInfo[] methodsM3 = ch.resolveCall(CLASS_C, "m", "()V", true);
		checkClasses(methodsM3, CLASS_C, CLASS_D);
	}
	
	@Test
	public void testResolveCall04() {
		// p() is defined by C and H, not by D.
		MethodInfo[] methodsPc = ch.resolveCall(CLASS_C, "p", "(I)V", true);
		checkClasses(methodsPc, CLASS_C, CLASS_H);
		MethodInfo[] methodsPd = ch.resolveCall(CLASS_C, "p", "(I)V", true);
		checkClasses(methodsPd, CLASS_C, CLASS_H);
		MethodInfo[] methodsPh = ch.resolveCall(CLASS_H, "p", "(I)V", true);
		checkClasses(methodsPh, CLASS_H);
	}
	
	@Test
	public void testResolveCall05() {
		// q() is defined by C and H but it is private.
		MethodInfo[] methodsQc = ch.resolveCall(CLASS_C, "q", "(D)V", true);
		checkClasses(methodsQc, CLASS_C);
		MethodInfo[] methodsQh = ch.resolveCall(CLASS_H, "q", "(D)V", true);
		checkClasses(methodsQh, CLASS_H);
	}
	
	@Test
	public void testResolveCall06() {
		MethodInfo[] methodsMi = ch.resolveCall(CLASS_I, "m", "()V", true);
		checkClasses(methodsMi, CLASS_D);
	}
	
	@Test
	public void testFields() {
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_D, "x", "I", false)).getClassName(), is(CLASS_C));
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_C, "x", "I", false)).getClassName(), is(CLASS_C));
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_H, "x", "I", false)).getClassName(), is(CLASS_H));
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_I, "x", "I", true)).getClassName(), is(CLASS_I));
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_J, "x", "I", true)).getClassName(), is(CLASS_J));
		assertThat(ch.resolveField(FieldAccess.createGetField(CLASS_K, "x", "I", true)).getClassName(), is(CLASS_I));
	}
	
	private void checkClasses(MethodInfo[] resolved, String... classNames) {
		List<String> classes = new ArrayList<>();
		for (MethodInfo m: resolved) {
			classes.add(m.getClassName());
		}
		assertThat(classes, containsInAnyOrder(classNames));
	}
	
	@Test
	public void testFreeze() {
		assertThat(ch.isFrozen(), is(false));
		ch.freeze();
		assertThat(ch.isFrozen(), is(true));
		try {
			ch.registerClass(f);
			fail();
		} catch (FrozenHierarchyException e) {
		}
		try {
			ch.registerInterfaces(CLASS_F, new ArrayList<String>());
			fail();
		} catch (FrozenHierarchyException e) {
		}
		try {
			ch.registerSuperClass(CLASS_C, "java/lang/Object");
			fail();
		} catch (FrozenHierarchyException e) {
		}
		try {
			ch.registerSubtype("pkg/NewChild", CLASS_C);
			fail();
		} catch (FrozenHierarchyException e) {
		}
	}
}
