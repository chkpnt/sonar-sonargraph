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
/**
 *
 */
package com.hello2morrow.sonarplugin.foundation;

import com.hello2morrow.sonarplugin.persistence.IReportReader;
import com.hello2morrow.sonarplugin.persistence.ReportFileReader;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Ingmar
 *
 */
public class ReportFileReaderTest {

  private static final String reportFileName = "src/test/resources/sonargraph-sonar-report.xml";

  @Test
  public void testReadSonargraphReport() {
    final Project project = new Project("test");
    final IReportReader reader = new ReportFileReader();
    final Settings settings = TestHelper.initSettings(reportFileName);
    reader.readSonargraphReport(project, null, settings);
    assertNotNull(reader.getReport());

    settings.setProperty(SonargraphPluginBase.REPORT_PATH, "fakeDir/ReporFileName.xml");
    reader.readSonargraphReport(project, null, settings);
    assertNull(reader.getReport());

    settings.setProperty(SonargraphPluginBase.REPORT_PATH, "src/test/resources/report_error.xml");
    reader.readSonargraphReport(project, null, settings);
    assertNull(reader.getReport());

    final Project module = new Project("module");
    module.setParent(project);

    settings.setProperty(SonargraphPluginBase.REPORT_PATH, "fakeDir/ReporFileName.xml");
    reader.readSonargraphReport(project, null, settings);
    assertNull(reader.getReport());
  }
}
