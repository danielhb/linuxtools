/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.oprofile.core.tests;

import java.io.FileReader;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.oprofile.core.daemon.OpEvent;
import org.eclipse.linuxtools.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.oprofile.core.daemon.OpUnitMask;
import org.eclipse.linuxtools.oprofile.core.opxml.OprofileSAXHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class TestInfoParse extends TestCase {
	private static final String REL_PATH_TO_TEST_XML = "resources/test_info.xml"; //$NON-NLS-1$
	
	private OpInfo info; 

	
	public TestInfoParse() {
		super("test info parsers"); //$NON-NLS-1$
	}
	
	@Override
	protected void setUp() throws Exception {
		/* this code mostly taken from OpxmlRunner */
		XMLReader reader = null;
		info = new OpInfo();
		OprofileSAXHandler handler = OprofileSAXHandler.getInstance(info);
		
		// Create XMLReader
        SAXParserFactory factory = SAXParserFactory.newInstance();
		reader = factory.newSAXParser().getXMLReader();
		
		// Set content/error handlers
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		
		String filePath = FileLocator.toFileURL(FileLocator.find(CoreTestsPlugin.getDefault().getBundle(), new Path(REL_PATH_TO_TEST_XML), null)).getFile();
		reader.parse(new InputSource(new FileReader(filePath)));
	}
	
	public void testParse() {
		assertEquals("/var/lib/oprofile/samples/", info.getDefault(OpInfo.DEFAULT_SAMPLE_DIR)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/lock", info.getDefault(OpInfo.DEFAULT_LOCK_FILE)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/samples/oprofiled.log", info.getDefault(OpInfo.DEFAULT_LOG_FILE)); //$NON-NLS-1$
		assertEquals("/var/lib/oprofile/complete_dump", info.getDefault(OpInfo.DEFAULT_DUMP_STATUS)); //$NON-NLS-1$
		
		assertEquals((double)800, info.getCPUSpeed());
		assertEquals(2, info.getNrCounters());

		OpEvent[] ctr0_events = info.getEvents(0), ctr1_events = info.getEvents(1);
		assertEquals(3, ctr0_events.length);
		assertEquals(3, ctr1_events.length);
		
		OpEvent ctr0_e1 = ctr0_events[0], ctr0_e2 = ctr0_events[1], ctr0_e3 = ctr0_events[2], 
				ctr1_e1 = ctr1_events[0], ctr1_e2 = ctr1_events[1], ctr1_e3 = ctr1_events[2];
		//events must be ordered alphabetically
		assertEquals(6000, ctr0_e1.getMinCount());
		assertEquals(60, ctr0_e1.getNumber());
		assertEquals("CPU_CLK_UNHALTED", ctr0_e1.getText()); //$NON-NLS-1$
		assertEquals("Clock cycles when not halted", ctr0_e1.getTextDescription()); //$NON-NLS-1$
		assertEquals(500, ctr0_e2.getMinCount());
		assertEquals(8, ctr0_e2.getNumber());
		assertEquals("DTLB_MISSES", ctr0_e2.getText()); //$NON-NLS-1$
		assertEquals("DTLB miss events", ctr0_e2.getTextDescription()); //$NON-NLS-1$
		assertEquals(6000, ctr0_e3.getMinCount());
		assertEquals(192, ctr0_e3.getNumber());
		assertEquals("INST_RETIRED_ANY_P", ctr0_e3.getText()); //$NON-NLS-1$
		assertEquals("number of instructions retired", ctr0_e3.getTextDescription()); //$NON-NLS-1$
		assertEquals(500, ctr1_e1.getMinCount());
		assertEquals(58, ctr1_e1.getNumber());
		assertEquals("EIST_TRANS_ALL", ctr1_e1.getText()); //$NON-NLS-1$
		assertEquals("Intel(tm) Enhanced SpeedStep(r) Technology transitions", ctr1_e1.getTextDescription()); //$NON-NLS-1$
		assertEquals(500, ctr1_e2.getMinCount());
		assertEquals(38, ctr1_e2.getNumber());
		assertEquals("L2_LINES_OUT", ctr1_e2.getText()); //$NON-NLS-1$
		assertEquals("number of recovered lines from L2", ctr1_e2.getTextDescription()); //$NON-NLS-1$
		assertEquals(500, ctr1_e3.getMinCount());
		assertEquals(37, ctr1_e3.getNumber());
		assertEquals("L2_M_LINES_IN", ctr1_e3.getText()); //$NON-NLS-1$
		assertEquals("number of modified lines allocated in L2", ctr1_e3.getTextDescription()); //$NON-NLS-1$
		
		OpUnitMask ctr0_e1_mask = ctr0_e1.getUnitMask(), ctr0_e2_mask = ctr0_e2.getUnitMask(), 
					ctr0_e3_mask = ctr0_e3.getUnitMask(), ctr1_e1_mask = ctr1_e1.getUnitMask(), 
					ctr1_e2_mask = ctr1_e2.getUnitMask(), ctr1_e3_mask = ctr1_e3.getUnitMask();
		assertEquals(0, ctr0_e1_mask.getMaskValue());
		assertEquals(OpUnitMask.EXCLUSIVE, ctr0_e1_mask.getType());
		assertEquals(3, ctr0_e1_mask.numMasks());
		assertEquals(0, ctr0_e1_mask.getMaskFromIndex(0));
		assertEquals("Unhalted core cycles", ctr0_e1_mask.getText(0)); //$NON-NLS-1$
		assertEquals(1, ctr0_e1_mask.getMaskFromIndex(1));
		assertEquals("Unhalted bus cycles", ctr0_e1_mask.getText(1)); //$NON-NLS-1$
		assertEquals(2, ctr0_e1_mask.getMaskFromIndex(2));
		assertEquals("Unhalted bus cycles of this core while the other core is halted", ctr0_e1_mask.getText(2)); //$NON-NLS-1$

		assertEquals(15, ctr0_e2_mask.getMaskValue());
		assertEquals(OpUnitMask.BITMASK, ctr0_e2_mask.getType());
		assertEquals(4, ctr0_e2_mask.numMasks());
		assertEquals(1, ctr0_e2_mask.getMaskFromIndex(0));
		assertEquals("ANY	Memory accesses that missed the DTLB.", ctr0_e2_mask.getText(0)); //$NON-NLS-1$
		assertEquals(2, ctr0_e2_mask.getMaskFromIndex(1));
		assertEquals("MISS_LD	DTLB misses due to load operations.", ctr0_e2_mask.getText(1)); //$NON-NLS-1$
		assertEquals(4, ctr0_e2_mask.getMaskFromIndex(2));
		assertEquals("L0_MISS_LD L0 DTLB misses due to load operations.", ctr0_e2_mask.getText(2)); //$NON-NLS-1$
		assertEquals(8, ctr0_e2_mask.getMaskFromIndex(3));
		assertEquals("MISS_ST	TLB misses due to store operations.", ctr0_e2_mask.getText(3)); //$NON-NLS-1$

		assertEquals(0, ctr0_e3_mask.getMaskValue());
		assertEquals(OpUnitMask.MANDATORY, ctr0_e3_mask.getType());
		assertEquals(1, ctr0_e3_mask.numMasks());
		assertEquals(0, ctr0_e3_mask.getMaskFromIndex(0));
		assertEquals("No unit mask", ctr0_e3_mask.getText(0)); //$NON-NLS-1$

		assertEquals(0, ctr1_e1_mask.getMaskValue());
		assertEquals(OpUnitMask.INVALID, ctr1_e1_mask.getType());
		assertEquals(1, ctr1_e1_mask.numMasks());
		assertEquals(-1, ctr1_e1_mask.getMaskFromIndex(0));		//-1 because of invalid mask type
		assertEquals("No unit mask", ctr1_e1_mask.getText(0)); //$NON-NLS-1$
		
		assertEquals(112, ctr1_e2_mask.getMaskValue());
		assertEquals(OpUnitMask.BITMASK, ctr1_e2_mask.getType());
		assertEquals(5, ctr1_e2_mask.numMasks());
		assertEquals(192, ctr1_e2_mask.getMaskFromIndex(0));
		assertEquals("core: all cores", ctr1_e2_mask.getText(0)); //$NON-NLS-1$
		assertEquals(64, ctr1_e2_mask.getMaskFromIndex(1));
		assertEquals("core: this core", ctr1_e2_mask.getText(1)); //$NON-NLS-1$
		assertEquals(48, ctr1_e2_mask.getMaskFromIndex(2));
		assertEquals("prefetch: all inclusive", ctr1_e2_mask.getText(2)); //$NON-NLS-1$
		assertEquals(16, ctr1_e2_mask.getMaskFromIndex(3));
		assertEquals("prefetch: Hardware prefetch only", ctr1_e2_mask.getText(3)); //$NON-NLS-1$
		assertEquals(0, ctr1_e2_mask.getMaskFromIndex(4));
		assertEquals("prefetch: exclude hardware prefetch", ctr1_e2_mask.getText(4)); //$NON-NLS-1$
		
		assertEquals(64, ctr1_e3_mask.getMaskValue());
		assertEquals(OpUnitMask.EXCLUSIVE, ctr1_e3_mask.getType());
		assertEquals(2, ctr1_e3_mask.numMasks());
		assertEquals(192, ctr1_e3_mask.getMaskFromIndex(0));
		assertEquals("All cores", ctr1_e3_mask.getText(0)); //$NON-NLS-1$
		assertEquals(64, ctr1_e3_mask.getMaskFromIndex(1));
		assertEquals("This core", ctr1_e3_mask.getText(1)); //$NON-NLS-1$
	}
	
	public void testUnitMask() throws Exception {
		//test types of masks setting/unsetting
	}
}