package org.slovit.kda;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TestJob {

    private static final Logger LOG = LoggerFactory.getLogger(TestJob.class);

    public static void main(String... args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.fromElements("dummy")
                .map(new PrintClasspath())
                .print();

        env.execute();
    }

    private static class PrintClasspath implements MapFunction<String, Object> {
        @Override
        public Object map(String s) throws Exception {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            while (classLoader != null) {
                LOG.warn("ClassLoader: {}", classLoader);
                for (Iterator<Object> iter = list(classLoader); iter.hasNext();) {
                    LOG.warn("\t{}", iter.next());
                }
                classLoader = classLoader.getParent();
            }

            return s;
        }

        private static Iterator<Object> list(ClassLoader classLoader) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
            Class clazz = classLoader.getClass();
            while (clazz != java.lang.ClassLoader.class) {
                clazz = clazz.getSuperclass();
            }
            java.lang.reflect.Field classesField = clazz.getDeclaredField("classes");
            classesField.setAccessible(true);
            List<Object> classes = (List) classesField.get(classLoader);
            return classes.iterator();
        }
    }
}
