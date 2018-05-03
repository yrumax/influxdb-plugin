package jenkinsci.plugins.influxdb.generators;

import hudson.model.Job;
import hudson.model.Run;
import jenkinsci.plugins.influxdb.renderer.MeasurementRenderer;
import jenkinsci.plugins.influxdb.renderer.ProjectNameRenderer;
import org.influxdb.dto.Point;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class CustomDataPointGeneratorTest {

    public static final String JOB_NAME = "master";
    public static final int BUILD_NUMBER = 11;
    public static final String CUSTOM_PREFIX = "test_prefix";

    private Run build;
    private Job job;

    private MeasurementRenderer<Run<?, ?>> measurementRenderer;

    @Before
    public void before() {
        build = Mockito.mock(Run.class);
        job = Mockito.mock(Job.class);
        measurementRenderer = new ProjectNameRenderer(CUSTOM_PREFIX, null);

        Mockito.when(build.getNumber()).thenReturn(BUILD_NUMBER);
        Mockito.when(build.getParent()).thenReturn(job);
        Mockito.when(job.getName()).thenReturn(JOB_NAME);

    }

    @Test
    public void hasReportTest() {
        //check with customDataMap = null
        CustomDataPointGenerator cdGen1 = new CustomDataPointGenerator(measurementRenderer, CUSTOM_PREFIX, build, null);
        Assert.assertFalse(cdGen1.hasReport());

        //check with empty customDataMap
        CustomDataPointGenerator cdGen2 = new CustomDataPointGenerator(measurementRenderer, CUSTOM_PREFIX, build, Collections.<String, Map<String, Object>>emptyMap());
        Assert.assertFalse(cdGen2.hasReport());
    }

    @Test
    public void generateTest() {

        Map<String, Object> customData = new HashMap<String, Object>();
        customData.put("test1", 11);
        customData.put("test2", 22);

        List<Point> pointsToWrite = new ArrayList<Point>();

        CustomDataPointGenerator cdGen = new CustomDataPointGenerator(measurementRenderer, CUSTOM_PREFIX, build, customData);
        pointsToWrite.addAll(Arrays.asList(cdGen.generate()));

        String lineProtocol = pointsToWrite.get(0).lineProtocol();
        Assert.assertTrue(lineProtocol.startsWith("jenkins_custom_data,prefix=test_prefix,project_name=test_prefix_master build_number=11i,build_time="));
        Assert.assertTrue(lineProtocol.indexOf("project_name=\"test_prefix_master\",test1=11i,test2=22i")>0);
    }
}
