/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.examples.mgmt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.management.TableMetaData;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.persistence.entity.PropertyEntity;
import org.flowable.engine.ManagementService;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for the various operations of the {@link ManagementService}
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
@DisabledIfSystemProperty(named = "database", matches = "cockroachdb")
public class ManagementServiceTest extends PluggableFlowableTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementServiceTest.class);

    @Test
    public void testTableCount() {
        Map<String, Long> tableCount = managementService.getTableCount();

        String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();
        
        managementService.executeCommand(new Command<Void>() {

            @Override
            public Void execute(CommandContext commandContext) {
                List<PropertyEntity> properties = Context.getProcessEngineConfiguration().getPropertyEntityManager().findAll();
                for (PropertyEntity propertyEntity : properties) {
                    LOGGER.info("!!!Property {} {}", propertyEntity.getName(), propertyEntity.getValue());
                }
                return null;
            }
            
        });

        assertEquals(Long.valueOf(13), tableCount.get(tablePrefix + "ACT_GE_PROPERTY"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_GE_BYTEARRAY"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RE_DEPLOYMENT"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_EXECUTION"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_GROUP"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_MEMBERSHIP"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_ID_USER"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RE_PROCDEF"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_TASK"));
        assertEquals(Long.valueOf(0), tableCount.get(tablePrefix + "ACT_RU_IDENTITYLINK"));
    }

    @Test
    public void testGetTableMetaData() {

        String tablePrefix = processEngineConfiguration.getDatabaseTablePrefix();

        TableMetaData tableMetaData = managementService.getTableMetaData(tablePrefix + "ACT_RU_TASK");
        assertEquals(tableMetaData.getColumnNames().size(), tableMetaData.getColumnTypes().size());
        assertEquals(30, tableMetaData.getColumnNames().size());
 
        int assigneeIndex = tableMetaData.getColumnNames().indexOf("ASSIGNEE_");
        int createTimeIndex = tableMetaData.getColumnNames().indexOf("CREATE_TIME_");

        assertTrue(assigneeIndex >= 0);
        assertTrue(createTimeIndex >= 0);

        assertOneOf(new String[] { "VARCHAR", "NVARCHAR2", "nvarchar", "NVARCHAR" }, tableMetaData.getColumnTypes().get(assigneeIndex));
        assertOneOf(new String[] { "TIMESTAMP", "TIMESTAMP(6)", "datetime", "DATETIME" }, tableMetaData.getColumnTypes().get(createTimeIndex));
    }

    private void assertOneOf(String[] possibleValues, String currentValue) {
        for (String value : possibleValues) {
            if (currentValue.equals(value)) {
                return;
            }
        }
        fail("Value '" + currentValue + "' should be one of: " + Arrays.deepToString(possibleValues));
    }

}
