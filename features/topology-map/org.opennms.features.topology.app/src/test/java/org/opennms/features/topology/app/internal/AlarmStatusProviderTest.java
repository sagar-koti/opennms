package org.opennms.features.topology.app.internal;

import com.vaadin.data.Item;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.topo.AbstractRef;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlarmStatusProviderTest {
    
    private class TestVertex extends AbstractRef implements Vertex {

        public TestVertex() {
            super("nodes", "1", null);
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public Item getItem() {
            return null;
        }

        @Override
        public String getTooltipText() {
            return null;
        }

        @Override
        public String getIconKey() {
            return null;
        }

        @Override
        public String getStyleName() {
            return null;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public void setParent(VertexRef parent) {}

        @Override
        public VertexRef getParent() {
            return null;
        }

        @Override
        public Integer getX() {
            return null;
        }

        @Override
        public Integer getY() {
            return null;
        }

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public String getIpAddress() {
            return null;
        }

        @Override
        public Integer getNodeID() {
            return 1;
        }
        
    }
    
    private AlarmDao m_alarmDao;
    private AlarmStatusProvider m_statusProvider;
    private VertexProvider m_vertexProvider;
    
    @Before
    public void setUp() {
        m_alarmDao = EasyMock.createMock(AlarmDao.class);
        m_statusProvider = new AlarmStatusProvider();
        m_statusProvider.setAlarmDao(m_alarmDao);

        m_vertexProvider = EasyMock.createMock(VertexProvider.class);
        EasyMock.expect(m_vertexProvider.getChildren(EasyMock.<VertexRef>anyObject())).andReturn(new ArrayList<Vertex>());
        EasyMock.replay(m_vertexProvider);
    }
    
    
    @Test
    public void testGetAlarmStatus() {
        Vertex vertex = new TestVertex();
        List<VertexRef> vertexList = new ArrayList<VertexRef>();
        vertexList.add(vertex);

        EasyMock.expect(
                m_alarmDao.getNodeAlarmSummariesIncludeAcknowledgedOnes(EasyMock.<List<Integer>>anyObject())).andReturn(createNormalAlarmSummaryList());
        
        EasyMock.replay(m_alarmDao);
        
        Map<VertexRef, Status> statusMap = m_statusProvider.getStatusForVertices(m_vertexProvider, vertexList);
        assertEquals(1, statusMap.size());
        assertEquals(vertex, new ArrayList<VertexRef>(statusMap.keySet()).get(0));
        String computeStatus = statusMap.get(vertex).computeStatus();
        assertTrue(computeStatus.equals("major"));
        
        EasyMock.verify(m_alarmDao);
    }


    private List<AlarmSummary> createNormalAlarmSummaryList() {
        List<AlarmSummary> alarms = new ArrayList<AlarmSummary>();
        alarms.add(new AlarmSummary(1, "node1", new Date(), OnmsSeverity.MAJOR, 1L));
        return alarms;
    }

}
