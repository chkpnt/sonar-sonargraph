/*
 * Sonar Sonargraph Plugin
 * Copyright (C) 2009, 2010, 2011 hello2morrow GmbH
 * mailto: info AT hello2morrow DOT com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hello2morrow.sonarplugin.foundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;

import com.hello2morrow.sonarplugin.metric.SonargraphSimpleMetrics;
import com.hello2morrow.sonarplugin.xsd.XsdAttribute;
import com.hello2morrow.sonarplugin.xsd.XsdAttributeRoot;
import com.hello2morrow.sonarplugin.xsd.XsdCycleGroup;
import com.hello2morrow.sonarplugin.xsd.XsdWarning;

public class UtilitiesTest {

  @Test
  public void testGetAttribute() {
    XsdAttributeRoot attributeRoot = new XsdAttributeRoot();
    
    List<XsdAttribute> list = attributeRoot.getAttribute();
    Utilities.addAttributeToList(list, "Path", "c:/users/test");
    Utilities.addAttributeToList(list, "Sonargraph Product", "Sonargraph 7.1.3");
    Utilities.addAttributeToList(list, "Sonargraph User", "Mr. Hubert Hudson");
    
    assertEquals("c:/users/test", Utilities.getAttribute(list, "Path"));
    assertNull(Utilities.getAttribute(list, "Not existent"));
  }


  @Test
  public void testGetBuildUnitNameString() {
    String name = null;
    assertEquals("<UNKNOWN>", Utilities.getBuildUnitName(name));
    assertEquals("<UNKNOWN>", Utilities.getBuildUnitName("AlarmClock-AlarmClock"));
    assertEquals("Old Sonarj Version", Utilities.getBuildUnitName("Old Sonarj Version::(Default Build Unit)"));
    assertEquals("AlarmClock", Utilities.getBuildUnitName("AlarmClock::AlarmClock"));
    assertEquals("AlarmClock", Utilities.getBuildUnitName("AlarmClock2::AlarmClock"));
  }

  @Test
  public void testGenerateSpaceEntity() {
    assertEquals("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Utilities.generateSpaceEntity(5));
  }
  
  @Test
  public void testGenerateSpaces() {
    assertEquals("     ", Utilities.generateSpaces(5));
  }
  
  @Test
  public void testRelativeFileNameToFqName() {
    assertEquals("com.h2m.alarm.presentation.AlarmToConsole",
        Utilities.relativeFileNameToFqName("com/h2m/alarm/presentation/AlarmToConsole.java"));
  }

  @Test
  public void testCreateDuplicateCodeBlock() {
    XsdWarning warning = new XsdWarning();
    Utilities.addAttributeToList(warning.getAttribute(), "Project", "AlarmClock");
    Utilities.addAttributeToList(warning.getAttribute(), "Build unit", "AlarmClock::AlarmClock");
    Utilities.addAttributeToList(warning.getAttribute(), "Element type", "Source file");
    Utilities.addAttributeToList(warning.getAttribute(), "Element", "com/h2m/alarm/presentation/AlarmToConsole.java");
    Utilities.addAttributeToList(warning.getAttribute(), "Attribute value", "37 lines");
    Utilities.addAttributeToList(warning.getAttribute(), "Start line", "11");
    Utilities.addAttributeToList(warning.getAttribute(), "Block id", "0");

    DuplicateCodeBlock block = Utilities.createDuplicateCodeBlock(warning);

    assertEquals(block.getProjectName(), "AlarmClock");
    assertEquals(block.getBuildUnitName(), "AlarmClock::AlarmClock");
    assertEquals(block.getElementType(), "Source file");
    assertEquals(block.getElementName(), "com/h2m/alarm/presentation/AlarmToConsole.java");
    assertEquals(block.getBlockLength(), 37);
    assertEquals(block.getStartLine(), 11);
    assertEquals(block.getBlockId(), 0);
  }

  @Test
  public void testGenerateDuplicateCodeBlockMessage() {
    List<DuplicateCodeBlock> blocks = createCodeBlocks();
    String message = Utilities.generateDuplicateCodeBlockMessage(blocks.get(0), blocks);
    String expected = "Line 10 to 51 is a duplicate of\n" + "line 4 to 44 of com/h2m/common/observer/T1.java and\n"
        + "line 10 to 50 of com/h2m/alarm/model/AlarmClock.java.";
    assertEquals(expected, message);
  }

  @Test
  public void testIsRootParentProject() {
    assertFalse(Utilities.isRootParentProject(null));
    
    Project singleProject = new Project("Test", null, "Test");
    assertFalse(Utilities.isRootParentProject(singleProject));
    
    
    Project parentProject = new Project("Parent", null, "Parent");
    Project module = new Project("Module", null, "Module");
    module.setParent(parentProject);
    assertTrue(Utilities.isRootParentProject(parentProject));
    assertFalse(Utilities.isRootParentProject(module));
    
    Project parentRoot = new Project("Parent", null, "Parent");
    singleProject.setParent(parentRoot);
    
    assertFalse(Utilities.isRootParentProject(singleProject));
    assertTrue(Utilities.isRootParentProject(parentRoot));
  }
  
  @Test
  public void testIsAggregationProject()
  {
    DecoratorContext context = mock(DecoratorContext.class);
    Collection<Measure> measures = new ArrayList<Measure>(2);
    measures.add(new Measure(SonargraphSimpleMetrics.ACD, 2.0, 1));
    measures.add(new Measure(SonargraphSimpleMetrics.ACD, 3.4, 1));
    when(context.getChildrenMeasures(SonargraphSimpleMetrics.ACD)).thenReturn(measures);
    assertTrue(Utilities.isAggregationProject(context, SonargraphSimpleMetrics.ACD));
    
    when(context.getChildrenMeasures(SonargraphSimpleMetrics.ALL_WARNINGS)).thenReturn(new ArrayList<Measure>(0));
    assertFalse(Utilities.isAggregationProject(context, SonargraphSimpleMetrics.ALL_WARNINGS));
  }
  
  @Test
  public void testGetBuildUnitNameCycleGroup()
  {
    XsdCycleGroup group = mock(XsdCycleGroup.class);
    when(group.getParent()).thenReturn(Utilities.DEFAULT_BUILD_UNIT);
    String projectName = "Alarm-Clock";
    when(group.getElementScope()).thenReturn(projectName);
    assertEquals(projectName, Utilities.getBuildUnitName(group));
    
    XsdCycleGroup group2 = mock(XsdCycleGroup.class);
    String buildUnitName = "Alarm-Clock";
    when(group2.getParent()).thenReturn(buildUnitName);
    assertEquals(buildUnitName, Utilities.getBuildUnitName(group));
  }

  @Test
  public void testIsSingleModuleProject()
  {
    assertFalse(Utilities.isSingleModuleProject(null));
    
    Project project = new Project("Test", null, "Test");
    assertTrue(Utilities.isSingleModuleProject(project));
    
    Project module = new Project("Module");
    module.setParent(project);
    assertFalse(Utilities.isSingleModuleProject(project));
  }
  
  @Test
  public void testBuildUnitMatchesAnalyzedProject()
  {
    String projectKeyWithBranchTag = "com.hello2morrow:sonargraph.core:architect-branch_7.1.8";
    String buildUnitName = "sonargraph.core";
    assertTrue(Utilities.buildUnitMatchesAnalyzedProject(buildUnitName, projectKeyWithBranchTag));
    
    String projectKey = "com.hello2morrow:sonargraph.core";
    assertTrue(Utilities.buildUnitMatchesAnalyzedProject(buildUnitName, projectKey));
    
    projectKey = "sonargraph.core";
    assertTrue(Utilities.buildUnitMatchesAnalyzedProject(buildUnitName, projectKey));
  }
  
  private List<DuplicateCodeBlock> createCodeBlocks() {
    List<DuplicateCodeBlock> list = new ArrayList<DuplicateCodeBlock>();

    DuplicateCodeBlock block1 = new DuplicateCodeBlock();
    block1.setBlockId(1);
    block1.setBlockLength(42);
    block1.setElementName("com/h2m/alarm/presentation/AlarmHandler.java");
    block1.setStartLine(10);
    list.add(block1);

    DuplicateCodeBlock block2 = new DuplicateCodeBlock();
    block2.setBlockId(1);
    block2.setBlockLength(41);
    block2.setElementName("com/h2m/common/observer/T1.java");
    block2.setStartLine(4);
    list.add(block2);

    DuplicateCodeBlock block3 = new DuplicateCodeBlock();
    block3.setBlockId(1);
    block3.setBlockLength(41);
    block3.setElementName("com/h2m/alarm/model/AlarmClock.java");
    block3.setStartLine(10);
    list.add(block3);

    return list;
  }
  
  

}
